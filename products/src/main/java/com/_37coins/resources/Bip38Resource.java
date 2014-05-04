package com._37coins.resources;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Random;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com._37coins.bip38.StoreRequest;
import com._37coins.bip38.WebfingerThread;
import com._37coins.dao.Pwallet;
import com.fruitcat.bitcoin.BIP38;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.DumpedPrivateKey;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.params.MainNetParams;

@Path(Bip38Resource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class Bip38Resource {
	public final static String PATH = "/pwallet";
	
	private final GenericRepository dao;
	private final HttpServletRequest httpReq;
	private final Random random;
	private final PeerGroup peerGroup;
	private final Wallet wallet;
	
	@Inject
	public Bip38Resource(ServletRequest request,
			Random random,
			PeerGroup peerGroup,
			Wallet wallet){
		httpReq = (HttpServletRequest)request;
		dao = (GenericRepository)httpReq.getAttribute("gr");
		this.random = random;
		this.peerGroup = peerGroup;
		this.wallet = wallet;
	}
	
	@POST
	public StoreRequest saveKey(StoreRequest sr){
		//validate key
		int rand = 0;
		Pwallet pw = new Pwallet()
			.setEncPrivKey(sr.getEncPrivKey())
			.setIdentifier(rand);
		dao.add(pw);
		return new StoreRequest().setIdentifier(pw.getIdentifier());
	}
	
	@GET
	@Path("/bal")
	public BigInteger getBal(){
		return wallet.getBalance();
	}

	@POST
	@Path("/claim")
	public void claim(StoreRequest sr){
		Pwallet pw = dao.queryEntity(new RNQuery().addFilter("identifier", sr.getIdentifier()), Pwallet.class);
		//lookup destication
		WebfingerThread wt = new WebfingerThread();
		wt.setup(sr);
		wt.start();
		try {
			//decrypt wallet
			String dk = BIP38.decrypt(sr.getPassword(), pw.getEncPrivKey());
			ECKey key = new DumpedPrivateKey(MainNetParams.get(), dk).getKey();
			wt.join();
			Address targetAddress = new Address(MainNetParams.get(), wt.getAddress());

			Wallet.SendResult result = wallet.sendCoins(peerGroup, targetAddress, new BigInteger("200000"));
			if (null!=result && result.tx!=null){
				dao.delete(pw.getId(), Pwallet.class);
				System.out.println("send: " + result.tx.getHashAsString());
			}
			//swipe to destination			
		} catch (UnsupportedEncodingException | AddressFormatException
				| GeneralSecurityException | InterruptedException e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		}
	}
	
	public int randInt(int min, int max) {
	    int randomNum = random.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
}

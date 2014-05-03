package com._37coins.resources;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Random;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com._37coins.bip38.StoreRequest;
import com._37coins.dao.Pwallet;
import com.fruitcat.bitcoin.BIP38;
import com.google.bitcoin.core.AddressFormatException;

@Path(HealthCheckResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class Bip38Resource {
	public final static String PATH = "/pwallet";
	
	private final GenericRepository dao;
	private final HttpServletRequest httpReq;
	private final Random random;
	
	@Inject
	public Bip38Resource(ServletRequest request,
			Random random){
		httpReq = (HttpServletRequest)request;
		dao = (GenericRepository)httpReq.getAttribute("gr");
		this.random = random;
	}
	
	@POST
	public StoreRequest saveKey(StoreRequest sr){
		//validate key
		Pwallet pw = new Pwallet()
			.setEncPrivKey(sr.getEncPrivKey())
			.setIdentifier(randInt(1000, 9999));
		dao.add(pw);
		return new StoreRequest().setIdentifier(pw.getIdentifier());
	}
	
	@POST
	@Path("/claim")
	public void claim(StoreRequest sr){
		Pwallet pw = dao.queryEntity(new RNQuery().addFilter("identifier", sr.getIdentifier()), Pwallet.class);
		//lookup destication
		
		
		try {
			String dk = BIP38.decrypt(sr.getPassword(), pw.getEncPrivKey());
			//decrypt wallet
			//color tranasction
			//swipe to destination			
		} catch (UnsupportedEncodingException | AddressFormatException
				| GeneralSecurityException e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		}
	}
	
	public int randInt(int min, int max) {
	    int randomNum = random.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
}

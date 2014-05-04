package com._37coins.bip38;

import org.btc4all.webfinger.WebfingerClient;
import org.btc4all.webfinger.WebfingerClientException;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.pojo.Link;

public class WebfingerThread extends Thread{
	private WebfingerClient wc = new WebfingerClient(true);
	private StoreRequest sr;
	private String rv;

	public void setup(StoreRequest sr){
		this.sr = sr;
	}

	public String getAddress(){
		return this.rv;
	}

	@Override
	public void run(){

		try {
			JsonResourceDescriptor jrd = wc.webFinger(sr.getUri());
			String bitcoinAddr = null;
			for (Link l : jrd.getLinks()){
				if (l.getRel().contains("bitcoin")){
					bitcoinAddr = l.getHref().toString();
				}
			}
			if (bitcoinAddr!=null){
				//parse link
				String[] str = 	bitcoinAddr.split(":");
				bitcoinAddr = str[(str.length>2)?1:str.length-1];
			}
			this. rv = bitcoinAddr;
		}catch(WebfingerClientException e){
			e.printStackTrace();
		}

	}

}

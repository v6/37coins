package com._37coins.helper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.web.GatewayUser;
import com._37coins.workflow.pojo.EmailFactor;

@Path(HelperResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class HelperResource {
	public final static String PATH = "/helper";
	public static Logger log = LoggerFactory.getLogger(HelperResource.class);
		
	private final Cache cache;


	@Inject
	public HelperResource(Cache cache){
			this.cache = cache;
	}
	
	@GET
	public String getToken(){
		Element e = cache.get("testHelper");
		if (null!=e){
			return (String)e.getObjectValue();
		}else{
			return null;
		}
	}
	
	@POST
	public void clear(){
		cache.flush();
	}
	
	@GET
	@Path("/all")
	public String tick(){
	    return "{\"KRW\": {\"24h_avg\": 594135.23,\"last\": 594135.23},\"EUR\": {\"24h_avg\": 434.85,\"ask\": 430.31,\"bid\": 427.93,\"last\": 428.65,\"timestamp\": \"Wed, 25 Jun 2014 01:59:53 -0000\",\"volume_btc\": 1490.44,\"volume_percent\": 5.31},  \"USD\": {\"24h_avg\": 591.57,\"ask\": 585.41,\"bid\": 582.18,\"last\": 583.16,\"timestamp\": \"Wed, 25 Jun 2014 01:59:53 -0000\",\"volume_btc\": 19208.09,\"volume_percent\": 68.42}}";
	}
	
	@POST
	@Path("/init")
	public void init(){
		Map<String,GatewayUser> gw = new HashMap<>();
		gw.put("DEV4N1JS2Z3476DE",new GatewayUser().setMobile("+491606789123").setId("DEV4N1JS2Z3476DE"));
		gw.put("NZV4N1JS2Z3476NK",new GatewayUser().setMobile("+821027423933").setId("NZV4N1JS2Z3476NK"));
		gw.put("OZV4N1JS2Z3476NL",new GatewayUser().setMobile("+821027423984").setId("OZV4N1JS2Z3476NL"));
		gw.put("PZV4N1JS2Z3476NM",new GatewayUser().setMobile("+821027423985").setId("PZV4N1JS2Z3476NM"));
		cache.put(new Element("gateways",gw));
		cache.put(new Element("emailVersmsemail",new EmailFactor().setTaskToken("taskToken").setEmailToken("bla")));
	}

}

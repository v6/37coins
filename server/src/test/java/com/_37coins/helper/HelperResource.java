package com._37coins.helper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.web.GatewayUser;
import com._37coins.workflow.pojo.EmailFactor;

@Path(HelperResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class HelperResource {
	public final static String PATH = "/helper";
	public static Logger log = LoggerFactory.getLogger(HelperResource.class);
		
	private final Cache cache;
		
	private final HttpServletRequest httpReq;

	@Inject
	public HelperResource(Cache cache,
				ServletRequest request){
			this.cache = cache;
			httpReq = (HttpServletRequest)request;
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

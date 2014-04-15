package com._37coins.resources;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.web.MerchantSession;


@Path(HealthCheckResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class HealthCheckResource {
	public final static String PATH = "/healthcheck";
	public static Logger log = LoggerFactory.getLogger(HealthCheckResource.class);
	
	@GET
	public Map<String,String> healthcheck(){
		Map<String,String> rv = new HashMap<>(1);
		rv.put("status", "ok!");
		return rv;
	}
	
	@POST
	@Path("/https")
	public void receive(MerchantSession sess){
		System.out.println(sess.getApiToken());
	}
	
}

package com._37coins.resources;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com._37coins.ProductsServletConfig;
import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.call.Call;
import com.plivo.helper.exception.PlivoException;


@Path(HealthCheckResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class HealthCheckResource {
	public final static String PATH = "/healthcheck";

	@GET
	public Map<String,String> healthcheck(){
		Map<String,String> rv = new HashMap<>(1);
		rv.put("status", "ok!");
		return rv;
	}
	
	@GET
	@Path("/start")
	public void startPlivo(@QueryParam("code")String code, @QueryParam("to") String to) throws PlivoException{
		RestAPI restAPI = new RestAPI(ProductsServletConfig.plivoKey, ProductsServletConfig.plivoSecret, "v1");
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
	    params.put("from", "+1201555"+code);
	    params.put("to", "+1"+to);
	    params.put("answer_url", ProductsServletConfig.basePath + "/plivo/answer/"+Locale.US.toString());
	    params.put("hangup_url", ProductsServletConfig.basePath + "/plivo/hangup");
	    params.put("ring_url", ProductsServletConfig.basePath + "/plivo/ring");
	    params.put("hangup_on_ring", "3");
	    params.put("time_limit", "1");
	    params.put("caller_name", "37 Coins");
	    Call response = restAPI.makeCall(params);
	    if (response.serverCode != 200 && response.serverCode != 201 && response.serverCode !=204){
	    	System.out.println(response.message);
	    	System.out.println(response.error);
	    	System.out.println(response.serverCode);
	    	throw new PlivoException(response.message);
	    }
	}
	
}

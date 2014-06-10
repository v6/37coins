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

import com._37coins.MerchantServletConfig;
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
	
}

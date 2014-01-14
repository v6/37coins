package com._37coins.resources;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com._37coins.MessagingServletConfig;
import com._37coins.web.WebfingerLink;
import com._37coins.web.WebfingerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Path(WebfingerResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class WebfingerResource {
	public final static String PATH = "/.well-known/webfinger";
	
	final private ObjectMapper mapper;
	
	@Inject
	public WebfingerResource(){
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
	}
	
	@GET
	public Response getWebfinger(@QueryParam("resource") String resource){
		List<WebfingerLink> links = new ArrayList<>();
		String[] email = resource.split("acct:");
		if (null==email || !email[1].contains("@")){
			throw new WebApplicationException("wrong format",Response.Status.BAD_REQUEST);
		}
		String mobile = email[1].split("@")[0];
		links.add(new WebfingerLink()
			.setType(MediaType.APPLICATION_JSON)
			.setRel("http://bitfinger.org/rel/bitcoin")
			.setHref(MessagingServletConfig.basePath+"/data/address?mobile="+mobile));
		WebfingerResponse rv = new WebfingerResponse()
			.setSubject(resource)
			.setLinks(links);
		try {
			return Response.ok(mapper.writeValueAsString(rv), "application/jrd+json").build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}

}

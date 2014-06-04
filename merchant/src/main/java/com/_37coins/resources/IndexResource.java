package com._37coins.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MerchantServletConfig;

@Path(IndexResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class IndexResource {
	public final static String PATH = "/";
	public static Logger log = LoggerFactory.getLogger(IndexResource.class);
	final private HttpServletRequest httpReq;
	
	@Inject public IndexResource(ServletRequest request) {
		this.httpReq = (HttpServletRequest)request;
	}

	@GET
	public Response index() throws URISyntaxException{
		return Response.seeOther(new URI(MerchantServletConfig.basePath+ httpReq.getPathInfo())).build();
	}
		
	@GET
	@Path("{path: .*}")
	public Response fullindex() throws URISyntaxException{
		return index();
	}
	
}
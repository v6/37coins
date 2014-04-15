package com._37coins.resources;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.workflow.pojo.DataSet;
import com.maxmind.geoip.LookupService;

import freemarker.template.TemplateException;

@Path(IndexResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class IndexResource {
	public final static String PATH = "/";
	public static Logger log = LoggerFactory.getLogger(IndexResource.class);
	final private MessageFactory htmlFactory;
	final private ServletContext servletContext;
	final private HttpServletRequest httpReq;
	final private LookupService lookupService;
	
	@Inject public IndexResource(ServletRequest request,
			MessageFactory htmlFactory, 
			ServletContext servletContext,
			LookupService lookupService) {
		this.httpReq = (HttpServletRequest)request;
		this.htmlFactory = htmlFactory;
		this.servletContext = servletContext;
		this.lookupService = lookupService;
	}
	
	public static String getRemoteAddress(HttpServletRequest httpReq){
		String addr = httpReq.getHeader("X-Forwarded-For");
		if (null==addr || addr.length()<7){
			addr = httpReq.getRemoteAddr();
		}
		return addr;
	}

	@GET
	public Response index(@HeaderParam("Accept-Language") String lng){
		Map<String,String> data = new HashMap<>();
		
		data.put("resPath", MessagingServletConfig.resPath);
		data.put("basePath", MessagingServletConfig.basePath);
		data.put("gaTrackingId", MessagingServletConfig.gaTrackingId);
		data.put("srvcPath", MessagingServletConfig.srvcPath);
		String country = null;
		try{
			country = lookupService.getCountry(IndexResource.getRemoteAddress(httpReq)).getCode();
			country = (country.equals("--"))?null:country;
		}catch(Exception e){
			log.error("geoip exception",e);
			e.printStackTrace();
		}
		data.put("country", country);
		data.put("captchaPubKey", MessagingServletConfig.captchaPubKey);
		data.put("lng", (lng!=null)?lng.split(",")[0]:"en-US");
		DataSet ds = new DataSet()
			.setService("index.html")
			.setPayload(data);
		String rsp;
		try {
			rsp = htmlFactory.processTemplate(ds, null);
		} catch (IOException | TemplateException e) {
			throw new WebApplicationException("template not loaded",
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
		return Response.ok(rsp, MediaType.TEXT_HTML_TYPE).build();
	}
	
	@GET
	@Path("robots.txt")
	public Response robots(){
		String response = "User-agent: *\n" +
				"Disallow: /ticket/\n" +
				"Disallow: /account/\n" +
				"Disallow: /envayaTest.html\n" +
				"Sitemap: "+MessagingServletConfig.resPath+"/sitemap.xml";
		return Response.ok(response, MediaType.TEXT_PLAIN_TYPE).build();
	}
	
	@GET
	@Path("sitemap.xml")
	public Response sitemap(){
		return Response.seeOther(URI.create(MessagingServletConfig.resPath+"/sitemap.xml")).build();
	}
	
	@GET
	@Path("{path: .*}")
	public Response fullindex(@HeaderParam("Accept-Language") String lng){
		return index(lng);
	}
	
	/*
	 * ###################### TEST SCOPE
	 */
	
	@GET
	@Path("envayaTest.html")
	public Response envayaTest(){
		Map<String,String> data = new HashMap<>();
		data.put("lng", "en-US");
		DataSet ds = new DataSet()
			.setService("envayaTest.html")
			.setPayload(data);
		String rsp;
		try {
			rsp = htmlFactory.processTemplate(ds, null);
		} catch (IOException | TemplateException e) {
			throw new WebApplicationException("template not loaded",
					javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
		return Response.ok(rsp, MediaType.TEXT_HTML_TYPE).build();
	}
	
}
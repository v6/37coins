package com._37coins.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.sf.ehcache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.web.MerchantSession;
import com._37coins.workflow.pojo.DataSet;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.inject.Inject;

import freemarker.template.TemplateException;

@Path(MerchantResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class MerchantResource {
	public final static String PATH = "/merchant";
	public static Logger log = LoggerFactory.getLogger(MerchantResource.class);
	
	final private MessageFactory htmlFactory;
	final private Cache cache;
	final private SocketIOServer server;
	
	@Inject
	public MerchantResource(MessageFactory htmlFactory,
			Cache cache,
			SocketIOServer server){
		this.htmlFactory = htmlFactory;
		this.cache = cache;
		this.server = server;
	}
	
	
	@GET
	public Response merchant(@HeaderParam("Accept-Language") String lng){
		Map<String,String> data = new HashMap<>();
		data.put("resPath", MessagingServletConfig.merchantResPath);
		data.put("basePath", MessagingServletConfig.basePath);
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
	
	//0. if there is a referrer url, check it with the backend
	//1. receive the phone number through socket.io, check if call already in progress
	//2. initiate the phone call, set state to calling, set form on frontend to disabled
	//   once call ended, verify number, notify frontend
	//3. generate new apitoken/key and replace it
	
	@GET
	@Path("/test/fin")
	public void testFin(@QueryParam("accountToken") String accountToken){
		server.getRoomOperations(accountToken+"/"+accountToken).sendJsonObject(new MerchantSession().setAction("failed"));
	}
	
	@GET
	@Path("/test/fail")
	public void testFail(@QueryParam("accountToken") String accountToken){
		server.getRoomOperations(accountToken+"/"+accountToken).sendJsonObject(new MerchantSession().setAction("failed"));
	}
	
	@POST
	@Path("/charge/{accountToken}")
	public Map<String,String> charge(
			@PathParam("accountToken") String accountToken, 
			MultivaluedMap<String, String> params,
			@HeaderParam("X-Request-Signature") String sig,
			@Context UriInfo uriInfo){//charge request hording amount, 
		
		return null;
	}

}

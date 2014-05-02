package com._37coins.resources;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.BasicAccessAuthFilter;
import com._37coins.web.WebfingerLink;
import com._37coins.web.WebfingerResponse;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Path(WebfingerResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class WebfingerResource {
	public final static String PATH = "/.well-known/webfinger";
	public static Logger log = LoggerFactory.getLogger(WebfingerResource.class);
	
	final private ObjectMapper mapper;
	final private InitialLdapContext ctx;
	final private Cache cache;
	final private NonTxWorkflowClientExternalFactoryImpl nonTxFactory;
	
	@Inject
	public WebfingerResource(ServletRequest request, 
			NonTxWorkflowClientExternalFactoryImpl nonTxFactory,
			Cache cache) {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		ctx = (InitialLdapContext)httpReq.getAttribute("ctx");
		this.cache = cache;
		this.nonTxFactory = nonTxFactory;
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
	}
	
	@GET
	public void getWebfinger(@QueryParam("resource") String resource,
			@Suspended final AsyncResponse asyncResponse){
		String[] email = resource.split("acct:");
		if (null == email[1]){
			asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
		}
		String mobile = email[1].split("@")[0];
		
		String cn = null;
		try {
			Attributes atts = BasicAccessAuthFilter.searchUnique("(&(objectClass=person)(mobile="+mobile+"))", ctx).getAttributes();
			cn = (String)atts.get("cn").get();
		} catch (IllegalStateException | NamingException e1) {
			log.error("webfinger exception",e1);
			e1.printStackTrace();
			asyncResponse.resume(Response.status(Response.Status.NOT_FOUND).build());
		}

		Element e = cache.get("address"+cn);
		Element e2 = cache.get("addressReq"+cn);
		if (null!=e && !e.isExpired()){
			sendResponse(resource, (String)e.getObjectValue(), asyncResponse);
		}
		if (null==e2 || e2.isExpired()){
			DataSet data = new DataSet()
				.setAction(Action.GW_DEPOSIT_REQ)
				.setCn(cn);
			nonTxFactory.getClient(data.getAction()+"-"+cn).executeCommand(data);
			cache.put(new Element("addressReq"+cn, true));
		}
		for (int i = 0;i<35;i++){
			e = cache.get("address"+cn);
			if (null==e){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					log.error("webfinger exception",e1);
					e1.printStackTrace();
					asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
				}
			}else{
				sendResponse(resource, (String)e.getObjectValue(), asyncResponse);
			}
		}
		throw new WebApplicationException("account not found", Response.Status.NOT_FOUND);
	}
	
	private void sendResponse(String resource, String address, final AsyncResponse asyncResponse){
		List<WebfingerLink> links = new ArrayList<>();
		links.add(new WebfingerLink()
			.setType("bitcoin")
			.setRel("http://bitfinger.org/rel/bitcoin")
			.setHref("bitcoin:"+address));
		WebfingerResponse rv = new WebfingerResponse()
			.setSubject(resource)
			.setLinks(links);
		try {
			asyncResponse.resume(Response.ok(mapper.writeValueAsString(rv), "application/jrd+json").build());
		} catch (JsonProcessingException ex) {
			asyncResponse.resume(ex);
		}
	}

}

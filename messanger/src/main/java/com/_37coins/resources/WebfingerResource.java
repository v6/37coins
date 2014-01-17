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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

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
	public Response getWebfinger(@QueryParam("resource") String resource){
		String[] email = resource.split("acct:");
		if (null == email[1]){
			throw new WebApplicationException("wrong format",Response.Status.BAD_REQUEST);
		}
		String mobile = email[1].split("@")[0];
		
		String cn = null;
		try {
			Attributes atts = BasicAccessAuthFilter.searchUnique("(&(objectClass=person)(mobile="+mobile+"))", ctx).getAttributes();
			cn = (String)atts.get("cn").get();
		} catch (IllegalStateException | NamingException e1) {
			e1.printStackTrace();
			throw new WebApplicationException("account not found", Response.Status.NOT_FOUND);
		}

		Element e = cache.get("address"+cn);
		Element e2 = cache.get("addressReq"+cn);
		if (null!=e && !e.isExpired()){
			return sendResponse(resource, (String)e.getObjectValue());
		}
		if (null==e2 || e2.isExpired()){
			DataSet data = new DataSet()
				.setAction(Action.GW_DEPOSIT_REQ)
				.setCn(cn);
			nonTxFactory.getClient(data.getAction()+"-"+cn).executeCommand(data);
			cache.put(new Element("addressReq"+cn, true));
		}
		for (int i = 0;i<15;i++){
			e = cache.get("address"+cn);
			if (null==e){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					throw new WebApplicationException(e1,Response.Status.INTERNAL_SERVER_ERROR);
				}
			}else{
				return sendResponse(resource, (String)e.getObjectValue());
			}
		}
		throw new WebApplicationException("account not found", Response.Status.NOT_FOUND);
	}
	
	private Response sendResponse(String resource, String address){
		List<WebfingerLink> links = new ArrayList<>();
		links.add(new WebfingerLink()
			.setType("bitcoin")
			.setRel("http://bitfinger.org/rel/bitcoin")
			.setHref("bitcoin:"+address));
		WebfingerResponse rv = new WebfingerResponse()
			.setSubject(resource)
			.setLinks(links);
		try {
			return Response.ok(mapper.writeValueAsString(rv), "application/jrd+json").build();
		} catch (JsonProcessingException ex) {
			return null;
		}
	}

}

package com._37coins.resources;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
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

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import com._37coins.BasicAccessAuthFilter;
import com._37coins.MessagingServletConfig;
import com._37coins.web.GatewayUser;
import com._37coins.web.Queue;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Path(DataResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class DataResource {
	public final static String PATH = "/data";
	
	final private InitialLdapContext ctx;
	final private Cache cache;
	final private NonTxWorkflowClientExternalFactoryImpl nonTxFactory;
	
	@Inject public DataResource(ServletRequest request, 
			NonTxWorkflowClientExternalFactoryImpl nonTxFactory,
			Cache cache) {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		ctx = (InitialLdapContext)httpReq.getAttribute("ctx");
		this.cache = cache;
		this.nonTxFactory = nonTxFactory;
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/gateways")
	public Set<GatewayUser> getGateways(){
		Element e = cache.get("gateways");
		if (null!=e && !e.isExpired()){
			Set<GatewayUser> gateways = (Set<GatewayUser>)e.getObjectValue();
			return gateways;
		}
		Set<GatewayUser> rv = new HashSet<GatewayUser>();
		NamingEnumeration<?> namingEnum = null;
		try{
			ctx.setRequestControls(null);
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setTimeLimit(1000);
			namingEnum = ctx.search("ou=gateways,"+MessagingServletConfig.ldapBaseDn, "(objectClass=person)", searchControls);
			while (namingEnum.hasMore()){
				Attributes atts = ((SearchResult) namingEnum.next()).getAttributes();
				String mobile = (atts.get("mobile")!=null)?(String)atts.get("mobile").get():null;
				String cn = (String)atts.get("cn").get();
				BigDecimal fee = (atts.get("description")!=null)?new BigDecimal((String)atts.get("description").get()):null;
				if (null!=mobile && null!=fee){
					PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
					PhoneNumber pn = phoneUtil.parse(mobile, "ZZ");
					String cc = phoneUtil.getRegionCodeForCountryCode(pn.getCountryCode());
					GatewayUser gu = new GatewayUser()
						.setMobile(PhoneNumberUtil.getInstance().format(pn,PhoneNumberFormat.E164))
						.setFee(fee)
						.setLocale(new Builder().setRegion(cc).build())
						.setId(cn);
					rv.add(gu);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
		}finally{
			if(null!=namingEnum)
				try {
					namingEnum.close();
				} catch (NamingException e1) {}
		}
		
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(MessagingServletConfig.amqpHost, 15672),
                new UsernamePasswordCredentials(MessagingServletConfig.amqpUser, MessagingServletConfig.amqpPassword));
		HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
		Set<GatewayUser> active = new HashSet<GatewayUser>();
		for(GatewayUser gu: rv){
			try{
				HttpGet someHttpGet = new HttpGet("http://"+MessagingServletConfig.amqpHost+":15672/api/queues/%2f/"+gu.getId());
				URI uri = new URIBuilder(someHttpGet.getURI()).build();
				HttpRequestBase request = new HttpGet(uri);
				HttpResponse response = client.execute(request);
				if (new ObjectMapper().readValue(response.getEntity().getContent(),Queue.class).getConsumers()>0){
					active.add(gu);
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		cache.put(new Element("gateways", active));
		return active;
	}
	
	@GET
	@Path("/address")
	public Map<String,String> getAddress(@QueryParam("mobile") String mobile){
		Map<String,String> rv = new HashMap<>();
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
			rv.put("address", (String)e.getObjectValue());
			return rv;
		}
		if (null==e2 || e2.isExpired()){
			DataSet data = new DataSet()
				.setAction(Action.GW_DEPOSIT_REQ)
				.setCn(cn);
			nonTxFactory.getClient(data.getAction()+"-"+cn).executeCommand(data);
			cache.put(new Element("addressReq"+cn, true));
		}
		throw new WebApplicationException("cache miss, requested, ask again later.", Response.Status.ACCEPTED);
	}


}

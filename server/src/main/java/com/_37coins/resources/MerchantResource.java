package com._37coins.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.restnucleus.filter.HmacFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.BasicAccessAuthFilter;
import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.parse.ParserAction;
import com._37coins.parse.ParserClient;
import com._37coins.persistence.dto.Transaction;
import com._37coins.workflow.WithdrawalWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.Withdrawal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxmind.geoip.LookupService;

import freemarker.template.TemplateException;

@Path(MerchantResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class MerchantResource {
	public final static String PATH = "/merchant";
	public static Logger log = LoggerFactory.getLogger(MerchantResource.class);
	
	final private MessageFactory htmlFactory;
	final private ObjectMapper mapper;
	final private HttpServletRequest httpReq;
	final private LookupService lookupService;
	private final ParserClient parserClient;
	final private InitialLdapContext ctx;
	private final WithdrawalWorkflowClientExternalFactoryImpl withdrawalFactory;
	private final Cache cache;
	private int localPort;
	
	@Inject
	public MerchantResource(ServletRequest request,
			MessageFactory htmlFactory,
			ParserClient parserClient,
			Cache cache,
			WithdrawalWorkflowClientExternalFactoryImpl withdrawalFactory,
			LookupService lookupService){
		this.httpReq = (HttpServletRequest)request;
		localPort = httpReq.getLocalPort();
		this.htmlFactory = htmlFactory;
		this.mapper = new ObjectMapper();
		this.parserClient = parserClient;
		this.lookupService = lookupService;
		this.cache = cache;
		this.withdrawalFactory = withdrawalFactory;
		this.ctx = (InitialLdapContext)httpReq.getAttribute("ctx");
	}
	
	
	@GET
	public Response merchant(@HeaderParam("Accept-Language") String lng,
			@QueryParam("delivery")String delivery,
			@QueryParam("deliveryParam")String deliveryParam){
		Map<String,String> data = new HashMap<>();
		data.put("resPath", MessagingServletConfig.resPath);
		data.put("delivery", delivery);
		data.put("deliveryParam", deliveryParam);
		data.put("basePath", MessagingServletConfig.basePath);
		data.put("gaTrackingId", MessagingServletConfig.gaTrackingId);
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
	
	public void authenticate(String apiToken, Withdrawal withdrawal, String path, String sig){
		String apiSecret = null;
		try{
			//read the user
			String sanitizedToken = BasicAccessAuthFilter.escapeLDAPSearchFilter(apiToken);
			Attributes atts = BasicAccessAuthFilter.searchUnique("(&(objectClass=person)(description="+sanitizedToken+"))", ctx).getAttributes();
			apiSecret = (atts.get("departmentNumber")!=null)?(String)atts.get("departmentNumber").get():null;
		}catch(NamingException e){
			throw new WebApplicationException(e,Response.Status.INTERNAL_SERVER_ERROR);
		}
		MultivaluedMap<String,String> mvm = null;
		try {
			mvm = HmacFilter.parseJson(mapper.writeValueAsBytes(withdrawal));
		} catch (IOException e) {
			throw new WebApplicationException(e,Response.Status.INTERNAL_SERVER_ERROR);
		}
		if (mvm==null || mvm.size()<2 || withdrawal.getAmount()==null || withdrawal.getPayDest()==null){
			throw new WebApplicationException("mandatory data (amount, payDest) missing.", Response.Status.BAD_REQUEST);
		}
		String url = MessagingServletConfig.basePath + path;
		String calcSig = null;
		try {
			calcSig = HmacFilter.calculateSignature(url, mvm, apiSecret);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new WebApplicationException(e,Response.Status.INTERNAL_SERVER_ERROR);
		}
		if (null==sig || null==calcSig || !calcSig.equals(sig)){
			throw new WebApplicationException("signatures don't match",Response.Status.UNAUTHORIZED);
		}
	}

	@POST
	@Path("/charge/{apiToken}/to/{phone}")
	public void chargePhone(Withdrawal withdrawal,
			@PathParam("apiToken") String apiToken,
			@PathParam("phone") String phone,
			@HeaderParam(HmacFilter.AUTH_HEADER) String sig,
			@Context UriInfo uriInfo,
			@Suspended final AsyncResponse asyncResponse){
		try{
			authenticate(apiToken, withdrawal, uriInfo.getPath(), sig);
		}catch (Exception e) {
			asyncResponse.resume(e);
		}
		String from = null;
		String gateway = null;
		from = BasicAccessAuthFilter.escapeLDAPSearchFilter(phone);
		try{
			Attributes atts = ctx.getAttributes("cn="+ from + ",ou=accounts,"+MessagingServletConfig.ldapBaseDn,new String[]{"manager"});
			String gwDn = (atts.get("manager")!=null)?(String)atts.get("manager").get():null;
			Attributes gwAtts = ctx.getAttributes(gwDn,new String[]{"mobile"});
			gateway = (gwAtts.get("mobile")!=null)?(String)gwAtts.get("mobile").get():null;
		}catch(Exception e){
			log.error("merchant api error" + from + " not found",e);
			e.printStackTrace();
		}
		if (null==from || null==gateway){
			asyncResponse.resume(new WebApplicationException(Response.Status.NOT_FOUND));
		}
		parserClient.start(from, gateway, "send "+withdrawal.getAmount().multiply(new BigDecimal(1000))+" "+withdrawal.getPayDest().getAddress(), localPort,
		new ParserAction() {
			@Override
			public void handleWithdrawal(DataSet data) {
				//save the transaction id to db
				Transaction t = new Transaction().setKey(Transaction.generateKey()).setState(Transaction.State.STARTED);
				cache.put(new Element(t.getKey(), t));
				withdrawalFactory.getClient(t.getKey()).executeCommand(data);
				asyncResponse.resume(Response.ok().build());
			}
			@Override
			public void handleResponse(DataSet data) {
				asyncResponse.resume(new WebApplicationException(data.getAction().toString(), Response.Status.BAD_REQUEST));
			}
			@Override
			public void handleDeposit(DataSet data) {
				asyncResponse.resume(new WebApplicationException(data.getAction().toString(), Response.Status.BAD_REQUEST));
			}
			@Override
			public void handleConfirm(DataSet data) {
				asyncResponse.resume(new WebApplicationException(data.getAction().toString(), Response.Status.BAD_REQUEST));
			}
		});
	}

	@POST
	@Path("/charge/{apiToken}")
	public Response charge(Withdrawal withdrawal,
			@PathParam("apiToken") String apiToken,
			@HeaderParam(HmacFilter.AUTH_HEADER) String sig,
			@Context UriInfo uriInfo){
		authenticate(apiToken, withdrawal, uriInfo.getPath(), sig);
		try{
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost req = new HttpPost(MessagingServletConfig.paymentsPath+"/charge");
			String reqValue = mapper.writeValueAsString(withdrawal);
			StringEntity entity = new StringEntity(reqValue, "UTF-8");
			entity.setContentType("application/json");
			String reqSig = HmacFilter.calculateSignature(
					MessagingServletConfig.paymentsPath+"/charge",
					HmacFilter.parseJson(reqValue.getBytes()),
					MessagingServletConfig.hmacToken);
			req.setHeader(HmacFilter.AUTH_HEADER, reqSig);
			req.setEntity(entity);
			CloseableHttpResponse rsp = httpclient.execute(req);
			if (rsp.getStatusLine().getStatusCode()==200){
				return Response.ok(rsp.getEntity().getContent(), MediaType.APPLICATION_JSON).build();
			}else{
				throw new WebApplicationException("received status: "+rsp.getStatusLine().getStatusCode(),Response.Status.INTERNAL_SERVER_ERROR);
			}
		}catch(Exception ex){
			log.error("merchant exception",ex);
			throw new WebApplicationException(ex,Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

}

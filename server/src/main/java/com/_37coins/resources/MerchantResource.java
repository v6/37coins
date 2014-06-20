package com._37coins.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.inject.Inject;
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

import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;
import org.restnucleus.filter.DigestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.merchant.MerchantClient;
import com._37coins.merchant.pojo.MerchantRequest;
import com._37coins.merchant.pojo.MerchantResponse;
import com._37coins.parse.ParserAction;
import com._37coins.parse.ParserClient;
import com._37coins.persistence.dao.Account;
import com._37coins.persistence.dao.Gateway;
import com._37coins.util.ResourceBundleFactory;
import com._37coins.web.MerchantSession;
import com._37coins.web.Transaction;
import com._37coins.workflow.WithdrawalWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxmind.geoip.LookupService;

import freemarker.template.TemplateException;

@Path(MerchantResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class MerchantResource {
	public final static String PATH = "/merchant";
	public static Logger log = LoggerFactory.getLogger(MerchantResource.class);
	
	private final MessageFactory htmlFactory;
	private final ObjectMapper mapper;
	private final HttpServletRequest httpReq;
	private final LookupService lookupService;
	private final ParserClient parserClient;
	private final GenericRepository dao;
	private final WithdrawalWorkflowClientExternalFactoryImpl withdrawalFactory;
	private final Cache cache;
	private final MerchantClient merchantClient;
	private int localPort;
	final private ResourceBundleFactory rbf;
	
	@Inject
	public MerchantResource(ServletRequest request,
			MessageFactory htmlFactory,
			ParserClient parserClient,
			ResourceBundleFactory rbf,
			Cache cache, MerchantClient merchantClient,
			WithdrawalWorkflowClientExternalFactoryImpl withdrawalFactory,
			LookupService lookupService){
		this.httpReq = (HttpServletRequest)request;
		localPort = httpReq.getLocalPort();
		this.htmlFactory = htmlFactory;
		this.rbf = rbf;
		this.mapper = new ObjectMapper();
		this.merchantClient = merchantClient;
		this.parserClient = parserClient;
		this.lookupService = lookupService;
		this.cache = cache;
		this.withdrawalFactory = withdrawalFactory;
		dao = (GenericRepository)httpReq.getAttribute("gr");
	}
	
	
	@GET
	public Response merchant(@HeaderParam("Accept-Language") String lng,
	        @Context UriInfo uriInfo,
			@QueryParam("delivery")String delivery,
			@QueryParam("deliveryParam")String deliveryParam){
		Map<String,Object> data = IndexResource.prepare(lng, uriInfo, lookupService, httpReq, rbf);
		data.put("delivery", delivery);
	    data.put("deliveryParam", deliveryParam);
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
	
	/**
	 * allow front-end to notify user about taken account 
	 * @param email
	 */
	@GET
	@Path("/check")
	public String checkDisplayName(@QueryParam("displayName") String displayName){
		//how to avoid account fishing?
	    if (cache.incr(TicketResource.REQUEST_SCOPE+TicketResource.getRemoteAddress(httpReq))>50){
				return "false"; //to many requests
		}
		//check it's not taken already
	    RNQuery q = new RNQuery().addFilter("displayName", displayName);
	    Account a = dao.queryEntity(q, Account.class, false);
	    if (null==a){
	        return "false";
	    }
		return "true";
	}
	
	@POST
	@Path("/name")
	public void setDisplayName(MerchantSession merchantSession){
		if (null==merchantSession.getSessionToken()||null==merchantSession.getDisplayName()){
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		Element el = cache.get("merchant"+ merchantSession.getSessionToken());
		MerchantSession ms = (MerchantSession)el.getObjectValue();
		if (null==ms || null==ms.getDisplayName()){
			throw new WebApplicationException("not verified merchant", Response.Status.FORBIDDEN);
		}
		RNQuery q = new RNQuery().addFilter("mobile", ms.getPhoneNumber());
		Account a = dao.queryEntity(q, Account.class);
		a.setDisplayName(merchantSession.getDisplayName());
	}
	
	public String authenticate(String apiToken, MerchantRequest withdrawal, String path, String sig){
		//read the user
	    Account a = dao.queryEntity(new RNQuery().addFilter("apiToken", apiToken), Account.class);
		MultivaluedMap<String,String> mvm = null;
		try {
			mvm = DigestFilter.parseJson(mapper.writeValueAsBytes(withdrawal));
		} catch (IOException e) {
			throw new WebApplicationException(e,Response.Status.INTERNAL_SERVER_ERROR);
		}
		if (mvm==null || mvm.size()<2 || withdrawal.getAmount()==null || withdrawal.getPayDest()==null){
			throw new WebApplicationException("mandatory data (amount, payDest) missing.", Response.Status.BAD_REQUEST);
		}
		String url = MessagingServletConfig.basePath + "/" + path;
		String calcSig = null;
		try {
			calcSig = DigestFilter.calculateSignature(url, mvm, a.getApiSecret());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new WebApplicationException(e,Response.Status.INTERNAL_SERVER_ERROR);
		}
		if (null==sig || null==calcSig || !calcSig.equals(sig)){
			throw new WebApplicationException("signatures don't match",Response.Status.UNAUTHORIZED);
		}
		return a.getDisplayName();
	}

	@POST
	@Path("/charge/{apiToken}/to/{phone}")
	public void chargePhone(MerchantRequest request,
			@PathParam("apiToken") String apiToken,
			@PathParam("phone") String mobile,
			@HeaderParam(DigestFilter.AUTH_HEADER) String sig,
			@Context UriInfo uriInfo,
			@Suspended final AsyncResponse asyncResponse){
		String dispName=null;
		try{
			dispName = authenticate(apiToken, request, uriInfo.getPath(), sig);
		}catch (Exception e) {
			asyncResponse.resume(e);
		}
		if (null!=request.getCallbackUrl()||null!=request.getConversion()||null!=request.getTimeout()){
			asyncResponse.resume(Response.status(Response.Status.NOT_IMPLEMENTED).build());
		}
		String from = null;
		String gateway = null;
		mobile = (mobile.contains("+"))?mobile:"+"+mobile;
		RNQuery q = new RNQuery().addFilter("mobile", mobile);
		Gateway g = dao.queryEntity(q, Account.class).getOwner();
		if (null==from || null==g.getMobile()){
			asyncResponse.resume(new WebApplicationException(Response.Status.NOT_FOUND));
		}
		final String displayName = dispName;
		parserClient.start(from, gateway, gateway, "send "+request.getAmount().multiply(new BigDecimal(1000))+" "+request.getPayDest().getAddress(), localPort,
		new ParserAction() {
			@Override
			public void handleWithdrawal(DataSet data) {
				//save the transaction id to db
				Transaction t = new Transaction().setKey(Transaction.generateKey()).setState(Transaction.State.STARTED);
				cache.put(new Element(t.getKey(), t));
				withdrawalFactory.getClient(t.getKey()).executeCommand(data);
				asyncResponse.resume(new MerchantResponse().setDisplayName(displayName));
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
	public MerchantResponse charge(MerchantRequest request,
			@PathParam("apiToken") String apiToken,
			@HeaderParam(DigestFilter.AUTH_HEADER) String sig,
			@Context UriInfo uriInfo){
		String displayName = authenticate(apiToken, request, uriInfo.getPath(), sig);
		if (null!=request.getCallbackUrl()){
			throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
		}
		try{
		    return merchantClient.charge(request.getAmount(), request.getPayDest().getAddress(), request.getOrderName()).setDisplayName(displayName);
		}catch(Exception ex){
			log.error("merchant exception",ex);
			throw new WebApplicationException(ex,Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

}

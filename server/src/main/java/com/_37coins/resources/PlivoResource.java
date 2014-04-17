package com._37coins.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

import javax.inject.Inject;
import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.BasicAccessAuthFilter;
import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.parse.ParserAction;
import com._37coins.parse.ParserClient;
import com._37coins.persistence.dto.Transaction;
import com._37coins.persistence.dto.Transaction.State;
import com._37coins.plivo.GetDigits;
import com._37coins.plivo.Redirect;
import com._37coins.plivo.Speak;
import com._37coins.plivo.Wait;
import com._37coins.plivo.XmlCharacterHandler;
import com._37coins.web.MerchantSession;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClient;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClientFactory;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClientFactoryImpl;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import freemarker.template.TemplateException;

@Path(PlivoResource.PATH)
@Produces(MediaType.APPLICATION_XML)
public class PlivoResource {
	public static Logger log = LoggerFactory.getLogger(PlivoResource.class);
	public final static String PATH = "/plivo";
	public static final int NUM_DIGIT = 5;
	
	final private InitialLdapContext ctx;
	
	final private JndiLdapContextFactory jlc;
	
	private final AmazonSimpleWorkflow swfService;
	
	private final MessageFactory msgFactory;
	
	final private Cache cache;
	
	private Marshaller marshaller;
	private final SocketIOServer server;
	private final NonTxWorkflowClientExternalFactoryImpl nonTxFactory;
	private final ParserClient parserClient;
	private int localPort;
	
	@Inject public PlivoResource(
			JndiLdapContextFactory jlc,
			ServletRequest request,
			AmazonSimpleWorkflow swfService,
			MessageFactory msgFactory,
			Cache cache,ParserClient parserClient,
			NonTxWorkflowClientExternalFactoryImpl nonTxFactory,
			SocketIOServer server) {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		this.parserClient = parserClient;
		this.server = server;
		this.nonTxFactory = nonTxFactory;
		localPort = httpReq.getLocalPort();
		ctx = (InitialLdapContext)httpReq.getAttribute("ctx");
		this.swfService = swfService;
		this.msgFactory = msgFactory;
		this.cache = cache;
		this.jlc = jlc;
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(com._37coins.plivo.Response.class);
			this.marshaller = jc.createMarshaller();
	        marshaller.setProperty(CharacterEscapeHandler.class.getName(),new XmlCharacterHandler());
		} catch (JAXBException e) {
			log.error("jaxb exception",e);
			e.printStackTrace();
		}
		
	}
	
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/answer/{cn}/{workflowId}/{locale}")
	public Response answer(
			@PathParam("cn") String cn,
			@PathParam("workflowId") String workflowId,
			@PathParam("locale") String locale){
		com._37coins.plivo.Response rv = null;
		DataSet ds = new DataSet().setLocaleString(locale);
		String sanitizedCn = BasicAccessAuthFilter.escapeDN(cn);
		String dn = "cn="+sanitizedCn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn;
		Object pw = null;
		try{
			Attributes atts = ctx.getAttributes(dn,new String[]{"userPassword"});
			pw = (atts.get("userPassword")!=null)?atts.get("userPassword").get():null;
		}catch(Exception e){
			log.error("plivo answer exception",e);
			e.printStackTrace();
			throw new WebApplicationException(e, javax.ws.rs.core.Response.Status.NOT_FOUND);
		}
		if (pw!=null){
			//only check pin
			try {
				rv = new com._37coins.plivo.Response()
					.add(new Speak().setText(msgFactory.getText("VoiceHello",ds)).setLanguage(locale))
					.add(new GetDigits()
						.setAction(MessagingServletConfig.basePath+"/plivo/check/"+sanitizedCn+"/"+workflowId+"/"+locale)
						.setNumDigits(NUM_DIGIT)
						.setRedirect(true)
						.setSpeak(new Speak()
							.setText(msgFactory.getText("VoiceEnter",ds)).setLanguage(locale)));
			} catch (IOException | TemplateException e) {
				log.error("plivo answer exception",e);
				e.printStackTrace();
			}
		}else{
			//create a new pin
			try {
				rv = new com._37coins.plivo.Response()
					.add(new Speak().setText(msgFactory.getText("VoiceHello",ds)+" "+msgFactory.getText("VoiceSetup",ds)).setLanguage(locale))
					.add(new Wait())
					.add(new GetDigits()
						.setAction(MessagingServletConfig.basePath+ "/plivo/create/"+cn+"/"+workflowId+"/"+locale)
						.setNumDigits(NUM_DIGIT)
						.setRedirect(true)
						.setSpeak(new Speak()
							.setText(msgFactory.getText("VoiceCreate",ds)).setLanguage(locale)));
			} catch (IOException | TemplateException e) {
				log.error("plivo answer exception",e);
				e.printStackTrace();
			}
		}
		try {
			StringWriter sw = new StringWriter();
			marshaller.marshal(rv, sw);
			return Response.ok(sw.toString(), MediaType.APPLICATION_XML).build();
		} catch (JAXBException e) {
			return null;
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/hangup/{workflowId}")
	public void hangup(
			MultivaluedMap<String, String> params,
			@PathParam("workflowId") String workflowId){
		Element e = cache.get(workflowId); 
		Transaction tx = (Transaction) e.getObjectValue();
		if (tx.getState() == State.STARTED){
			ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
	        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(tx.getTaskToken());
	        manualCompletionClient.complete(Action.TX_CANCELED);
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/check/{cn}/{workflowId}/{locale}")
	public Response check(
			@PathParam("cn") String cn,
			@PathParam("workflowId") String workflowId,
			@PathParam("locale") String locale,
			@FormParam("Digits") String digits){
		com._37coins.plivo.Response rv =null;
		String dn = "cn="+cn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn;
		try {
			AuthenticationToken at = new UsernamePasswordToken(dn, digits);
			jlc.getLdapContext(at.getPrincipal(),at.getCredentials());
			Element e = cache.get(workflowId);
			Transaction tx = (Transaction) e.getObjectValue();
			tx.setState(State.CONFIRMED);
			cache.put(e);
		    ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
		    ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(tx.getTaskToken());		 
			manualCompletionClient.complete(Action.WITHDRAWAL_REQ);
			rv = new com._37coins.plivo.Response().add(new Speak().setText(msgFactory.getText("VoiceOk",new DataSet().setLocaleString(locale))));
		} catch (AuthenticationException ae){
			
			//check if blocked
			boolean pwLocked = true;
			try{
				InitialLdapContext ctx = null;
				AuthenticationToken at = new UsernamePasswordToken(MessagingServletConfig.ldapUser, MessagingServletConfig.ldapPw);
				ctx = (InitialLdapContext)jlc.getLdapContext(at.getPrincipal(),at.getCredentials());
				String sanitizedCn = BasicAccessAuthFilter.escapeDN(cn);
				Attributes atts = ctx.getAttributes("cn="+sanitizedCn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn,new String[]{"pwdAccountLockedTime", "cn"});
				pwLocked = (atts.get("pwdAccountLockedTime")!=null)?true:false;
			}catch(Exception ne){
			}
			String callText;
			try{
				if (pwLocked){
					callText = msgFactory.getText("AccountBlocked",new DataSet().setLocaleString(locale));
					rv = new com._37coins.plivo.Response()
					.add(new Speak().setText(callText).setLanguage(locale));
				}else{
					callText = msgFactory.getText("VoiceFail",new DataSet().setLocaleString(locale));
					rv = new com._37coins.plivo.Response()
					.add(new Speak().setText(callText).setLanguage(locale))
					.add(new Redirect().setText(MessagingServletConfig.basePath+ "/plivo/answer/"+cn+"/"+workflowId+"/"+locale));
				}
			}catch(IOException | TemplateException ex){
				log.error("plivo exception",ex);
				ex.printStackTrace();
				throw new WebApplicationException(ex, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
			}
		} catch (IllegalStateException | NamingException | IOException | TemplateException e) {
			log.error("plivo exception",e);
			e.printStackTrace();
			throw new WebApplicationException(e, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
		try {
			StringWriter sw = new StringWriter();
			marshaller.marshal(rv, sw);
			return Response.ok(sw.toString(), MediaType.APPLICATION_XML).build();
		} catch (JAXBException e) {
			return null;
		}
	}
	
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/create/{cn}/{workflowId}/{locale}")
	public Response create(
			@PathParam("cn") String cn, 
			@PathParam("locale") String locale,
			@PathParam("workflowId") String workflowId, 
			@FormParam("Digits") String digits){
		com._37coins.plivo.Response rv = null;
		try {
			if (digits.length()<NUM_DIGIT-1){
				throw new IOException();
			}
			rv = new com._37coins.plivo.Response()
				.add(new GetDigits()
				.setAction(MessagingServletConfig.basePath+ "/plivo/confirm/"+cn+"/"+workflowId+"/"+locale+"/"+digits)
				.setNumDigits(5)
				.setRedirect(true)
				.setSpeak(new Speak()
					.setText(msgFactory.getText("VoiceConfirm",new DataSet().setLocaleString(locale)))));
		} catch (IOException | TemplateException e) {
			log.error("plivo create exception",e);
			e.printStackTrace();
			throw new WebApplicationException(e, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
		try {
			StringWriter sw = new StringWriter();
			marshaller.marshal(rv, sw);
			return Response.ok(sw.toString(), MediaType.APPLICATION_XML).build();
		} catch (JAXBException e) {
			return null;
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/confirm/{cn}/{workflowId}/{locale}/{prev}")
	public Response confirm(
			@PathParam("cn") String cn, 
			@PathParam("locale") String locale,
			@PathParam("workflowId") String workflowId,
			@PathParam("prev") String prev,
			@FormParam("Digits") String digits){
		com._37coins.plivo.Response rv =null;
        DataSet ds = new DataSet().setLocaleString(locale);
        try{
	        if (digits!=null && prev != null && Integer.parseInt(digits)==Integer.parseInt(prev)){
	        	//set password
	        	Attributes toModify = new BasicAttributes();
	        	toModify.put("userPassword", digits);
	        	ctx.modifyAttributes("cn="+cn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn, DirContext.REPLACE_ATTRIBUTE, toModify);
	        	//continue transaction
				Element e = cache.get(workflowId);
				Transaction tx = (Transaction) e.getObjectValue();
				tx.setState(State.CONFIRMED);
				cache.put(e);
			    ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
			    ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(tx.getTaskToken());		 
				manualCompletionClient.complete(Action.WITHDRAWAL_REQ);
				rv = new com._37coins.plivo.Response().add(new Speak().setText(msgFactory.getText("VoiceSuccess",ds)));        	
	        }else{
	        	throw new NumberFormatException();
	        }
        }catch(NumberFormatException e){
        	try{
	        	cache.remove(workflowId);
				rv = new com._37coins.plivo.Response()
					.add(new Speak().setText(msgFactory.getText("VoiceMismatch",ds)).setLanguage(locale))
					.add(new Redirect().setText(MessagingServletConfig.basePath+ "/plivo/answer/"+cn+"/"+workflowId+"/"+locale));
				log.error("plivo confirm exception",e);
	        	e.printStackTrace();
			} catch (IOException | TemplateException e1) {
				log.error("plivo confirm exception",e1);
				e1.printStackTrace();
				throw new WebApplicationException(e1, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
			}
        } catch (IOException | TemplateException| NamingException e) {
        	log.error("plivo confirm exception",e);
        	e.printStackTrace();
			throw new WebApplicationException(e, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
		try {
			StringWriter sw = new StringWriter();
			marshaller.marshal(rv, sw);
			return Response.ok(sw.toString(), MediaType.APPLICATION_XML).build();
		} catch (JAXBException e) {
			return null;
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/register/{code}/{locale}")
	public Response register(
			@PathParam("code") String code,
			@PathParam("locale") String locale){
		com._37coins.plivo.Response rv = null;
		String spokenCode = "";
		for (char c : code.toCharArray()){
			spokenCode+=c+", ";
		}
		DataSet ds = new DataSet()
			.setLocaleString(locale)
			.setPayload(spokenCode);
		try {
			String text = msgFactory.getText("VoiceRegister",ds);
			rv = new com._37coins.plivo.Response().add(new Speak()
				.setText(text)
				.setLanguage(ds.getLocaleString()));
		} catch (IOException | TemplateException e) {
			log.error("plivo register exception",e);
			e.printStackTrace();
		}
		try {
			StringWriter sw = new StringWriter();
			marshaller.marshal(rv, sw);
			return Response.ok(sw.toString(), MediaType.APPLICATION_XML).build();
		} catch (JAXBException e) {
			return null;
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/merchant/req/{session}/{code}/{locale}")
	public Response merchantReq(
			@PathParam("session") String session,
			@PathParam("code") String code,
			@PathParam("locale") String locale){
		com._37coins.plivo.Response rv = null;
		DataSet ds = new DataSet().setLocaleString(locale);
		try {
			rv = new com._37coins.plivo.Response()
				.add(new Speak().setText(msgFactory.getText("VoiceHello",ds)).setLanguage(locale))
				.add(new GetDigits()
					.setAction(MessagingServletConfig.basePath+"/plivo/merchant/"+session+"/"+code+"/"+locale)
					.setNumDigits(NUM_DIGIT)
					.setRedirect(true)
					.setSpeak(new Speak()
						.setText(msgFactory.getText("VoiceMerchantConfirm",ds)).setLanguage(locale)));
		} catch (IOException | TemplateException e) {
			log.error("plivo answer exception",e);
			e.printStackTrace();
		}
		try {
			StringWriter sw = new StringWriter();
			marshaller.marshal(rv, sw);
			return Response.ok(sw.toString(), MediaType.APPLICATION_XML).build();
		} catch (JAXBException e) {
			return null;
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/merchant/{session}/{code}/{locale}")
	public Response merchant(
			@PathParam("session") String session,
			@PathParam("code") String code,
			@PathParam("locale") String locale,
			@FormParam("Digits") String digits){
		com._37coins.plivo.Response rv =null;
        DataSet ds = new DataSet().setLocaleString(locale);
	        if (digits!=null && Integer.parseInt(digits)==Integer.parseInt(code)){
	        	try {
	        		rv = new com._37coins.plivo.Response().add(new Speak().setText(msgFactory.getText("VoiceSuccess",ds)));
	        	} catch (IOException | TemplateException e) {
	    			log.error("plivo answer exception",e);
	    			e.printStackTrace();
	    		}
	        	
	        	//TODO: indicate success to hangup message
	    		Element e = cache.get("merchant"+session);
	    		MerchantSession ms = (MerchantSession)e.getObjectValue();
	    		String apiToken = UUID.randomUUID().toString();
	    		String apiSecret = UUID.randomUUID().toString();
	    		//update cache
	    		cache.put(new Element("merchant"+session,ms.setApiToken(apiToken).setApiSecret(apiSecret)));
	        	
	        }else{
	        	try {
	        		rv = new com._37coins.plivo.Response()
						.add(new Speak().setText(msgFactory.getText("VoiceMismatch",ds)).setLanguage(locale))
						.add(new Redirect().setText(MessagingServletConfig.basePath+ "/plivo/merchant/req/"+session+"/"+code+"/"+locale));
	    		} catch (IOException | TemplateException e) {
	    			log.error("plivo answer exception",e);
	    			e.printStackTrace();
	    		}
	        }
	        try {
				StringWriter sw = new StringWriter();
				marshaller.marshal(rv, sw);
				return Response.ok(sw.toString(), MediaType.APPLICATION_XML).build();
			} catch (JAXBException e) {
				return null;
			}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/merchant/hangup/{sessionToken}")
	public void merchantHangup(
			@PathParam("sessionToken") String sessionToken){
		Element e = cache.get("merchant"+sessionToken);
		MerchantSession ms = (MerchantSession)e.getObjectValue();
		if (ms.getApiToken()==null){
			server.getRoomOperations(sessionToken+"/"+sessionToken).sendJsonObject(new MerchantSession().setAction("error"));
		}else{
			String apiToken = ms.getApiToken();
			String apiSecret = ms.getApiSecret();
			try {			
				//check if user exists, if not, create
				ctx.setRequestControls(null);
				SearchControls searchControls = new SearchControls();
				searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				searchControls.setTimeLimit(500);
				NamingEnumeration<?> namingEnum = null;
				final String sanitizedMobile = BasicAccessAuthFilter.escapeLDAPSearchFilter(ms.getPhoneNumber().replace("+", ""));
				namingEnum = ctx.search(MessagingServletConfig.ldapBaseDn, "(&(objectClass=person)(mobile="+sanitizedMobile+"))", searchControls);
				if (namingEnum.hasMore()){
					boolean found = false;
					if (ms.getCallAction()==null || ms.getCallAction().equals("get")){
						SearchResult result = (SearchResult) namingEnum.next();
						Attributes atts = result.getAttributes();
						String ldapApiToken = (atts.get("description")!=null)?(String)atts.get("description").get():null;
						String ldapApiSecret = (atts.get("departmentNumber")!=null)?(String)atts.get("departmentNumber").get():null;
						namingEnum.close();
						if (ldapApiSecret!=null){
							found = true;
							apiToken = ldapApiToken;
							apiSecret = ldapApiSecret;
						}
					}
					if (!found||(ms.getCallAction()!=null && ms.getCallAction().equals("reset"))){
						//update ldap record
						Attributes a = new BasicAttributes();
						//some abuses here: description -> token and departementNumber -> secret
						a.put("description",apiToken);
						a.put("departmentNumber",apiSecret);
						ctx.modifyAttributes("cn="+sanitizedMobile+",ou=accounts,"+MessagingServletConfig.ldapBaseDn, DirContext.REPLACE_ATTRIBUTE, a);		
					}
				}else{
					final String parserApiToken = apiToken;
					final String parserApiSecret = apiSecret;
					final String parserSessionToken = sessionToken;
					parserClient.start(ms.getPhoneNumber(), null, Action.SIGNUP.toString(), localPort,
						new ParserAction() {
							@Override
							public void handleResponse(DataSet data) {
								if (null!=data && data.getAction()==Action.SIGNUP){
									nonTxFactory.getClient(data.getAction()+"-"+data.getCn()).executeCommand(data);
								}
								if (null!=data && data.getAction()==Action.DST_ERROR){
									server.getRoomOperations(parserSessionToken+"/"+parserSessionToken).sendJsonObject(new MerchantSession().setAction("error"));
									return;
								}
								//update ldap record
								Attributes a = new BasicAttributes();
								//some abuses here: description -> token and departementNumber -> secret
								a.put("description",parserApiToken);
								a.put("departmentNumber",parserApiSecret);
								try{
									ctx.modifyAttributes("cn="+sanitizedMobile+",ou=accounts,"+MessagingServletConfig.ldapBaseDn, DirContext.REPLACE_ATTRIBUTE, a);
								}catch(NamingException e){
									log.error("ldap exception ",e);
									e.printStackTrace();
								}
							}
							@Override
							public void handleDeposit(DataSet data) {}
							@Override
							public void handleConfirm(DataSet data) {}
							@Override
							public void handleWithdrawal(DataSet data) {}
						});
				}
			} catch (IllegalStateException | NamingException e1) {
				log.error("ldap exception",e1);
				e1.printStackTrace();
				throw new WebApplicationException(e1, Response.Status.INTERNAL_SERVER_ERROR);
			}


			//execute callback
			if (ms.getDelivery().equals("callback")){
				if (!ms.getDeliveryParam().contains("https")){
					server.getRoomOperations(sessionToken+"/"+sessionToken).sendJsonObject(new MerchantSession().setAction("error"));
					return;
				}
				try{
					CloseableHttpClient httpclient = HttpClients.createDefault();
					HttpPost req = new HttpPost(ms.getDeliveryParam());
					String reqValue = new ObjectMapper().writeValueAsString(new MerchantSession().setApiToken(apiToken).setApiSecret(apiSecret));
					StringEntity entity = new StringEntity(reqValue, "UTF-8");
					entity.setContentType("application/json");
					req.setEntity(entity);
					CloseableHttpResponse rsp = httpclient.execute(req);
					if (rsp.getStatusLine().getStatusCode()>=200 && rsp.getStatusLine().getStatusCode()<=300){
						server.getRoomOperations(sessionToken+"/"+sessionToken).sendJsonObject(new MerchantSession().setAction("success"));
					}else{
						throw new RuntimeException("post failed");
					}
				}catch(Exception ex){
					log.error("callback exception",ex);
					ex.printStackTrace();
					server.getRoomOperations(sessionToken+"/"+sessionToken).sendJsonObject(new MerchantSession().setAction("error"));
					return;
				}
			}else{
				server.getRoomOperations(sessionToken+"/"+sessionToken).sendJsonObject(new MerchantSession().setAction("success").setApiToken(apiToken).setApiSecret(apiSecret));
			}
		}
	}
	
}

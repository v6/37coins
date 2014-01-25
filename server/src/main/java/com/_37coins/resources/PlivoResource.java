package com._37coins.resources;

import java.io.IOException;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
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

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.persistence.dto.Transaction;
import com._37coins.persistence.dto.Transaction.State;
import com._37coins.plivo.GetDigits;
import com._37coins.plivo.Redirect;
import com._37coins.plivo.Speak;
import com._37coins.plivo.Wait;
import com._37coins.plivo.XmlCharacterHandler;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClient;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClientFactory;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClientFactoryImpl;
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
	
	@Inject public PlivoResource(
			JndiLdapContextFactory jlc,
			ServletRequest request,
			AmazonSimpleWorkflow swfService,
			MessageFactory msgFactory,
			Cache cache) {
		HttpServletRequest httpReq = (HttpServletRequest)request;
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
		String dn = "cn="+cn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn;
		Object pw = null;
		try{
			Attributes atts = ctx.getAttributes(dn,new String[]{"userPassword"});
			pw = (atts.get("userPassword")!=null)?atts.get("userPassword").get():null;
		}catch(Exception e){
			e.printStackTrace();
			throw new WebApplicationException(e, javax.ws.rs.core.Response.Status.NOT_FOUND);
		}
		if (pw!=null){
			//only check pin
			try {
				rv = new com._37coins.plivo.Response()
					.add(new Speak().setText(msgFactory.getText("VoiceHello",ds)).setLanguage(locale))
					.add(new GetDigits()
						.setAction(MessagingServletConfig.basePath+"/plivo/check/"+cn+"/"+workflowId+"/"+locale)
						.setNumDigits(NUM_DIGIT)
						.setRedirect(true)
						.setSpeak(new Speak()
							.setText(msgFactory.getText("VoiceEnter",ds)).setLanguage(locale)));
			} catch (IOException | TemplateException e) {
				e.printStackTrace();
			}
		}else{
			//create a new pin
			try {
				rv = new com._37coins.plivo.Response()
					.add(new Speak().setText(msgFactory.getText("VoiceHello",ds)+ msgFactory.getText("VoiceSetup",ds)).setLanguage(locale))
					.add(new Wait())
					.add(new GetDigits()
						.setAction(MessagingServletConfig.basePath+ "/plivo/create/"+cn+"/"+workflowId+"/"+locale)
						.setNumDigits(NUM_DIGIT)
						.setRedirect(true)
						.setSpeak(new Speak()
							.setText(msgFactory.getText("VoiceCreate",ds)).setLanguage(locale)));
			} catch (IOException | TemplateException e) {
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
				Attributes atts = ctx.getAttributes("cn="+cn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn,new String[]{"pwdAccountLockedTime", "cn"});
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
				ex.printStackTrace();
				throw new WebApplicationException(ex, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
			}
		} catch (IllegalStateException | NamingException | IOException | TemplateException e) {
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
	        	e.printStackTrace();
			} catch (IOException | TemplateException e1) {
				e1.printStackTrace();
				throw new WebApplicationException(e1, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
			}
        } catch (IOException | TemplateException| NamingException e) {
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
	
}

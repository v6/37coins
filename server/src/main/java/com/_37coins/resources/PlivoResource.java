package com._37coins.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
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

import org.apache.shiro.cache.CacheException;
import org.restnucleus.dao.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.ldap.CryptoUtils;
import com._37coins.parse.ParserClient;
import com._37coins.persistence.dao.Account;
import com._37coins.plivo.GetDigits;
import com._37coins.plivo.Redirect;
import com._37coins.plivo.Speak;
import com._37coins.plivo.Wait;
import com._37coins.plivo.XmlCharacterHandler;
import com._37coins.web.Transaction;
import com._37coins.web.Transaction.State;
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
	
	final private GenericRepository dao;
	final private AmazonSimpleWorkflow swfService;
	final private MessageFactory msgFactory;
	final private Cache cache;
	private Marshaller marshaller;
	
	@Inject public PlivoResource(
			AmazonSimpleWorkflow swfService,
			MessageFactory msgFactory,
			Cache cache,ParserClient parserClient,
			GenericRepository dao) {
		this.dao = dao;
		this.swfService = swfService;
		this.msgFactory = msgFactory;
		this.cache = cache;
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
		Account a = dao.getObjectById(Long.parseLong(cn), Account.class);
		if (a.getPassword()!=null){
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
		Account a = dao.getObjectById(Long.parseLong(cn), Account.class);
		try {
    		if (CryptoUtils.verifySaltedPassword(digits.getBytes(), a.getPassword())){
    		    a.setPinWrongCount(0);
    			Element e = cache.get(workflowId);
    			Transaction tx = (Transaction) e.getObjectValue();
    			tx.setState(State.CONFIRMED);
    			cache.put(e);
    		    ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
    		    ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(tx.getTaskToken());		 
    			manualCompletionClient.complete(Action.WITHDRAWAL_REQ);
    			try{
    			    rv = new com._37coins.plivo.Response().add(new Speak().setText(msgFactory.getText("VoiceOk",new DataSet().setLocaleString(locale))));
                }catch(IOException | TemplateException ex){
                    log.error("plivo exception",ex);
                    ex.printStackTrace();
                    throw new WebApplicationException(ex, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
                }
    		}else{
    		    a.setPinWrongCount(a.getPinWrongCount()+1);
    			String callText;
    			try{
    				if (a.getPinWrongCount()>=3){
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
    		}
		}catch(NoSuchAlgorithmException | UnsupportedEncodingException | IllegalStateException | CacheException | IllegalArgumentException ex){
		    ex.printStackTrace();
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
	            Account a = dao.getObjectById(Long.parseLong(cn), Account.class);
	            a.setPassword(CryptoUtils.getSaltedPassword(digits.getBytes()));
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
        } catch (IOException | TemplateException | NoSuchAlgorithmException e) {
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
}

package com._37coins.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com._37coins.MessagingServletConfig;
import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.envaya.QueueClient;
import com._37coins.parse.ParserAction;
import com._37coins.parse.ParserClient;
import com._37coins.pojo.EnvayaEvent;
import com._37coins.pojo.EnvayaRequest;
import com._37coins.pojo.EnvayaRequest.MessageType;
import com._37coins.pojo.EnvayaRequest.Status;
import com._37coins.pojo.EnvayaResponse;
import com._37coins.web.Transaction;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.WithdrawalWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClient;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClientFactory;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClientFactoryImpl;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.inject.Injector;

import freemarker.template.TemplateException;

/**
 * local service, service, exposed to gateways
 *
 * @author johann
 *
 */

@Path(EnvayaSmsResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class EnvayaSmsResource {
	public final static String PATH = "/envayasms";
	public static Logger log = LoggerFactory.getLogger(EnvayaSmsResource.class);

	private final QueueClient qc;
	private final NonTxWorkflowClientExternalFactoryImpl nonTxFactory;
	private final WithdrawalWorkflowClientExternalFactoryImpl withdrawalFactory;
	private final AmazonSimpleWorkflow swfService;
	private final ParserClient parserClient;
	private final Cache cache;
	private int localPort;
	private final EnvayaRequest req;
	
	@Inject public EnvayaSmsResource(ServletRequest request,
			QueueClient qc,
			Injector i,
			ParserClient parserClient,
			Cache cache,
			NonTxWorkflowClientExternalFactoryImpl nonTxFactory,
			WithdrawalWorkflowClientExternalFactoryImpl withdrawalFactory,
			AmazonSimpleWorkflow swfService) {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		localPort = httpReq.getLocalPort();
		req = (EnvayaRequest)httpReq.getAttribute("er");
		this.qc = qc;
		this.cache = cache;
		this.swfService = swfService;
		this.parserClient = parserClient;
		this.nonTxFactory = nonTxFactory;
		this.withdrawalFactory = withdrawalFactory;
	}
	
	@POST
	@Path("/{cn}/sms/")
	public EnvayaResponse receive(@PathParam("cn") String cn){
		try{
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			switch (req.getAction()) {
				case SEND_STATUS:
                    MDC.put("hostName", cn);
                    MDC.put("mobile", req.getPhoneNumber());
                    MDC.put("event", req.getAction().toString());
                    MDC.put("log", req.getLog());
                    MDC.put("msgId", req.getId());
                    MDC.put("status", req.getStatus().toString());
                    MDC.put("error", req.getError());
                    log.info("send status received");
					MDC.clear();
					if (req.getStatus() == Status.SENT &&! req.getId().contains("SmsResource")){
				        ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
				        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(req.getId());
				        manualCompletionClient.complete(null);
					}
					break;
				case TEST:
					MDC.put("hostName", cn);
					MDC.put("mobile", req.getPhoneNumber());
					MDC.put("event", req.getAction().toString());
					MDC.put("log", req.getLog());
					log.info("test received");
					MDC.clear();
					try{
						PhoneNumber pn = phoneUtil.parse(req.getPhoneNumber(), "ZZ");
						if (!pn.hasCountryCode())
							throw new NumberParseException(NumberParseException.ErrorType.INVALID_COUNTRY_CODE,"");
					}catch(NumberParseException e){
						throw new WebApplicationException("phone not valid",
								javax.ws.rs.core.Response.Status.BAD_REQUEST);
					}
				case AMQP_STARTED:
                    MDC.put("hostName", cn);
                    MDC.put("mobile", req.getPhoneNumber());
                    MDC.put("event", req.getAction().toString());
                    MDC.put("log", req.getLog());
                    MDC.put("consumer_tag", req.getConsumerTag());
                    log.info("amqp started received");
					MDC.clear();
					break;
				case DEVICE_STATUS:
                    MDC.put("hostName", cn);
                    MDC.put("mobile", req.getPhoneNumber());
                    MDC.put("event", req.getAction().toString());
                    MDC.put("log", req.getLog());
                    MDC.put("status", req.getStatus().toString());
                    log.info("device status received");
					MDC.clear();
					break;
                case FORWARD_SEND:
                    MDC.put("hostName", cn);
                    MDC.put("mobile", req.getPhoneNumber());
                    MDC.put("event", req.getAction().toString());
                    MDC.put("log", req.getLog());
                    log.info("forward message "+ req.getMessage()+" send to "+ req.getTo()+" via "+ req.getMessageType() + " at " + req.getTimestamp());
					MDC.clear();
					break;
				case INCOMING:
					if (req.getMessageType() == MessageType.SMS) {
						String from = req.getFrom();
						String gateway = req.getPhoneNumber();
						String message = req.getMessage();
						PhoneNumber pn = phoneUtil.parse(gateway, "ZZ");
						if (pn.getCountryCode()==1){
							from = fixAmerica(from, gateway,message);
							message = fixAmerica(message);
						}
						MDC.put("hostName", cn);
						MDC.put("mobile", from);
						MDC.put("event", req.getAction().toString());
						MDC.put("log", req.getLog());
						MDC.put("message_type", req.getMessageType().toString());
						log.info("incoming message {} received from {} via {} at {}",req.getMessage(),from,req.getMessageType(),req.getTimestamp());
						MDC.clear();
						parserClient.start(from, gateway, cn, message, localPort,
						new ParserAction() {
							@Override
							public void handleWithdrawal(DataSet data) {
								//save the transaction id to db
								Transaction t = new Transaction().setKey(Transaction.generateKey()).setState(Transaction.State.STARTED);
								cache.put(new Element(t.getKey(), t));
								withdrawalFactory.getClient(t.getKey()).executeCommand(data);
							}
							@Override
							public void handleResponse(DataSet data) {
								try {
									if (data.getAction()==Action.SIGNUP){
										nonTxFactory.getClient(data.getAction()+"-"+data.getCn()).executeCommand(data);
									}else{
										qc.send(data, MessagingServletConfig.queueUri,
											(String) data.getTo().getGateway(), "amq.direct",
											"SmsResource" + System.currentTimeMillis());
									}
								} catch (KeyManagementException
										| NoSuchAlgorithmException
										| IOException | TemplateException
										| URISyntaxException e) {
									log.warn("sms response failed", e);
									e.printStackTrace();
								}
							}
							
							@Override
							public void handleDeposit(DataSet data) {
								nonTxFactory.getClient(data.getAction()+"-"+data.getCn()).executeCommand(data);
							}
							
							@Override
							public void handleConfirm(DataSet data) {
								if (data.getAction()==Action.WITHDRAWAL_CONF){
									Element e = cache.get(data.getPayload()+"tt");
									Transaction tx = (Transaction)e.getObjectValue();
							        ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
							        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(tx.getTaskToken());
							        manualCompletionClient.complete(null);
								}
							}
						});
						
					}
				break;
			}
		}catch(Exception e){
			log.warn("envaya call failed", e);
			e.printStackTrace();
		}
		return new EnvayaResponse().setEvents(new ArrayList<EnvayaEvent>());
	}
	
	/*
	 * get the proxied number
	 * 
	 * TODO: fix this crappy code
	 */
	static public String fixAmerica(String from, String gateway, String message){
		if (null!=message 
				&& message.length()> 2 
				&& message.substring(0, 2).equalsIgnoreCase("+1") 
				&& message.contains(" - ")){
			return message.split(" - ")[0];
		}
		return from;
	}
	
	/*
	 * get the cleaned number
	 * 
	 * TODO: fix this crappy code
	 */
	static public String fixAmerica(String message){
		if (message.substring(0, 2).equalsIgnoreCase("+1") &&
				message.contains(" - ")){
			return message.substring(message.indexOf(" - ")+3, message.length());
		}
		return message;
	}

}

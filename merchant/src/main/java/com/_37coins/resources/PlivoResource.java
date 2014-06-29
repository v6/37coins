package com._37coins.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MerchantServletConfig;
import com._37coins.MessageFactory;
import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.parse.ParserAction;
import com._37coins.parse.ParserClient;
import com._37coins.persistence.dao.Account;
import com._37coins.plivo.GetDigits;
import com._37coins.plivo.Redirect;
import com._37coins.plivo.Speak;
import com._37coins.plivo.XmlCharacterHandler;
import com._37coins.web.MerchantSession;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
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
	
	private final MessageFactory msgFactory;
	
	private Marshaller marshaller;
	final private Cache cache;
	final private GenericRepository dao;
	final private ParserClient parserClient;
	final private SocketIOServer server;
	final private NonTxWorkflowClientExternalFactoryImpl nonTxFactory;
	private int localPort;
	
	@Inject 
	public PlivoResource(
			ServletRequest request,
			Cache cache,
			SocketIOServer server,
			GenericRepository dao,
			ParserClient parserClient,
			MessageFactory msgFactory,
			NonTxWorkflowClientExternalFactoryImpl nonTxFactory) {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		localPort = httpReq.getLocalPort();
		this.msgFactory = msgFactory;
		this.cache = cache;
		this.nonTxFactory = nonTxFactory;
		this.server = server;
		this.parserClient = parserClient;
		this.dao = dao;
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
	@Path("/answer/{locale}")
	public Response answer(
			@PathParam("locale") String locale){
		com._37coins.plivo.Response rv = null;
		DataSet ds = new DataSet().setLocaleString(locale);
		try {
			rv = new com._37coins.plivo.Response()
				.add(new Speak().setText(msgFactory.getText("VoiceHello",ds)+" "+msgFactory.getText("VoiceSetup",ds)).setLanguage(locale));
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
	@Path("/hangup")
	public void hangup(){
		System.out.println("done: "+System.currentTimeMillis());
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/ring")
	public void ring(){
		System.out.println("ring: "+System.currentTimeMillis());
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
                    .setAction(MerchantServletConfig.basePath+"/plivo/merchant/"+session+"/"+code+"/"+locale)
                    .setNumDigits(NUM_DIGIT)
                    .setRedirect(true)
                    .setRetries(2)
                    .setSpeak(new Speak()
                        .setText(msgFactory.getText("VoiceMerchantConfirm",ds)).setLanguage(locale)))
                .add(new Speak().setText("no dial tones received.").setLanguage(locale));
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
                        .add(new Redirect().setText(MerchantServletConfig.basePath+ "/plivo/merchant/req/"+session+"/"+code+"/"+locale));
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
        String displayName = null;
        if (ms.getApiToken()==null){
            server.getRoomOperations(sessionToken+"/"+sessionToken).sendJsonObject(new MerchantSession().setAction("error"));
            return;
        }else{
            String apiToken = ms.getApiToken();
            String apiSecret = ms.getApiSecret();
            final String cn = ms.getPhoneNumber().replace("+", "");
            try {           
                //check if user exists, if not, create
                
                RNQuery q = new RNQuery().addFilter("mobile", ms.getPhoneNumber());
                Account a = dao.queryEntity(q, Account.class, false);
                if (null!=a){
                    boolean found = false;
                    if (ms.getCallAction()==null || ms.getCallAction().equals("get")){
                        if (a.getApiToken()!=null){
                            found = true;
                            apiToken = a.getApiToken();
                            apiSecret = a.getApiSecret();
                        }
                    }
                    if (!found||(ms.getCallAction()!=null && ms.getCallAction().equals("reset"))){
                        //update ldap record
                        a.setApiToken(apiToken);
                        a.setApiSecret(apiSecret);
                    }
                }else{
                    final String parserApiToken = apiToken;
                    final String parserApiSecret = apiSecret;
                    final String parserSessionToken = sessionToken;
                    parserClient.start(ms.getPhoneNumber(), null, null, Action.SIGNUP.toString(), localPort,
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
                                RNQuery q = new RNQuery().addFilter("mobile", "+"+cn);
                                Account a = dao.queryEntity(q, Account.class);
                                a.setApiToken(parserApiToken);
                                a.setApiSecret(parserApiSecret);
                            }
                            @Override
                            public void handleDeposit(DataSet data) {}
                            @Override
                            public void handleConfirm(DataSet data) {}
                            @Override
                            public void handleWithdrawal(DataSet data) {}
                        });
                }
            } catch (IllegalStateException e1) {
                log.error("ldap exception",e1);
                e1.printStackTrace();
                throw new WebApplicationException(e1, Response.Status.INTERNAL_SERVER_ERROR);
            }

            if (null==displayName){
                displayName = "merchant"+apiToken.split("-")[0];
            }
            ms.setDisplayName(displayName);
            cache.put(new Element("merchant"+sessionToken,ms));
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
                        server.getRoomOperations(sessionToken+"/"+sessionToken).sendJsonObject(new MerchantSession()
                            .setDisplayName(ms.getDisplayName())
                            .setAction("success"));
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
                server.getRoomOperations(sessionToken+"/"+sessionToken).sendJsonObject(
                        new MerchantSession()
                            .setAction("success")
                            .setApiToken(apiToken)
                            .setApiSecret(apiSecret)
                            .setDelivery(ms.getDelivery())
                            .setDisplayName(ms.getDisplayName())
                            .setDeliveryParam(ms.getDeliveryParam()));
            }
        }
    }

}

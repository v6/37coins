package com._37coins;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.jdo.PersistenceManagerFactory;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

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
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.persistence.dao.Gateway;
import com._37coins.sendMail.MailServiceClient;
import com._37coins.web.GatewayUser;
import com._37coins.web.Queue;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import freemarker.template.TemplateException;

public class ServiceLevelThread extends Thread {
	public static Logger log = LoggerFactory.getLogger(ServiceLevelThread.class);
	final private Cache cache;
	private final MailServiceClient mailClient;
	private final MessageFactory msgFactory;
	private final GenericRepository dao;
	private final ObjectMapper objectMapper;
	private final CredentialsProvider credsProvider;
	private final HttpClient client;

	boolean isActive = true;
	Map<String,GatewayUser> buffer = new HashMap<>();
	Map<String,GatewayUser> activeBefore = new HashMap<>();
	Map<String,GatewayUser> activeNow = new HashMap<>();

	@Inject
	public ServiceLevelThread(Cache cache,
			MailServiceClient mailClient,
			MessageFactory msgFactory,
			PersistenceManagerFactory pmf)
			throws IllegalStateException {
		this.cache = cache;
        this.credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(
                MerchantServletConfig.amqpHost, 15672),
                new UsernamePasswordCredentials(
                        MerchantServletConfig.amqpUser,
                        MerchantServletConfig.amqpPassword));
        this.client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credsProvider).build();
		this.dao = new GenericRepository(pmf);
		this.objectMapper = new ObjectMapper();
		this.mailClient = mailClient;
		this.msgFactory = msgFactory;
	}

	@Override
	public void run() {
		while (isActive) {
		    Map<String,GatewayUser> rv = new HashMap<>();
		    try{
    			System.err.println("started new pull");
    		    List<Gateway> gateways = dao.queryList(new RNQuery().setRange(0L,200L), Gateway.class);
    		    System.err.println("fetched " + gateways.size() + " entries");
    			for (Gateway g: gateways){
    				if (null != g.getMobile() && null != g.getSettings() && null != g.getSettings().getFee()) {
    					PhoneNumberUtil phoneUtil = PhoneNumberUtil
    							.getInstance();
    					PhoneNumber pn = null;
    			        try {
    			            pn = phoneUtil.parse(g.getMobile(), "ZZ");
    			        } catch (NumberParseException ex) {
    		                log.error("ldap connection failed", ex);
    		                continue;
    		            }
    					String cc = phoneUtil.getRegionCodeForNumber(pn);
    					GatewayUser gu = new GatewayUser()
    							.setMobile(
    									PhoneNumberUtil.getInstance().format(
    											pn, PhoneNumberFormat.E164))
    							.setFee(g.getSettings().getFee())
    							.setEnvayaToken(g.getEmail())
    							.setLocale(new Builder().setRegion(cc).build())
    							.setId(g.getCn());
    					rv.put(gu.getId(), gu);
    				}
    			}
	         }finally{
	                dao.closePersistenceManager();
	         }
		     try{
    			System.err.println("starting amqp");
    			Map<String,GatewayUser> active = new HashMap<String,GatewayUser>();
    			for (Entry<String,GatewayUser> gu : rv.entrySet()) {
    			    HttpGet httpGet = new HttpGet("http://"
                            + MerchantServletConfig.amqpHost
                            + ":15672/api/queues/%2f/" + gu.getKey());
    				try {
    					URI uri = new URIBuilder(httpGet.getURI()).build();
    					HttpRequestBase request = new HttpGet(uri);
    					System.err.println("before client");
    					HttpResponse response = client.execute(request);
    					if (objectMapper.readValue(response.getEntity().getContent(), Queue.class)
    							.getConsumers() > 0) {
    						MDC.put("hostName", gu.getKey());
    						MDC.put("mobile", gu.getValue().getMobile());
    						MDC.put("event", "check");
    						MDC.put("Online", "true");
    						log.info("{} online", gu.getKey());
    						MDC.clear();
    						active.put(gu.getKey(),gu.getValue());
    					} else {
    						MDC.put("hostName", gu.getKey());
    						MDC.put("mobile", gu.getValue().getMobile());
    						MDC.put("event", "check");
    						MDC.put("Online", "false");
    						log.info("{} offline", gu.getKey());
    						MDC.clear();
    					}
    				} catch (Exception ex) {
    					log.error("AMQP connection failed", ex);
    				}finally{
    				    httpGet.releaseConnection();
    				}
    			}
    			System.err.println("before cache");
    			cache.put(new Element("gateways", active));
    			runAlerts(active);
		    }catch(Exception e){
		        log.error("service level failed", e);
		        e.printStackTrace();
		    }
			try {
				Thread.sleep(59000L);
			} catch (InterruptedException e) {
				log.error("gateway statistics stopping");
				isActive = false;
			}
		}
	}

	private void runAlerts(Map<String,GatewayUser> activeNow){
		if (activeBefore.size()==0){
			activeBefore.putAll(activeNow);
			return;
		}else{
			MapDifference<String, GatewayUser> dif = Maps.difference(activeBefore, activeNow);
			//handle reconnected
			Map<String, GatewayUser> connected = dif.entriesOnlyOnRight();
			for (Entry<String, GatewayUser> e: connected.entrySet())
				buffer.remove(e.getKey());
			//check buffer for 2nd time offline
			for (Entry<String, GatewayUser> e: buffer.entrySet()){
				String email = e.getValue().getEnvayaToken();
				try {
					sendAlertEmail(email);
				} catch (MessagingException | IOException | TemplateException e1) {
					log.error("send email failed",e1);
					e1.printStackTrace();
				}
			}
			buffer.clear();
			//handle dropped
			Map<String, GatewayUser> dropped = dif.entriesOnlyOnLeft();
			for (Entry<String, GatewayUser> e: dropped.entrySet())
				buffer.put(e.getKey(),e.getValue());
			//make new old
			activeBefore.clear();
			activeBefore.putAll(activeNow);
		}
	}

	private void sendAlertEmail(String email) throws AddressException, MessagingException, IOException, TemplateException{
		DataSet ds = new DataSet()
			.setLocale(Locale.ENGLISH)
			.setAction(Action.GW_ALERT);
		mailClient.send(
			msgFactory.constructSubject(ds),
			email,
			MerchantServletConfig.senderMail,
			msgFactory.constructTxt(ds),
			msgFactory.constructHtml(ds));
	}

	public void kill() {
		isActive = false;
		dao.closePersistenceManager();
		this.interrupt();
	}

}

package com._37coins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;

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

import com._37coins.activities.MessagingActivities;
import com._37coins.envaya.QueueClient;
import com._37coins.persistence.dto.MsgAddress;
import com._37coins.persistence.dto.Transaction;
import com._37coins.persistence.dto.Transaction.State;
import com._37coins.resources.EmailServiceResource;
import com._37coins.sendMail.MailTransporter;
import com._37coins.util.FiatPriceProvider;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.EmailFactor;
import com._37coins.workflow.pojo.MessageAddress;
import com._37coins.workflow.pojo.MessageAddress.MsgType;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.Withdrawal;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContext;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClient;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClientFactory;
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClientFactoryImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.ManualActivityCompletion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.call.Call;
import com.plivo.helper.exception.PlivoException;

public class MessagingActivitiesImpl implements MessagingActivities {
	public static Logger log = LoggerFactory.getLogger(MessagingActivitiesImpl.class);
	ActivityExecutionContextProvider contextProvider = new ActivityExecutionContextProviderImpl();
	
	@Inject
	MailTransporter mt;
	
	@Inject
	QueueClient qc;
	
	@Inject
	JndiLdapContextFactory jlc;
	
	@Inject
	AmazonSimpleWorkflow swfService;
	
	@Inject
	Cache cache;
	
	@Inject
	MessageFactory mf;

	@Override
	@ManualActivityCompletion
	public void sendMessage(DataSet rsp) {
		ActivityExecutionContext executionContext = contextProvider.getActivityExecutionContext();
		String taskToken = executionContext.getTaskToken();
		try {
			rsp.setFiatPriceProvider(new FiatPriceProvider(cache));
			if (rsp.getTo().getAddressType() == MsgType.EMAIL){
				mt.sendMessage(rsp);
		        ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
		        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(taskToken);
		        manualCompletionClient.complete(null);
			}else{
				qc.send(rsp,MessagingServletConfig.queueUri, rsp.getTo().getGateway(),"amq.direct",taskToken);
			}
		} catch (Exception e) {
			log.error("messaging activity exception",e);
			e.printStackTrace();
			ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
	        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(taskToken);
	        manualCompletionClient.fail(e);
			return;
		}
	}

	@Override
	public void putCache(DataSet rsp) {
		cache.put(new Element("balance"+rsp.getCn(), ((Withdrawal)rsp.getPayload()).getBalance()));
	}
	
	@Override
	public void putAddressCache(DataSet rsp) {
		cache.put(new Element("address"+rsp.getCn(), ((PaymentAddress)rsp.getPayload()).getAddress()));
	}

	@Override
	@ManualActivityCompletion
	public Action sendConfirmation(DataSet rsp, String workflowId) {
		ActivityExecutionContext executionContext = contextProvider.getActivityExecutionContext();
		String taskToken = executionContext.getTaskToken();
		try{
			Element e = cache.get(workflowId);
			Transaction tt = (Transaction)e.getObjectValue();
			tt.setTaskToken(taskToken);
			String confLink = MessagingServletConfig.basePath + "/rest/withdrawal/approve?key="+URLEncoder.encode(tt.getKey(),"UTF-8");
			Withdrawal w = (Withdrawal)rsp.getPayload();
			w.setConfKey(tt.getKey());
			w.setConfLink(confLink);
			rsp.setFiatPriceProvider(new FiatPriceProvider(cache));
			if (rsp.getTo().getAddressType() == MsgType.EMAIL){
		        ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
		        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(taskToken);
		        manualCompletionClient.complete(null);
			}else{
				qc.send(rsp,MessagingServletConfig.queueUri, rsp.getTo().getGateway(),"amq.direct","SmsResource"+taskToken);
			}
		} catch (Exception e) {
			ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
	        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(taskToken);
	        manualCompletionClient.fail(e);
	        log.error("send confirmation exception",e);
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	@ManualActivityCompletion
	public Action phoneConfirmation(DataSet rsp, String workflowId) {
		ActivityExecutionContext executionContext = contextProvider.getActivityExecutionContext();
		String taskToken = executionContext.getTaskToken();
		String sanitizedCn = BasicAccessAuthFilter.escapeDN(rsp.getCn());
		try{
			Transaction tt = new Transaction();
			tt.setTaskToken(taskToken);
			tt.setState(State.STARTED);
			cache.put(new Element(workflowId,tt));
			
			InitialLdapContext ctx = null;
			AuthenticationToken at = new UsernamePasswordToken(MessagingServletConfig.ldapUser, MessagingServletConfig.ldapPw);
			ctx = (InitialLdapContext)jlc.getLdapContext(at.getPrincipal(),at.getCredentials());
			Attributes atts = ctx.getAttributes("cn="+sanitizedCn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn,new String[]{"pwdAccountLockedTime", "cn"});
			boolean pwLocked = (atts.get("pwdAccountLockedTime")!=null)?true:false;
			
			if (!pwLocked){
				RestAPI restAPI = new RestAPI(MessagingServletConfig.plivoKey, MessagingServletConfig.plivoSecret, "v1");
	
				LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
			    params.put("from", "+4971150888362");
			    params.put("to", rsp.getTo().getAddress());
			    params.put("answer_url", MessagingServletConfig.basePath + "/plivo/answer/"+sanitizedCn+"/"+workflowId+"/"+mf.getLocale(rsp).toString());
			    params.put("hangup_url", MessagingServletConfig.basePath + "/plivo/hangup/"+workflowId);
			    params.put("caller_name", "37 Coins");
			    Call response = restAPI.makeCall(params);
			    if (response.serverCode != 200 && response.serverCode != 201 && response.serverCode !=204){
			    	throw new PlivoException(response.message);
			    }
			}else{
		        ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
		        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(taskToken);
		        manualCompletionClient.complete(Action.ACCOUNT_BLOCKED);				
			}
		    return null;
		} catch (PlivoException | NamingException | MalformedURLException e) {
	        ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
	        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(taskToken);
	        manualCompletionClient.complete(Action.TX_CANCELED);
	        log.error("phone confirmation exception",e);
	        e.printStackTrace();
	        return null;
		}
	}
	


	@Override
	public DataSet readMessageAddress(DataSet data) {
		InitialLdapContext ctx = null;
		AuthenticationToken at = new UsernamePasswordToken(MessagingServletConfig.ldapUser, MessagingServletConfig.ldapPw);
		try {
			ctx = (InitialLdapContext)jlc.getLdapContext(at.getPrincipal(),at.getCredentials());
		} catch (IllegalStateException | NamingException e) {
			log.error("read message address exception",e);
			e.printStackTrace();
		}
		try{
			String sanitizedCn = BasicAccessAuthFilter.escapeDN(data.getCn());
			Attributes atts = ctx.getAttributes("cn="+sanitizedCn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn,new String[]{"mobile", "manager","preferredLanguage"});
			String locale = (atts.get("preferredLanguage")!=null)?(String)atts.get("preferredLanguage").get():null;
			String gwDn = (atts.get("manager")!=null)?(String)atts.get("manager").get():null;
			String mobile = (atts.get("mobile")!=null)?(String)atts.get("mobile").get():null;
			MessageAddress to =  new MessageAddress()
				.setAddress(mobile)
				.setAddressType(MsgType.SMS)
				.setGateway(gwDn.substring(3, gwDn.indexOf(",")));
			return data.setTo(to)
				.setLocaleString(locale)
				.setService("37coins");
		}catch(NamingException e){
			return null;
		}
	}

	public MsgAddress pickMsgAddress(Set<MsgAddress> list){
		//TODO: get a strategy here
		return list.iterator().next();
	}

	
	@Override
	@ManualActivityCompletion
    public String emailVerification(EmailFactor ef, Locale locale){
		ActivityExecutionContext executionContext = contextProvider.getActivityExecutionContext();
		String taskToken = executionContext.getTaskToken();
		try{
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost req = new HttpPost("http://127.0.0.1:"+MessagingServletConfig.localPort+EmailServiceResource.PATH+"/verify");
			if (null!=locale){
				req.addHeader("Accept-Language", locale.toString().replace("_", "-"));
			}
			StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(ef), "UTF-8");
			entity.setContentType("application/json");
			req.setEntity(entity);
			CloseableHttpResponse rsp = httpclient.execute(req);
			if (rsp.getStatusLine().getStatusCode()==200){
				EmailFactor c = new ObjectMapper().readValue(rsp.getEntity().getContent(),EmailFactor.class);
				cache.put(new Element("emailVer"+ef.getSmsToken()+ef.getEmailToken(),new EmailFactor().setEmailToken(c.getEmailToken()).setTaskToken(taskToken)));
			}else{
				throw new IOException("return code: "+rsp.getStatusLine().getStatusCode());
			}
		}catch(Exception ex){
			log.error("email verification exepction",ex);
			ex.printStackTrace();
			ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
	        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(taskToken);
	        manualCompletionClient.fail(ex);
		}
		return null;
	}
    
	@Override
    public void emailConfirmation(String emailServiceToken, Locale locale){
		try{
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost req = new HttpPost("http://127.0.0.1:"+MessagingServletConfig.localPort+EmailServiceResource.PATH+"/confirm");
			if (null!=locale){
				req.addHeader("Accept-Language", locale.toString().replace("_", "-"));
			}
			StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(new EmailFactor().setEmailToken(emailServiceToken)), "UTF-8");
			entity.setContentType("application/json");
			req.setEntity(entity);
			CloseableHttpResponse rsp = httpclient.execute(req);
			if (rsp.getStatusLine().getStatusCode()!=204){
				throw new IOException("return code: "+rsp.getStatusLine().getStatusCode());
			}
		}catch(Exception ex){
			log.error("email confirmation exception",ex);
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
    
	@Override
    public void emailOtpCreation(String cn, String email, Locale locale){
		try{
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost req = new HttpPost("http://127.0.0.1:"+MessagingServletConfig.localPort+EmailServiceResource.PATH+"/renew");
			if (null!=locale){
				req.addHeader("Accept-Language", locale.toString().replace("_", "-"));
			}
			StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(new EmailFactor().setCn(cn).setEmail(email)), "UTF-8");
			entity.setContentType("application/json");
			req.setEntity(entity);
			CloseableHttpResponse rsp = httpclient.execute(req);
			if (rsp.getStatusLine().getStatusCode()!=204){
				throw new IOException("return code: "+rsp.getStatusLine().getStatusCode());
			}
		}catch(Exception ex){
			log.error("email otp exception",ex);
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}		
	}
	
	@Override
	public Action otpConfirmation(String cn, String otp, Locale locale){
		try{
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost req = new HttpPost("http://127.0.0.1:"+MessagingServletConfig.localPort+EmailServiceResource.PATH+"/consume");
			if (null!=locale){
				req.addHeader("Accept-Language", locale.toString().replace("_", "-"));
			}
			StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(new EmailFactor().setCn(cn).setTaskToken(otp)), "UTF-8");
			entity.setContentType("application/json");
			req.setEntity(entity);
			CloseableHttpResponse rsp = httpclient.execute(req);
			if (rsp.getStatusLine().getStatusCode()==204){
				return Action.WITHDRAWAL_REQ;
			}else{
				throw new IOException("return code: "+rsp.getStatusLine().getStatusCode());
			}
		}catch(Exception ex){
			log.error("otp confirmation exception",ex);
			ex.printStackTrace();
			return Action.TX_FAILED;
		}	
	}

}

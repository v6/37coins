package com._37coins;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.jdo.JDOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.joda.money.CurrencyUnit;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.activities.MessagingActivities;
import com._37coins.envaya.QueueClient;
import com._37coins.persistence.dao.Account;
import com._37coins.sendMail.MailTransporter;
import com._37coins.util.FiatPriceProvider;
import com._37coins.web.Transaction;
import com._37coins.web.Transaction.State;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
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
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
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
	AmazonSimpleWorkflow swfService;
	
	@Inject
	Cache cache;
	
	@Inject
	MessageFactory mf;
	
	@Inject
	GenericRepository dao;

	@Override
	@ManualActivityCompletion
	public void sendMessage(DataSet rsp) {
        if (rsp.getLocale()!=null && (rsp.getLocale().getCountry()==null||rsp.getLocale().getCountry()=="")){
            rsp.setLocale(new Locale(rsp.getLocale().getLanguage(),"US"));
        }
		ActivityExecutionContext executionContext = contextProvider.getActivityExecutionContext();
		String taskToken = executionContext.getTaskToken();
		try {
			if (rsp.getPayload() instanceof Withdrawal){
				Withdrawal w = (Withdrawal)rsp.getPayload();
				if (w.getRate()!=null && w.getCurrencyCode()!=null){
					rsp.setFiatPriceProvider(new FiatPriceProvider(new BigDecimal(w.getRate()),CurrencyUnit.of(w.getCurrencyCode())));
				}
			}
			if (rsp.getFiatPriceProvider()==null){
				rsp.setFiatPriceProvider(new FiatPriceProvider(cache));
			}
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
			if (w.getRate()!=null && w.getCurrencyCode()!=null){
				rsp.setFiatPriceProvider(new FiatPriceProvider(new BigDecimal(w.getRate()),CurrencyUnit.of(w.getCurrencyCode())));
			}else{
				rsp.setFiatPriceProvider(new FiatPriceProvider(cache));
			}
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
		try{
			Transaction tt = new Transaction();
			tt.setTaskToken(taskToken);
			tt.setState(State.STARTED);
			cache.put(new Element(workflowId,tt));
			Account a = dao.getObjectById(Long.parseLong(rsp.getCn()), Account.class);
			if (null!=a.getPinWrongCount()&& a.getPinWrongCount()<3){
				RestAPI restAPI = new RestAPI(MessagingServletConfig.plivoKey, MessagingServletConfig.plivoSecret, "v1");
	
				LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
				PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
				String regionCode = phoneUtil.getRegionCodeForNumber(rsp.getTo().getPhoneNumber());
				String from = PhoneNumberUtil.getInstance().format(phoneUtil.getExampleNumberForType(regionCode, PhoneNumberType.MOBILE), PhoneNumberFormat.E164);
			    params.put("from", from.substring(0,from.length()-4)+"3737");
			    params.put("to", rsp.getTo().getAddress());
			    params.put("answer_url", MessagingServletConfig.basePath + "/plivo/answer/"+rsp.getCn()+"/"+workflowId+"/"+mf.getLocale(rsp).toString());
			    params.put("hangup_url", MessagingServletConfig.basePath + "/plivo/hangup/"+workflowId);
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
		} catch (PlivoException | MalformedURLException e) {
	        ManualActivityCompletionClientFactory manualCompletionClientFactory = new ManualActivityCompletionClientFactoryImpl(swfService);
	        ManualActivityCompletionClient manualCompletionClient = manualCompletionClientFactory.getClient(taskToken);
	        manualCompletionClient.complete(Action.TX_CANCELED);
	        log.error("phone confirmation exception",e);
	        e.printStackTrace();
	        return null;
		}
	}
	
	@Override
	public BigDecimal readAccountFee(String mobile) {
	    mobile = (mobile.contains("+"))?mobile:"+"+mobile;
	    RNQuery q = new RNQuery().addFilter("mobile", mobile);
		Account a = dao.queryEntity(q, Account.class);
		BigDecimal fee = (a.getOwner().getFee()!=null)?a.getOwner().getFee():BigDecimal.ZERO;
		return fee;
	}
	
	@Override
	public BigDecimal readRate(String curCode, BigDecimal amountBtc) {
		FiatPriceProvider fpp = new FiatPriceProvider(cache);
		return fpp.getLocalCurValue(amountBtc, CurrencyUnit.of(curCode)).getLast();
	}

	@Override
	public DataSet readMessageAddress(DataSet data) {
	    try{
	        String mobile = data.getCn();
	        mobile = (mobile.contains("+"))?mobile:"+"+mobile;
	        RNQuery q = new RNQuery().addFilter("mobile", mobile);
	        Account a = dao.queryEntity(q, Account.class);
			MessageAddress to =  new MessageAddress()
				.setAddress(a.getMobile())
				.setAddressType(MsgType.SMS)
				.setGateway(a.getOwner().getCn());
			return data.setTo(to)
				.setLocale(a.getLocale())
				.setService("37coins");
		}catch(JDOException e){
			return null;
		}
	}
}

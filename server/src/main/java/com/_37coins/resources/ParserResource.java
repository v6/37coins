package com._37coins.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;

import javax.inject.Inject;
import javax.jdo.JDOException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.money.CurrencyUnit;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.merchant.MerchantClient;
import com._37coins.merchant.MerchantClientException;
import com._37coins.merchant.pojo.Charge;
import com._37coins.merchant.pojo.PaymentDestination.AddressType;
import com._37coins.persistence.dao.Account;
import com._37coins.persistence.dao.Gateway;
import com._37coins.util.FiatPriceProvider;
import com._37coins.util.GatewayPriceComparator;
import com._37coins.web.GatewayUser;
import com._37coins.web.Seller;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.MessageAddress;
import com._37coins.workflow.pojo.MessageAddress.MsgType;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Signup;
import com._37coins.workflow.pojo.Withdrawal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.call.Call;
import com.plivo.helper.exception.PlivoException;

@Path(ParserResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ParserResource {
	public final static String PATH = "/parser";
	public static Logger log = LoggerFactory.getLogger(ParserResource.class);
	final private List<DataSet> responseList;
	private final GenericRepository dao;
	final private ObjectMapper mapper;
	final private Cache cache;
	final private FiatPriceProvider fiatPriceProvider;
	final private MessageFactory mf;
	final private MerchantClient merchantClient;
	private int localPort;
	
	@SuppressWarnings("unchecked")
	@Inject public ParserResource(@Context HttpServletRequest request,
			Cache cache, FiatPriceProvider fiatPriceProvider,
			MessageFactory mf,MerchantClient merchantClient, GenericRepository dao) {
		this.cache = cache;
		responseList = (List<DataSet>)request.getAttribute("dsl");
		DataSet ds = (DataSet)request.getAttribute("create");
		if (null!=ds)
			responseList.add(ds);
		this.dao = dao;
		this.fiatPriceProvider = fiatPriceProvider;
		this.mf = mf;
		this.merchantClient = merchantClient;
		localPort = request.getLocalPort();
		MessagingServletConfig.localPort = localPort;
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
	}
	
	@POST
	@Path("/Balance")
	public Response balance(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	@POST
	@Path("/Transactions")
	public Response transactions(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	@POST
	@Path("/DepositReq")
	public Response depositReq(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	@POST
	@Path("/Help")
	public Response help(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
    @POST
    @Path("/HelpSend")
    public Response helpSend(){
        try {
            return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
        } catch (JsonProcessingException e) {
            return null;
        }
    }
	
	@POST
	@Path("/Signup")
	public Response signup(){
		DataSet data = responseList.get(0);
		responseList.clear();
		Map<String,String> rv = signup(data.getTo(), null, data.getGwCn(), data.getLocaleString(), data.getService());
		try {
			if (null==rv){
				responseList.clear();
				responseList.add(new DataSet().setTo(data.getTo()).setLocale(data.getLocale()).setAction(Action.DST_ERROR));
				return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
			}
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
		    e.printStackTrace();
		    log.error("json process exception",e);
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,String> signup(MessageAddress recipient, MessageAddress referrer, String gwCn, String locale, String service){
		Gateway gwDn = null;
		String gwAddress = null;
		Locale gwLng = null;
		String cnString = null;
		String mobile = null;
		if (recipient.getAddressType()==MsgType.SMS){//create a new user
            if (null == referrer && null != gwCn && gwCn.length() > 0){
                RNQuery q = new RNQuery().addFilter("cn", gwCn);
                Gateway tmp = dao.queryEntity(q, Gateway.class);
                try {
                  referrer = MessageAddress.fromString(tmp.getMobile(), (String)null);
                  referrer.setGateway(gwCn);
                } catch (AddressException | NumberParseException e) {
                    log.error("invite exception",e);
                }
            }
			//set gateway from referring user's gateway
			if (null != referrer && referrer.getAddressType() == MsgType.SMS 
					&& recipient.getPhoneNumber().getCountryCode() == referrer.getPhoneNumber().getCountryCode()){
			    RNQuery q = new RNQuery().addFilter("cn", gwCn);
			    gwDn = dao.queryEntity(q, Gateway.class);
				gwAddress = referrer.getGateway();
			}else{//or try to find a gateway in the database
				try{
					RNQuery q = new RNQuery().addFilter("countryCode", recipient.getPhoneNumber().getCountryCode());
					List<Gateway> qResultsFixed = dao.queryList(q.setRange(0L,200L), Gateway.class);
					List<Gateway> qResults = new ArrayList<>(qResultsFixed);
					Collections.sort(qResults, new GatewayPriceComparator());
					Element gws = cache.get("gateways");
					if (null!=gws && !gws.isExpired()){
						Map<String,GatewayUser> gateways = (Map<String,GatewayUser>)gws.getObjectValue();
						for (Gateway g: qResults){
							for (GatewayUser gu: gateways.values()){
								if (gu.getId().equals(g.getCn())){
									gwLng = g.getLocale();
									gwDn = g;
									gwAddress = g.getCn();
									break;
								}
							}
							if (null!=gwDn) break;
						}
					}
					if (null==gwDn){
						return null;
					}
				}catch (Exception e1){
					log.error("signup exception",e1);
					e1.printStackTrace();
					throw new WebApplicationException(e1, Response.Status.INTERNAL_SERVER_ERROR);
				}
			}
		}else{
			throw new RuntimeException("not implemented");
		}
		if (null!=gwDn){
			//the locale is either passed in from the website, the inviting user, or the taken from the gateway
	        Locale uLocale = (null==gwLng)?DataSet.parseLocaleString(locale):gwLng;
		//create new user
		    Account newUser = new Account()
		        .setOwner(gwDn)
		        .setMobile(recipient.getAddress())
		        .setLocale(uLocale);
			try {
			    dao.add(newUser);
			    cnString = newUser.getId().toString();
			    mobile = newUser.getMobile();
				//and say hi to new user
				DataSet create = new DataSet()
					.setAction(Action.SIGNUP)
					.setTo(new MessageAddress()
						.setAddress(recipient.getAddressObject())
						.setAddressType(recipient.getAddressType())
						.setGateway(gwAddress))
					.setCn(newUser.getId().toString())
					.setPayload(new Signup()
					        .setReferrer((null!=referrer)?referrer.getAddress():null)
					        .setMobile(recipient.getAddress())
					        .setSource((null!=referrer)?Signup.Source.MOVE:Signup.Source.NEW)
					        .setSignupCallback(gwDn.getSettings().getSignupCallback())
					        .setWelcomeMessage(gwDn.getSettings().getWelcomeMsg())
					        .setDigestToken(gwDn.getApiSecret()))
					.setLocale(uLocale)
					.setService(service);
				responseList.add(create);
			} catch (JDOException e1) {
				log.error("signup exception",e1);
				e1.printStackTrace();
				throw new WebApplicationException(e1, Response.Status.INTERNAL_SERVER_ERROR);
			}
		}
		Map<String,String> rv = new HashMap<>();
		rv.put("gwAddress",gwAddress);
		rv.put("cn",cnString);
		rv.put("mobile", mobile);
		return rv;
	}
	
	@POST
	@Path("/WithdrawalReq")
	public Response withdrawalReq(){
		DataSet data = responseList.get(0);
		Withdrawal w = (Withdrawal)data.getPayload();
		if (null!= w.getMsgDest() && w.getMsgDest().getAddress()!=null){
			Map<String,String> newGw = null;
			String cn = null;
			String gwAddress = null;
		    RNQuery q = new RNQuery().addFilter("mobile", w.getMsgDest().getAddress());
		    Account a = dao.queryEntity(q, Account.class, false);
		    if (null!=a){
		        cn = a.getMobile();
		    }else{
				newGw = signup(w.getMsgDest(), data.getTo(), data.getGwCn(), data.getLocaleString(), data.getService());
				if (null==newGw){
					responseList.clear();
					responseList.add(new DataSet().setTo(data.getTo()).setLocale(data.getLocale()).setAction(Action.DST_ERROR));
					try{
						return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
					} catch (JsonProcessingException ex) {
						return null;
					}
				}
				cn = newGw.get("mobile");
				gwAddress = newGw.get("gwAddress");
		    }
			if (cn!=null){
				//set our payment destination
				if (null == w.getPayDest()){
					w.setPayDest(new PaymentAddress());
				}
				w.getPayDest()
					.setAddress(cn.replace("+", ""))
					.setAddressType(PaymentType.ACCOUNT);
				w.getMsgDest()
					.setGateway(gwAddress);
			}
		}
		//set the fee
		w.setFee(data.getGwFee());
		w.setFeeAccount(data.getGwCn());
		//check that transaction amount is > fee 
		//(otherwise tx history gets screwed up)
		if (w.getAmount()!=BigDecimal.ZERO && w.getAmount().compareTo(w.getFee())<=0){
			data.setAction(Action.BELOW_FEE);
			try {
				return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
			} catch (JsonProcessingException e) {
				return null;
			}
		}
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/Restore")
	public Response restore(){
		return withdrawalReq();
	}

	@POST
	@Path("/Charge")
	public Response charge(){
        DataSet data = responseList.get(0);
        Withdrawal w = (Withdrawal)data.getPayload();
        try {
            w.setComment(merchantClient.charge(w.getAmount(), data.getTo().getPhoneNumber()).getToken());
        } catch (MerchantClientException | IOException | NoSuchAlgorithmException e) {
            log.error("charge exception",e);
            e.printStackTrace();
            return null;
        }
        try {
            return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
        } catch (JsonProcessingException e) {
            return null;
        }
	}
	
	@POST
	@Path("/Product")
	public Response products(){
		DataSet data = responseList.get(0);
		Withdrawal w = (Withdrawal)data.getPayload();
		try {
            w.setComment(merchantClient.product(w.getAmount(), data.getTo().getPhoneNumber()).getToken());
        } catch (NoSuchAlgorithmException | MerchantClientException | IOException e) {
            log.error("charge exception",e);
            e.printStackTrace();
            return null;
        }
        try {
            return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
        } catch (JsonProcessingException e) {
            return null;
        }
	}

	@POST
	@Path("/Pay")
	public Response pay(){
		DataSet data = responseList.get(0);
		Withdrawal w = (Withdrawal)data.getPayload();
		try {
            Charge charge = merchantClient.getCharge(w.getComment());
            data.setAction(Action.WITHDRAWAL_REQ);
            w.setPayDest(new PaymentAddress().setAddress(charge.getPayDest().getAddress())
                    .setAddressType((charge.getPayDest().getAddressType()==AddressType.ACCOUNT)?PaymentType.ACCOUNT:PaymentType.BTC));
            if (w.getAmount()!=null && w.getAmount().compareTo(charge.getAmount())!=0){
                return null;
            }
            w.setAmount(charge.getAmount())
             .setComment(charge.getComment())
             .setConfLink(charge.getConfLink())
             .setCurrencyCode(charge.getCurrencyCode())
             .setRate(charge.getRate());
            data.setPayload(w);
            return withdrawalReq();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException
                | URISyntaxException | MerchantClientException e) {
            log.error("pay exception",e);
            e.printStackTrace();
            return null;
        }
	}
	
	@POST
	@Path("/WithdrawalConf")
	public Response withdrawalConf(){
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/Buy") @SuppressWarnings("unchecked")
	public Response buy(){
		DataSet data = responseList.get(0);
		PhoneNumber pn = data.getTo().getPhoneNumber();
		if (null!=pn){
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			String mobile = phoneUtil.format(pn, PhoneNumberFormat.NATIONAL);
			Element e = cache.get("market"+pn.getCountryCode());
			List<Seller> sellers = null;
			if (null!=e){
				sellers = (List<Seller>)e.getObjectValue();
			}else{
				sellers = new ArrayList<Seller>();
			}
			boolean found = false;
			for (Seller seller: sellers){
				if (seller.getMobile().equalsIgnoreCase(mobile))
					found = true;
			}
			if (found){
				responseList.clear();
			}else{
				sellers.add(new Seller().setMobile(mobile).setPrice((float)data.getPayload()));
				cache.put(new Element("market"+pn.getCountryCode(),sellers));
			}
		}
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/Sell") @SuppressWarnings("unchecked")
	public Response sell(){
		DataSet data = responseList.get(0);
		PhoneNumber pn = data.getTo().getPhoneNumber();
		if (null!=pn){
			Element e = cache.get("market"+pn.getCountryCode());
			if (null!=e){
				List<Seller> sellers = (List<Seller>)e.getObjectValue();
				data.setPayload(sellers);
			}
		}
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/Price")
	public Response getPrice(){
		DataSet data = responseList.get(0);
		Withdrawal w = (Withdrawal)data.getPayload();
		if (null!=w){
			data.setGwFee(w.getAmount());
			data.setPayload(fiatPriceProvider.getLocalCurValue(w.getAmount(),data.getLocale()));
		}else{
		    CurrencyUnit cu = CurrencyUnit.of(new Builder().setRegion(data.getLocale().getCountry()).build());
			data.setPayload(fiatPriceProvider.getLocalCurValue(null,cu));
		}
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/Voice")
	public Response activateVoiceSecondFactor(){
		// nothing, just start workflow in parser client
		try {
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	@POST
	@Path("/Claim")
	public Response claimKey(){
		DataSet data = responseList.get(0);
		// nothing, just start workflow in parser client
		try {
			RestAPI restAPI = new RestAPI(MessagingServletConfig.plivoKey, MessagingServletConfig.plivoSecret, "v1");

			LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			String regionCode = phoneUtil.getRegionCodeForNumber(data.getTo().getPhoneNumber());
			String from = PhoneNumberUtil.getInstance().format(phoneUtil.getExampleNumberForType(regionCode, PhoneNumberType.MOBILE), PhoneNumberFormat.E164);
		    params.put("from", from.substring(0,from.length()-4)+"3737");
		    params.put("to", data.getTo().getAddress());
		    params.put("answer_url", MessagingServletConfig.basePath + "/plivo/claim/"+data.getTo().getAddress().replace("+", "")+"/"+data.getPayload()+"/"+mf.getLocale(data).toString());
		    params.put("hangup_url", MessagingServletConfig.basePath + "/plivo/claim/hangup/");
		    Call response = restAPI.makeCall(params);
		    if (response.serverCode != 200 && response.serverCode != 201 && response.serverCode !=204){
			throw new PlivoException(response.message);
		    }
			return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException | PlivoException | MalformedURLException e) {
			return null;
		}
	}

	@POST
	@Path("/UnknownCommand")
	public Response unknown(){
		if (responseList.size()==2){
			responseList.remove(0);
		}
		try {
			if (responseList.size()>0){
				return Response.ok(mapper.writeValueAsString(responseList), MediaType.APPLICATION_JSON).build();
			}else{
				return null;
			}
		} catch (JsonProcessingException e) {
			return null;
		}
	}

}

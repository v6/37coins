package com._37coins.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.internet.AddressException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Cache;

import org.btc4all.webfinger.WebfingerClient;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.pojo.Link;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.resources.ParserResource;
import com._37coins.util.FiatPriceProvider;
import com._37coins.web.PriceTick;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.MessageAddress;
import com._37coins.workflow.pojo.MessageAddress.MsgType;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Withdrawal;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Base58;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Singleton
public class ParserFilter implements Filter {
	public static Logger log = LoggerFactory.getLogger(ParserFilter.class);
	public static final String BC_ADDR_REGEX = "^[mn13][1-9A-Za-z][^OIl]{20,40}";
	
	private final Cache cache;
	private final FiatPriceProvider fiatPriceProvider;
	
	@Inject
	public ParserFilter(Cache cache,
			FiatPriceProvider fiatPriceProvider){
		this.cache = cache;
		this.fiatPriceProvider = fiatPriceProvider;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		// parse parameters
		String from = httpReq.getParameter("from");
		String gateway = httpReq.getParameter("gateway");
		String message = httpReq.getParameter("message");
		// Parse the locale
		String acceptLng = httpReq.getHeader("Accept-Language");
		Locale locale = DataSet.parseLocaleString(acceptLng);
		// parse action
		String url = httpReq.getRequestURL().toString();
		String actionString = url.substring(
				url.indexOf(ParserResource.PATH) + ParserResource.PATH.length() + 1, url.length());
		try {
			// parse message address
			MessageAddress md = MessageAddress.fromString(from, gateway)
					.setGateway(gateway);
			//exclude all roaming requests
			if (md.getAddressType() == MsgType.SMS 
					&& !isFromSameCountry(md, gateway)){
				respond(new ArrayList<DataSet>(), response);
			}
			//exclude non mobile numbers
			if (md.getAddressType() == MsgType.SMS
					&& !isMobileNumber(md)){
				respond(new ArrayList<DataSet>(), response);
			}
			//set locale
			if (md.getAddressType() == MsgType.SMS){
				PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
				String rc = phoneUtil.getRegionCodeForNumber(md.getPhoneNumber());
				locale = DataSet.parseLocaleString(rc);
			}else if (null==locale){
				locale = Locale.US;
			}
			// parse message into dataset
			DataSet responseData = process(md, message, locale,Action.fromString(actionString));
			List<DataSet> responseList = new ArrayList<>();
			responseList.add(responseData);
			//use it
			if (responseData.getAction()==Action.UNKNOWN_COMMAND||CommandParser.reqCmdList.contains(responseData.getAction())){
				httpReq.setAttribute("dsl", responseList);
				chain.doFilter(request, response);
			}else{
				respond(responseList,response);
			}
		} catch (Exception e) {
			log.error("parser exception", e);
			e.printStackTrace();
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private boolean isMobileNumber(MessageAddress sender){
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber pn = sender.getPhoneNumber();
		if (phoneUtil.getNumberType(pn)==PhoneNumberType.FIXED_LINE_OR_MOBILE 
				|| phoneUtil.getNumberType(pn)==PhoneNumberType.MOBILE){
			return true;
		}
		return false;
	}
	
	private boolean isFromSameCountry(MessageAddress sender, String gateway){
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber pn = null;
		if (null==gateway || gateway.length()<3){
			return true;
		}
		try {
			pn = phoneUtil.parse(gateway, "ZZ");
		} catch (NumberParseException e) {
		}
		if (pn!=null && pn.getCountryCode()==sender.getPhoneNumber().getCountryCode()){
			return true;
		}
		return false;
	}
	
	public void respond(List<DataSet> dsl, ServletResponse response){
		OutputStream os = null;
		try {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setContentType("application/json");
			os = httpResponse.getOutputStream();
			ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
	        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
			mapper.writeValue(os, dsl);
		} catch (IOException e) {
			log.error("parser exception", e);
			e.printStackTrace();
		} finally{
			try {if (null!=os)os.close();} catch (IOException e) {
				log.error("parser exception", e);
			}
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	public boolean readReceiver(Withdrawal w, String receiver, MessageAddress to) {
		if (receiver == null | receiver.length() < 3) {
			return false;
		}
		//try bitcoin address
		if (receiver.matches(BC_ADDR_REGEX)) {
			try {
				Base58.decodeChecked(receiver);
				w.setPayDest(new PaymentAddress().setAddress(receiver)
						.setAddressType(PaymentType.BTC));
				return true;
			} catch (AddressFormatException e) {
				return false;
			}
		}
		//try email
		if (receiver.matches(MessageAddress.EMAIL_REGEX)){
			String bitcoinAddr = null;
			try{
				WebfingerClient wc = new WebfingerClient(true);
				JsonResourceDescriptor jrd = wc.webFinger(receiver);
				
				for (Link l : jrd.getLinks()){
					if (l.getRel().contains("bitcoin")){
						bitcoinAddr = l.getHref().toString();
					}
				}
			}catch(IOException| URISyntaxException e){
				log.error("parser exception",e);
				e.printStackTrace();
			}
			if (bitcoinAddr!=null){
				//parse link
				String[] str = 	bitcoinAddr.split(":");
				bitcoinAddr = str[(str.length>2)?1:str.length-1];
			}else{
				return false;
			}
			w.setPayDest(new PaymentAddress()
				.setAddress(bitcoinAddr)
				.setAddressType(PaymentType.BTC));
			return true;
		}
		//try phone
		try {
			w.setMsgDest(MessageAddress.fromString(receiver, to));
			if (w.getMsgDest().getAddressType()==MsgType.SMS
				&& isMobileNumber(w.getMsgDest())){
				return true;
			}else{
				return false;
			}
		} catch (AddressException | NumberParseException e1) {
			return false;
		} catch (RuntimeException e2){
			return false;
		}
	}

	public boolean readAmount(Withdrawal w, String amount) {
		if (!amount.matches(".*[a-zA-Z]{3}.*")) {
			amount = "BTC " + amount;
		}else{
			if (amount.equals("all")){
				w.setAmount(BigDecimal.ZERO);
				return true;
			}else{
				String r[] = amount.split("[a-zA-Z]{3}");
				Pattern pattern = Pattern.compile("[a-zA-Z]{3}");
				Matcher matcher = pattern.matcher(amount);
				matcher.find();
				amount = matcher.group().toUpperCase() + " "+r[r.length-1];
			}
		}
		try {
			BigMoney money = BigMoney.parse(amount);
			BigDecimal val = money.getAmount().setScale(8, RoundingMode.HALF_UP); 
			if (money.getCurrencyUnit()!=CurrencyUnit.getInstance("BTC")){
				PriceTick pt = fiatPriceProvider.getLocalCurValue(null, money.getCurrencyUnit());
				val = money.getAmount().divide(pt.getLast(),8,RoundingMode.HALF_UP); 
			}else{
				val = val.divide(new BigDecimal(1000));
			}
			w.setAmount(val);
			return true;
		} catch (Exception e) {
			log.error("parser exception",e);
			e.printStackTrace();
			return false;
		}
	}

	public DataSet process(MessageAddress sender, String subject,
			Locale locale, Action action) {
		subject = subject.trim().replaceAll(" +", " ");
		String[] ca = subject.split(" ");
		DataSet data = new DataSet().setLocale(locale).setAction(action)
				.setTo(sender);

		if (data.getAction() == Action.WITHDRAWAL_REQ) {
			int pos = (ca[1].length() > ca[2].length()) ? 1 : 2;
			Withdrawal w = new Withdrawal();
			if (!readReceiver(w, ca[pos], data.getTo())
					|| !readAmount(w, ca[(pos == 1) ? 2 : 1])) {
				data.setAction(Action.FORMAT_ERROR);
				return data;
			}
			if (ca.length > 3) {
				int i = subject.indexOf(' ', 1 + subject.indexOf(' ',
						1 + subject.trim().indexOf(' ')));
				w.setComment(subject.replaceAll("::", "").substring(
						i + 1,
						(i + 1 + 20 > subject.length()) ? subject.length()
								: i + 1 + 20));
			}
			data.setPayload(w);
		}
		if (data.getAction() == Action.RESTORE){
			if (ca.length!=2){
				data.setAction(Action.FORMAT_ERROR);
				return data;
			}
			Withdrawal w = new Withdrawal();
			w.setAmount(BigDecimal.ZERO);
			MessageAddress ma;
			try{
				ma = MessageAddress.fromString(ca[1], data.getTo());
			}catch(AddressException | NumberParseException e){
				log.error("format error",e);
				data.setAction(Action.FORMAT_ERROR);
				return data;
			}
			w.setMsgDest(data.getTo());
			data.setTo(ma);
			data.setPayload(w);
			data.setAction(Action.WITHDRAWAL_REQ);
		}
		if (data.getAction() == Action.CHARGE 
				|| data.getAction() == Action.PRODUCT){
			Withdrawal w = new Withdrawal();
			if (readAmount(w, ca[1])){
				data.setPayload(w);
			}else{
				//error
			}
		}
		if (data.getAction() == Action.PAY){
			Withdrawal w = new Withdrawal();
			if (ca.length>1){
				w.setComment(ca[1]);
			}else{
				data.setAction(Action.FORMAT_ERROR);
			}
			data.setPayload(w);
		}
		if (data.getAction() == Action.WITHDRAWAL_CONF) {
			if (ca.length==2 && ca[1].toLowerCase().matches("[adgjmptw]\\d\\d\\d\\d")){
				data.setPayload(ca[1].toLowerCase());
			}else{
				data.setPayload(ca[0].toLowerCase());
			}
		}
		if (data.getAction() == Action.BUY 
				|| data.getAction() == Action.SELL) {
			float price = 1f;
			try{
				price = Float.parseFloat(ca[1]);
			}catch(Exception e){
				log.error("parse float failed",e);
			}
			data.setPayload(price);
		}
		if (data.getAction() == Action.CLAIM){
			data.setPayload(ca[1]);
		}
		if (data.getAction() == Action.PRICE){
			if (ca.length>1){
				Withdrawal w = new Withdrawal();
				readAmount(w, ca[1]);
				data.setPayload(w);
			}
		}
		return data;
	}
}

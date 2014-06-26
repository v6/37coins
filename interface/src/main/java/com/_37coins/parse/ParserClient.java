package com._37coins.parse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;
import javax.mail.internet.AddressException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.restnucleus.filter.DigestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.MessageAddress;
import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.i18n.phonenumbers.NumberParseException;

public class ParserClient extends Thread {
	public static Logger log = LoggerFactory.getLogger(ParserClient.class);
	private String from;
	private String gateway;
	private String gwCn;
	private String message;
	private int localPort;
	private final CommandParser commandParser;
	private ParserAction pa;
	private final GoogleAnalytics ga;
	private String digestToken;
	
	@Inject
	public ParserClient(CommandParser commandParser,
			GoogleAnalytics ga, String digestToken){
		this.commandParser = commandParser;
		this.ga = ga;
		this.digestToken = digestToken;
	}
	
	public void start(String from, String gateway, String gwCn, String message, int localPort, ParserAction pa){
		this.from = from;
		this.gateway = gateway;
		this.gwCn = (null==gwCn)?"":gwCn;
		this.message = message;
		this.localPort = localPort;
		this.pa = pa;
		this.start();
	}
	
    public static String calculateSignature(String uri, List<NameValuePair> nvps, String pw) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MultivaluedMap<String,String> paramMap = new MultivaluedHashMap<>(nvps.size());
        for (NameValuePair nvp: nvps){
            paramMap.add(nvp.getName(), nvp.getValue());
        }
        return DigestFilter.calculateSignature(uri, paramMap, pw);
    }

	@Override
	public void run() {
		Action action = commandParser.processCommand(message);
		Locale locale = commandParser.guessLocale(message);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost req = new HttpPost("http://127.0.0.1:"+localPort+"/parser/"+((null!=action)?action.getText():"UnknownCommand"));
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("from", from));
		nvps.add(new BasicNameValuePair("gateway", (null==gateway)?"":gateway));
		nvps.add(new BasicNameValuePair("gwCn", gwCn));
		nvps.add(new BasicNameValuePair("message", message));
		if (null!=locale){
			req.addHeader("Accept-Language", locale.toString().replace("_", "-"));
		}
		List<DataSet> results = null;
		try {
			req.setEntity(new UrlEncodedFormEntity(nvps));
			String reqSig = calculateSignature(req.getURI().toString(), nvps, digestToken);
            req.setHeader(DigestFilter.AUTH_HEADER, reqSig);
			CloseableHttpResponse rsp = httpclient.execute(req);
			int status = rsp.getStatusLine().getStatusCode();
			if (status==200){
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
		        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
				results = mapper.readValue(rsp.getEntity().getContent(),new TypeReference<List<DataSet>>() { });
				Collections.reverse(results);
			}else if(status==401){
			    throw new IllegalAccessError("not authorized to parser resource");
			}
			if (null==results){
				results = Arrays.asList(new DataSet().setAction(Action.FORMAT_ERROR).setTo(MessageAddress.fromString(from, gateway)).setLocale(locale));
			}
		} catch (IOException |AddressException |NumberParseException | NoSuchAlgorithmException e) {
			log.error("parser exception", e);
			e.printStackTrace();
		}
		String uuid = UUID.nameUUIDFromBytes(from.getBytes()).toString();
		uuid = uuid.substring(0, 14)+"4"+uuid.substring(15);//violates google analytics terms, as it is not randomA
		for (DataSet result: results){
			switch(result.getAction()){
			case WITHDRAWAL_REQ:
				pa.handleWithdrawal(result);
				break;
			case BALANCE:
			case TRANSACTION:
			case VOICE:
			case GW_DEPOSIT_REQ:
			case DEPOSIT_REQ:
				pa.handleDeposit(result);
				break;
			case WITHDRAWAL_CONF:
				pa.handleConfirm(result);
				break;
			default:
				pa.handleResponse(result);
				break;
			}
			if (result.getAction()!=Action.GW_BALANCE && result.getAction()!=Action.GW_DEPOSIT_REQ){
				ga.postAsync(new EventHit("parser", "processed", result.getAction().toString(), results.size()).clientId(uuid));
			}
		}
		if (results.size()==0){
			ga.postAsync(new EventHit("parser", "processed", message, results.size()).clientId(uuid));
		}
	}
	

	
	
}

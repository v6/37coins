package com._37coins.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.web.PriceTick;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FiatPriceProvider {
	public static Logger log = LoggerFactory.getLogger(FiatPriceProvider.class);
	
	final private Cache cache;
	private String url;
	private BigDecimal rate;
	private CurrencyUnit cu;
	
	@Inject
	public FiatPriceProvider(Cache cache, String url){
		this.cache = cache;
		this.url = url;
	}
	
	public FiatPriceProvider(BigDecimal rate, CurrencyUnit cu){
		this.rate = rate;
		this.cu = cu;
		this.cache = null;
	}
	
	public PriceTick getLocalCurValue(Locale locale){
		return getLocalCurValue(null, locale);
	}
	
	public PriceTick getLocalCurValue(BigDecimal btcValue, CurrencyUnit cu){
		//collected from api: https://api.bitcoinaverage.com/ticker/global/
		//on jan 25
		List<String> currencies = Arrays.asList(new String[] {"AED","AFN","AMD","ANG","AOA","ARS","AUD","AWG","AZN","BAM","BBD","BDT","BGN","BHD","BIF","BMD","BND","BOB","BRL","BSD","BTC","BTN","BWP","BYR","BZD","CAD","CDF","CHF","CLF","CLP","CNY","COP","CRC","CUP","CVE","CZK","DJF","DKK","DOP","DZD","EEK","EGP","ERN","ETB","EUR","FJD","FKP","GBP","GEL","GHS","GIP","GMD","GNF","GTQ","GYD","HKD","HNL","HRK","HTG","HUF","IDR","ILS","INR","IQD","IRR","ISK","JEP","JMD","JOD","JPY","KES","KGS","KHR","KMF","KPW","KRW","KWD","KYD","KZT","LAK","LBP","LKR","LRD","LSL","LTL","LVL","LYD","MAD","MDL","MGA","MKD","MMK","MNT","MOP","MRO","MTL","MUR","MVR","MWK","MXN","MYR","MZN","NAD","NGN","NIO","NOK","NPR","NZD","OMR","PAB","PEN","PGK","PHP","PKR","PLN","PYG","QAR","RON","RSD","RUB","RWF","SAR","SBD","SCR","SDG","SEK","SGD","SHP","SLL","SOS","SRD","STD","SVC","SYP","SZL","THB","TJS","TMT","TND","TOP","TRY","TTD","TWD","TZS","UAH","UGX","USD","UYU","UZS","VEF","VND","VUV","WST","XAF","XAG","XAU","XCD","XDR","XOF","XPF","YER","ZAR","ZMK","ZMW","ZWL"});
		if (!currencies.contains(cu.getCode())){
			cu = CurrencyUnit.of("USD");
		}
		Element e = null;
		if (null!=cache){
			e = cache.get("price"+cu.getCode());
		}
		if (null==e){
			Map<String,PriceTick> temp = null;
			try{
			    RequestConfig defaultRequestConfig = RequestConfig.custom()
			        .setSocketTimeout(500)
			        .setConnectTimeout(500)
			        .setConnectionRequestTimeout(500)
			        .setStaleConnectionCheckEnabled(true)
			        .build();
				HttpClient client = HttpClientBuilder.create()
				    .setDefaultRequestConfig(defaultRequestConfig).build();
				HttpGet someHttpGet = new HttpGet(url+"/all");
				URI uri = new URIBuilder(someHttpGet.getURI()).build();
				HttpRequestBase request = new HttpGet(uri);
				HttpResponse response = client.execute(request);
				temp = new ObjectMapper().readValue(response.getEntity().getContent(), new TypeReference<Map<String,PriceTick>>(){});
			}catch(Exception ex){
				log.error("fiat price exception",ex);
				ex.printStackTrace();
				return null;
			}
			for (Entry<String,PriceTick> pt: temp.entrySet()){
				if (pt.getKey().equalsIgnoreCase(cu.getCode())){
					e = new Element("price"+pt.getKey(), pt.getValue());
				}
				if (pt.getValue()!=null){
    				Element te = new Element("price"+pt.getKey(), pt.getValue());
    				if (null!=cache){
    					cache.put(te);
    				}
				}
			}
		}
		PriceTick pt = (PriceTick)e.getObjectValue();
		if (btcValue!=null){
			btcValue.setScale(8,RoundingMode.HALF_DOWN);
			BigDecimal price = pt.getLast().setScale(cu.getDecimalPlaces(),RoundingMode.HALF_DOWN);
			pt.setLastFactored(btcValue.multiply(price));
		}
		pt.setCurCode(cu.getCode());
		return pt;		
	}
	
	public PriceTick getLocalCurValue(BigDecimal btcValue, Locale locale){
		if (null!=btcValue){
			if (null!=rate&&null!=cu){
				return new PriceTick().setLastFactored(btcValue.multiply(rate)).setCurCode(cu.getCode()).setLast(rate);
			}
			CurrencyUnit cu = CurrencyUnit.of(new Builder().setRegion(locale.getCountry()).build());
			return getLocalCurValue(btcValue, cu);
		}
		return null;
	}
	
	public String getLocalCurCode(Locale locale){
		CurrencyUnit cu = CurrencyUnit.of(new Builder().setRegion(locale.getCountry()).build());
		return cu.getCode();
	}
	
	public String getLocalCurCode(){
		if (null!=cu){
			return cu.getCode();
		}else{
			return null;
		}
	}

}

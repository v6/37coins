package com._37coins.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.inject.Inject;

@Singleton
public class AbuseFilter implements Filter {
	public static Logger log = LoggerFactory.getLogger(AbuseFilter.class);
	private Cache cache;
	
	@Inject
	public AbuseFilter(Cache cache){
		this.cache = cache;
	}
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override @SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		List<DataSet> responseList = (List<DataSet>)httpReq.getAttribute("dsl");
		DataSet ds = responseList.get(0);
		if (null!=ds.getTo().getPhoneNumber() &&(
				ds.getAction()==Action.PRICE
				|| ds.getAction()==Action.SIGNUP
				|| ds.getAction()==Action.DEPOSIT_REQ)){
			String number = PhoneNumberUtil.getInstance().format(ds.getTo().getPhoneNumber(),PhoneNumberFormat.E164);
			String key= ds.getAction()+number;
			if (null==cache.get(key)){
				chain.doFilter(request, response);
				cache.put(new Element(key,true));
			}else{
				if (null==cache.get("abuse"+number)){
					ds.setAction(Action.OVERUSE);
					respond(responseList, httpResponse);
					cache.put(new Element("abuse"+number,true));
				}else{
					responseList.clear();
					respond(responseList, httpResponse);
				}
			}
		}else{
			chain.doFilter(request, response);
		}
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
			log.error("abuse filter exception",e);
			e.printStackTrace();
		} finally{
			try {if (null!=os)os.close();} catch (IOException e) {}
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}

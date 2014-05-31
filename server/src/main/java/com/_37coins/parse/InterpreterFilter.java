package com._37coins.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.persistence.dao.Account;
import com._37coins.persistence.dao.Gateway;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;

@Singleton
public class InterpreterFilter implements Filter {
	public static Logger log = LoggerFactory.getLogger(InterpreterFilter.class);
	
	@SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		List<DataSet> responseList = (List<DataSet>)httpReq.getAttribute("dsl");
		DataSet responseData = responseList.get(0);
	    String gwCn = httpReq.getParameter("gwCn");
		GenericRepository dao = (GenericRepository)httpReq.getAttribute("gr");
		//get user from directory
		Account a = dao.queryEntity(new RNQuery().addFilter("mobile", responseData.getTo().getAddress()), Account.class,false);
		if (null!=a){
			//check if account is disabled
			if (null!=a.getPinWrongCount()&&a.getPinWrongCount()>=3){
				responseData.setAction(Action.ACCOUNT_BLOCKED);
				respond(responseList,response);
				return;
			}
			responseData.setCn(a.getId().toString());
			//read the gateway
			Gateway g = a.getOwner();

			//check if gateway changed
			if (!(null==responseData.getTo().getGateway()||g.getCn().equalsIgnoreCase(responseData.getTo().getGateway()))){
				//look up the new gateway and overwrite all values
			    g = dao.queryEntity(new RNQuery().addFilter("mobile", responseData.getTo().getGateway()), Gateway.class);
			    a.setOwner(g);
			}
			//check locale
			if (a.getLocale()!=null){
			    if (a.getLocale().getCountry()==null){
			        a.setLocale(new Locale(a.getLocale().getLanguage(),responseData.getLocale().getCountry()));
			    }else{
			        a.setLocale(new Locale(g.getLocale().getLanguage(),responseData.getLocale().getCountry()));
			    }
			}else{
			    a.setLocale(new Locale(g.getLocale().getLanguage(),responseData.getLocale().getCountry()));
			}
			responseData.setGwFee(g.getFee())
			    .setLocale(a.getLocale())
			    .getTo().setGateway(g.getCn());
			responseData.setGwCn(g.getCn());
		}else{//new user
			if (responseData.getAction()!=Action.SIGNUP){
			    Gateway g = dao.queryEntity(new RNQuery().addFilter("mobile", responseData.getTo().getGateway()), Gateway.class);
			    a = new Account()
			        .setMobile(responseData.getTo().getAddress())
			        .setOwner(g);
			    dao.add(a);
			    responseData.getTo().setGateway(g.getCn());
			    responseData.setCn(responseData.getTo().getAddress().replace("+", "")).setGwCn(g.getCn()).setGwFee(g.getFee());
			    //respond to new user with welcome message
				DataSet create = new DataSet()
					.setAction(Action.SIGNUP)
					.setTo(responseData.getTo())
					.setCn(responseData.getCn())
					.setLocale(responseData.getLocale())
					.setService(responseData.getService());
				httpReq.setAttribute("create", create);
			}
		}
        if (gwCn!=null){
            responseData.getTo().setGateway(gwCn);
            responseData.setGwCn(gwCn);
        }
		chain.doFilter(request, response);
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
			log.error("interpreter exception", e);
			e.printStackTrace();
		} finally{
			try {if (null!=os)os.close();} catch (IOException e) {}
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

}

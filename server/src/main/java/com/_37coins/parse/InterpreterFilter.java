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
import com._37coins.workflow.pojo.Signup;
import com._37coins.workflow.pojo.Signup.Source;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

@Singleton
public class InterpreterFilter implements Filter {
	public static Logger log = LoggerFactory.getLogger(InterpreterFilter.class);
	
	@SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
	    HttpServletRequest httpReq = (HttpServletRequest)request;
	    if (null==httpReq.getAttribute("flag")){
	        httpReq.setAttribute("flag", true);
    		List<DataSet> responseList = (List<DataSet>)httpReq.getAttribute("dsl");
    		DataSet responseData = responseList.get(0);
    	    String gwCn = (String)httpReq.getAttribute("gwCn");
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
    			//check if gateway changed
    			if (null!=responseData.getTo().getGateway()&&!g.getMobile().equalsIgnoreCase(responseData.getTo().getGateway())){
    				//look up the new gateway and overwrite all values
    			    g = dao.queryEntity(new RNQuery().addFilter("mobile", responseData.getTo().getGateway()), Gateway.class);
    			    a.setOwner(g);
    	            //respond to user with welcome message from new gateway.
                    DataSet create = new DataSet()
                        .setAction(Action.SIGNUP)
                        .setTo(responseData.getTo())
                        .setCn(responseData.getCn())
                        .setLocale(responseData.getLocale())
                        .setPayload(new Signup()
                            .setMobile(responseData.getTo().getAddress())
                            .setSource(Source.MOVE)
                            .setSignupCallback(g.getSettings().getSignupCallback())
                            .setWelcomeMessage(g.getSettings().getWelcomeMsg())
                            .setDigestToken(g.getApiSecret()))
                        .setService(g.getSettings().getCompanyName());
                    httpReq.setAttribute("create", create);                
    			}
    			responseData.setGwFee(g.getSettings().getFee())
    			    .setLocale(a.getLocale())
    			    .getTo().setGateway(g.getCn());
    			responseData.setGwCn(g.getCn());
    		}else{//new user
    			if (responseData.getAction()!=Action.SIGNUP){
    			    Gateway g = dao.queryEntity(new RNQuery().addFilter("mobile", responseData.getTo().getGateway()), Gateway.class);
    			    a = new Account()
    			        .setMobile(responseData.getTo().getAddress())
    			        .setOwner(g);
    			    if (g.getLocale()==null||g.getLocale().getCountry()==null){
    			        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    	                String rc = phoneUtil.getRegionCodeForNumber(responseData.getTo().getPhoneNumber());
    			        a.setLocale(new Locale("",rc));
    			    }
    			    a.setLocale(g.getLocale());
    			    for (DataSet ds: responseList)
    			        ds.setLocale(g.getLocale());
    			    dao.add(a);
    			    responseData.getTo().setGateway(g.getCn());
    			    responseData.setCn(responseData.getTo().getAddress().replace("+", "")).setGwCn(g.getCn()).setGwFee(g.getSettings().getFee());
    			    //respond to new user with welcome message
    				DataSet create = new DataSet()
    					.setAction(Action.SIGNUP)
    					.setTo(responseData.getTo())
    					.setCn(responseData.getCn())
    					.setLocale(responseData.getLocale())
    					.setPayload(new Signup()
                            .setMobile(responseData.getTo().getAddress())
                            .setSource(Source.NEW)
                            .setSignupCallback(g.getSettings().getSignupCallback())
                            .setWelcomeMessage(g.getSettings().getWelcomeMsg())
                            .setDigestToken(g.getApiSecret()))
    					.setService(g.getSettings().getCompanyName());
    				httpReq.setAttribute("create", create);
    			}
    		}
            if (gwCn!=null){
                responseData.getTo().setGateway(gwCn);
                responseData.setGwCn(gwCn);
            }
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

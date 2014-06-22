package com._37coins;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.restnucleus.WrappedRequest;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com._37coins.persistence.dao.Gateway;
import com._37coins.pojo.EnvayaRequest;

@Singleton
public class EnvayaFilter implements Filter {
    private String basePath;
    
    public EnvayaFilter(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        WrappedRequest wrappedRequest = new WrappedRequest(
                (HttpServletRequest) request);
        
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String url = basePath + httpReq.getServletPath() + httpReq.getPathInfo();
        String queryString = httpReq.getQueryString();
        if (queryString != null)  {
            url = url + '?' +queryString;
        }
        String sig = httpReq.getHeader(EnvayaClient.AUTH_HEADER);
        String calcSig = null;
        
        EnvayaRequest envayaRequest = null;
        if (httpReq.getMethod().equalsIgnoreCase("POST") || httpReq.getMethod().equalsIgnoreCase("PUT")){
            envayaRequest = EnvayaRequest.fromBody(wrappedRequest.getInputStream());
        }
        wrappedRequest.setAttribute("er", envayaRequest);
        String p = httpReq.getPathInfo().substring(1,httpReq.getPathInfo().length()-1);
        String cn = p.substring(p.indexOf("/")+1, p.lastIndexOf("/"));
        GenericRepository dao = (GenericRepository)httpReq.getAttribute("gr");
        wrappedRequest.setAttribute("gr", dao);
        Gateway g = dao.queryEntity(new RNQuery().addFilter("cn", cn), Gateway.class);
        try {
            calcSig = EnvayaClient.calculateSignature(url, (null!=envayaRequest)?envayaRequest.toMap():new ArrayList<NameValuePair>(), g.getApiSecret());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (calcSig!=null && calcSig.equals(sig)){
            chain.doFilter(wrappedRequest, response);
        } else{
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(401);
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

package com._37coins;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.inject.Singleton;
import javax.jdo.PersistenceManagerFactory;
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
import com.google.inject.Key;
import com.google.inject.name.Names;

@Singleton
public class EnvayaFilter implements Filter {
    private String basePath;
    private final GenericRepository dao;
    
    public EnvayaFilter(String basePath, PersistenceManagerFactory pmf) {
        this.basePath = basePath;
        this.dao = new GenericRepository(pmf);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String sig = httpReq.getHeader(EnvayaClient.AUTH_HEADER);
        String calcSig = null;
        WrappedRequest wrappedRequest = null;
        if (null==httpReq.getAttribute("er")){
            wrappedRequest = new WrappedRequest(
                (HttpServletRequest) request);
            String url = basePath + httpReq.getServletPath() + httpReq.getPathInfo();
            String queryString = httpReq.getQueryString();
            if (queryString != null)  {
                url = url + '?' +queryString;
            }
            
            EnvayaRequest envayaRequest = null;
            if (httpReq.getMethod().equalsIgnoreCase("POST") || httpReq.getMethod().equalsIgnoreCase("PUT")){
                envayaRequest = EnvayaRequest.fromBody(wrappedRequest.getInputStream());
            }
            wrappedRequest.setAttribute("er", envayaRequest);
            httpReq.setAttribute(Key.get(EnvayaRequest.class, Names.named("er")).toString(), envayaRequest);
            String p = httpReq.getPathInfo().substring(1,httpReq.getPathInfo().length()-1);
            String cn = p.substring(p.indexOf("/")+1, p.lastIndexOf("/"));
            try {
                Gateway g = dao.queryEntity(new RNQuery().addFilter("cn", cn), Gateway.class);
                calcSig = EnvayaClient.calculateSignature(url, (null!=envayaRequest)?envayaRequest.toMap():new ArrayList<NameValuePair>(), g.getApiSecret());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } finally{
                dao.closePersistenceManager();
            }
            if (calcSig!=null && calcSig.equals(sig)){
                chain.doFilter(wrappedRequest, response);
            } else{
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(401);
            }
        }else{
            chain.doFilter(request, response);
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

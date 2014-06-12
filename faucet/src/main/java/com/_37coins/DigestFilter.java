package com._37coins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.restnucleus.WrappedRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.ImmutableList;

@Singleton
public class DigestFilter implements Filter {
    public final static String AUTH_HEADER = "X-Request-Signature";
    
    private String hmacToken;
    private String faucetPath;
    
    @Inject
    public DigestFilter(String hmacToken, String faucetPath){
        this.hmacToken = hmacToken;
        this.faucetPath = faucetPath;
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        WrappedRequest wrappedRequest = new WrappedRequest(
                (HttpServletRequest) request);
        
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String url = faucetPath;
        String sig = httpReq.getHeader(AUTH_HEADER);
        String calcSig = null;
        
        MultivaluedMap<String, String> map = null;
        if (httpReq.getMethod().equalsIgnoreCase("POST") || httpReq.getMethod().equalsIgnoreCase("PUT")){
            try {
                map = parseJson(wrappedRequest.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (map==null){
            map = new MultivaluedHashMap<>();
        }
        
        try {
            calcSig = calculateSignature(url, map, hmacToken);
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
    
    public static String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }
    
    public static MultivaluedMap<String, String> parseJson(byte[] json) throws JsonProcessingException, IOException{
        return parseJson(new ByteArrayInputStream(json));
    }
    
    public static MultivaluedMap<String, String> parseJson(InputStream json) throws JsonProcessingException, IOException{
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        JsonNode node = mapper.readTree(json);
        return (collectLeaves(node, map));
    }
    
    public static MultivaluedMap<String, String> collectLeaves(JsonNode node, MultivaluedMap<String, String> map){
        Iterator<Entry<String,JsonNode>> ite = node.fields();
        while (ite.hasNext()) {
            Entry<String,JsonNode> temp = ite.next();
            if (temp.getValue().getNodeType()==JsonNodeType.OBJECT){
                collectLeaves(temp.getValue(), map);
            }else{
                if (temp.getValue() instanceof DecimalNode){
                    DecimalNode dn = (DecimalNode)temp.getValue();
                    map.put(temp.getKey(),ImmutableList.of(dn.decimalValue().toPlainString()));
                }else{
                    map.put(temp.getKey(),ImmutableList.of(temp.getValue().asText()));
                }
            }
        }
        return map;
    }
    
    public static String calculateSignature(String url, MultivaluedMap<String,String> paramMap, String pw) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        if (null==url||null==paramMap||null==pw){
            return null;
        }
        List<String> params = new ArrayList<>();
        for (Entry<String,List<String>> m :paramMap.entrySet()){
            if (m.getValue().size()>0){
                params.add(m.getKey()+"="+m.getValue().get(0));
            }
        }
        Collections.sort(params);
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        for (String s : params){
            sb.append(",");
            sb.append(s);
        }
        sb.append(",");
        sb.append(pw);
        String value = sb.toString();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(value.getBytes("utf-8"));

        return new String(Base64.encodeBase64(md.digest()));     
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}
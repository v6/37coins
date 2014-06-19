package com._37coins;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.pojo.EnvayaRequest;
import com._37coins.pojo.EnvayaResponse;
import com._37coins.pojo.EnvayaRequest.Action;
import com._37coins.pojo.EnvayaRequest.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EnvayaClient {
    private static final Logger log = LoggerFactory.getLogger(EnvayaClient.class);
    public final static String AUTH_HEADER = "X-Request-Signature";
    private final static String CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
    private HttpClient httpClient;
    private String uri;
    private String digestToken;
    private String mobile;

    public static HttpClientBuilder getClientBuilder() {
        return HttpClientBuilder.create().setRedirectStrategy(
            new DefaultRedirectStrategy() {
                @Override
                public boolean isRedirected(HttpRequest request,
                        HttpResponse response, HttpContext context)
                        throws ProtocolException {
                    return response.getStatusLine().getStatusCode() == 308
                            || super.isRedirected(request, response,
                                    context);
                }
            });
    }
    
    public static String calculateSignature(String uri, List<NameValuePair> nvps, String pw) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Map<String,String> paramMap = new HashMap<>(nvps.size());
        for (NameValuePair nvp: nvps){
            paramMap.put(nvp.getName(), nvp.getValue());
        }
        return calculateSignature(uri, paramMap, pw);
    }
    
    
    public static String calculateSignature(String url, Map<String,String> paramMap, String pw) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        if (null==url||null==paramMap||null==pw){
            return null;
        }
        List<String> params = new ArrayList<>();
        for (Entry<String,String> m :paramMap.entrySet()){
            if (null != m.getValue()){
                params.add(m.getKey()+"="+m.getValue());
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
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(value.getBytes("utf-8"));

        return new String(Base64.encodeBase64(md.digest()));     
    }

    public EnvayaClient(String uri, String digestToken, String mobile) {
        this(uri, digestToken, mobile, getClientBuilder().build());
    }

    public EnvayaClient(String uri, String digestToken, String mobile,
            HttpClient httpClient) {
        this.uri = uri;
        this.mobile = mobile;
        this.digestToken = digestToken;
        this.httpClient = httpClient;
    }

    protected <K> K parsePayload(HttpResponse response, Class<K> entityClass)
            throws EnvayaClientException {
        try {
            return new ObjectMapper().readValue(response.getEntity()
                    .getContent(), entityClass);
        } catch (IOException e) {
            log.error("envaya client error", e);
            throw new EnvayaClientException(
                    EnvayaClientException.Reason.ERROR_PARSING, e);
        }
    }

    protected <K> K getPayload(HttpRequestBase request, Class<K> entityClass)
            throws EnvayaClientException {
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            log.error("envaya client error", e);
            throw new EnvayaClientException(
                    EnvayaClientException.Reason.ERROR_GETTING_RESOURCE, e);
        }
        if (isSucceed(response) && request.getMethod() != "DELETE") {
            return parsePayload(response, entityClass);
        } else if (isSucceed(response)) {
            return null;
        } else {
            throw new EnvayaClientException(
                    EnvayaClientException.Reason.AUTHENTICATION_FAILED);
        }
    }
    
    protected <K> K getPayload(EnvayaRequest request, Class<K> entityClass)
            throws EnvayaClientException {
        request
            .setPhoneNumber(mobile)
            .setNow(System.currentTimeMillis());
        HttpPost req = new HttpPost(uri);
        req.setHeader("Content-type", CONTENT_TYPE);
        String reqSig = null;
        try {
            reqSig = EnvayaClient.calculateSignature(uri, request.toMap(), digestToken);
            req.setHeader(EnvayaClient.AUTH_HEADER, reqSig);
            req.setEntity(request.toBody());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new EnvayaClientException(
                    EnvayaClientException.Reason.ERROR_PARSING);
        }
        return getPayload(req, entityClass);        
    }

    public EnvayaResponse test() throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.TEST), EnvayaResponse.class);
    }

    public EnvayaResponse sendStatus(EnvayaRequest.Status status, String id, String error) throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.SEND_STATUS)
            .setId(id)
            .setStatus(status)
            .setError(error), EnvayaResponse.class);
    }
    
    public EnvayaResponse deviceStatus(EnvayaRequest.Status status) throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.DEVICE_STATUS)
            .setStatus(status), EnvayaResponse.class);
    }
    
    public EnvayaResponse incoming(String from, MessageType messageType, String message, Long timestamp) throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.INCOMING)
            .setFrom(from)
            .setMessageType(messageType)
            .setMessage(message)
            .setTimestamp(timestamp), EnvayaResponse.class);
    }
    
    public EnvayaResponse outgoing() throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.OUTGOING), EnvayaResponse.class);
    }
    
    public EnvayaResponse amqpStarted(String consumerTag) throws EnvayaClientException {
        return getPayload(new EnvayaRequest()
            .setAction(Action.AMQP_STARTED)
            .setConsumerTag(consumerTag), EnvayaResponse.class);
    }
    
    public static boolean isSucceed(HttpResponse response) {
        return response.getStatusLine().getStatusCode() >= 200
                && response.getStatusLine().getStatusCode() < 300;
    }

    public static String toLowerCase(String str) {
        return !str.contains("%") ? str.toLowerCase() : str;
    }

}

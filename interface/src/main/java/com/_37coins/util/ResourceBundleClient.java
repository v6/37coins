package com._37coins.util;

import java.io.IOException;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.restnucleus.filter.DigestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResourceBundleClient {
    private HttpClient httpClient;
    private String uri;
    static private RequestConfig config = RequestConfig.custom().
            setConnectTimeout(3 * 1000).
            setConnectionRequestTimeout(3 * 1000).
            setSocketTimeout(3 * 1000).build();

    public ResourceBundleClient(String uri) {
        this(uri, HttpClients.custom().disableContentCompression().setDefaultRequestConfig(config).build());
    }

    public ResourceBundleClient(String uri, HttpClient httpClient) {
        this.uri = uri;
        this.httpClient = httpClient;
    }
    
    protected MultivaluedMap<String,String> parsePayload(HttpResponse response) throws IOException {
        ResourceBundleInputStream is = new ResourceBundleInputStream(response.getEntity().getContent());
        JsonNode rootNode = new ObjectMapper().readTree(is);
        MultivaluedMap<String,String> rv = new MultivaluedHashMap<>();
        DigestFilter.collectLeaves(rootNode.get("title"), rv);
        DigestFilter.collectLeaves(rootNode.get("desc"), rv);
        DigestFilter.collectLeaves(rootNode.get("sms"), rv);
        DigestFilter.collectLeaves(rootNode.get("commands"), rv);
        DigestFilter.collectLeaves(rootNode.get("email"), rv);
        DigestFilter.collectLeaves(rootNode.get("voice"), rv);
        return rv;
    }

    protected MultivaluedMap<String,String> getPayload(HttpRequestBase request) throws IOException {
        HttpResponse response;
        try{
            response = httpClient.execute(request);
            if (isSucceed(response) && request.getMethod() != "DELETE") {
                return parsePayload(response);
            } else if (isSucceed(response)) {
                return null;
            }else{
                throw new IOException("no result");
            }
        }finally{
            request.releaseConnection();
        }
    }

    public MultivaluedMap<String,String> fetchBundle(String locale, String className) throws IOException {
        if (null==locale || locale.equals("en") || locale.equals("")){
            locale = "root";
        }
        HttpGet req = new HttpGet(uri+locale+"/"+className+".js");
        return getPayload(req);
    }


    
    public static boolean isSucceed(HttpResponse response) {
        return response.getStatusLine().getStatusCode() >= 200
                && response.getStatusLine().getStatusCode() < 300;
    }

    public static String toLowerCase(String str) {
        return !str.contains("%") ? str.toLowerCase() : str;
    }

}

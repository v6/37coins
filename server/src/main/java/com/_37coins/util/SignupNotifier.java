package com._37coins.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class SignupNotifier extends Thread {
    
    public enum Source {
        MOVE,
        REFERRED,
        NEW
    }
    
    private String mobile;
    private Source source;
    private String url;
    private CloseableHttpClient httpclient;
    
    public SignupNotifier(String url, String mobile, Source source){
        this.mobile = mobile;
        this.source = source;
        this.url = url;
        httpclient = HttpClients.createDefault();
    }
    
    @Override
    public void run() {
        HttpPost httpPost = new HttpPost(url);
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("mobile", mobile));
        nvps.add(new BasicNameValuePair("source", source.toString()));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

}

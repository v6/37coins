package com._37coins.util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.restnucleus.filter.DigestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.workflow.pojo.Signup;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SignupNotifier extends Thread {
    private static final Logger log = LoggerFactory.getLogger(SignupNotifier.class);
    private Signup signup;
    private CloseableHttpClient httpClient;
    
    public SignupNotifier(Signup signup) {
        this.signup = signup;
        httpClient = HttpClients.createDefault();
    }

    @Override
    public void run() {
        Signup value = new Signup()
            .setMobile(signup.getMobile())
            .setReferrer(signup.getReferrer())
            .setDestination(signup.getDestination());
        try {
            String reqValue = new ObjectMapper().writeValueAsString(value);
            StringEntity entity = new StringEntity(reqValue, "UTF-8");
            entity.setContentType("application/json");
            String reqSig = DigestFilter.calculateSignature(
                    signup.getSignupCallback(),
                    DigestFilter.parseJson(reqValue.getBytes()),
                    signup.getDigestToken());
            HttpPost req = new HttpPost(signup.getSignupCallback());
            req.setHeader(DigestFilter.AUTH_HEADER, reqSig);
            req.setEntity(entity);
            httpClient.execute(req);
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("products client error", e);
        }
    }

}

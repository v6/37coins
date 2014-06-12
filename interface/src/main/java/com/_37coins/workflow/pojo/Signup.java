package com._37coins.workflow.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Signup {
    
    public enum Source {
        MOVE,
        REFERRED,
        NEW
    }
    
    private String mobile;
    private PaymentAddress destination;
    private String referrer;
    private String signupCallback;
    private String welcomeMessage;
    private Source source;
    private String digestToken;

    public String getMobile() {
        return mobile;
    }
    public Signup setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }
    public PaymentAddress getDestination() {
        return destination;
    }
    public Signup setDestination(PaymentAddress destination) {
        this.destination = destination;
        return this;
    }
    public String getReferrer() {
        return referrer;
    }
    public Signup setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }
    public String getSignupCallback() {
        return signupCallback;
    }
    public Signup setSignupCallback(String signupCallback) {
        this.signupCallback = signupCallback;
        return this;
    }
    public String getWelcomeMessage() {
        return welcomeMessage;
    }
    public Signup setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
        return this;
    }
    public Source getSource() {
        return source;
    }
    public Signup setSource(Source source) {
        this.source = source;
        return this;
    }
    public String getDigestToken() {
        return digestToken;
    }
    public Signup setDigestToken(String digestToken) {
        this.digestToken = digestToken;
        return this;
    }    
    
    
}

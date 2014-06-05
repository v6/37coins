package com._37coins.web;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class SettingsRequest implements Serializable{
    private static final long serialVersionUID = -3542021994039992803L;
    private BigDecimal fee;
    private String companyName;
    private String welcomeMsg;
    private String callbackUrl;
    public BigDecimal getFee() {
        return fee;
    }
    public SettingsRequest setFee(BigDecimal fee) {
        this.fee = fee;
        return this;
    }
    public String getCompanyName() {
        return companyName;
    }
    public SettingsRequest setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }
    public String getWelcomeMsg() {
        return welcomeMsg;
    }
    public SettingsRequest setWelcomeMsg(String welcomeMsg) {
        this.welcomeMsg = welcomeMsg;
        return this;
    }
    public String getCallbackUrl() {
        return callbackUrl;
    }
    public SettingsRequest setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }

}

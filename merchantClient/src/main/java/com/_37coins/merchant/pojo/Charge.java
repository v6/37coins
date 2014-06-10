package com._37coins.merchant.pojo;

import java.math.BigDecimal;

public class Charge {
    
    private BigDecimal amount;
    private String comment;
    private String confLink;
    private String currencyCode;
    private Double rate;
    private PaymentDestination payDest;
    
    public BigDecimal getAmount() {
        return amount;
    }
    public Charge setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
    public String getComment() {
        return comment;
    }
    public Charge setComment(String comment) {
        this.comment = comment;
        return this;
    }
    public String getConfLink() {
        return confLink;
    }
    public Charge setConfLink(String confLink) {
        this.confLink = confLink;
        return this;
    }
    public String getCurrencyCode() {
        return currencyCode;
    }
    public Charge setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }
    public Double getRate() {
        return rate;
    }
    public Charge setRate(Double rate) {
        this.rate = rate;
        return this;
    }
    public PaymentDestination getPayDest() {
        return payDest;
    }
    public Charge setPayDest(PaymentDestination payDest) {
        this.payDest = payDest;
        return this;
    }

}

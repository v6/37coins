package com._37coins.web;

import java.math.BigDecimal;

import com._37coins.workflow.pojo.PaymentAddress;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonInclude(Include.NON_NULL)
public class MerchantRequest {
	
	private BigDecimal amount;
	
	private String callbackUrl;
	
	private PriceTick conversion;
	
	private String orderName;
	
	private PaymentAddress payDest;
	
	private Timeout timeout;
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	@JsonIgnore
	public BigDecimal getMiliAmount() {
		return amount.multiply(new BigDecimal(1000));
	}

	public MerchantRequest setAmount(BigDecimal amount) {
		this.amount = amount;
		return this;
	}

	public PaymentAddress getPayDest() {
		return payDest;
	}

	public MerchantRequest setPayDest(PaymentAddress payAddress) {
		this.payDest = payAddress;
		return this;
	}

	public String getOrderName() {
		return orderName;
	}

	public MerchantRequest setOrderName(String orderName) {
		this.orderName = orderName;
		return this;
	}

	public Timeout getTimeout() {
		return timeout;
	}

	public MerchantRequest setTimeout(Timeout timeout) {
		this.timeout = timeout;
		return this;
	}

	public PriceTick getConversion() {
		return conversion;
	}

	public MerchantRequest setConversion(PriceTick conversion) {
		this.conversion = conversion;
		return this;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public MerchantRequest setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
		return this;
	}

}
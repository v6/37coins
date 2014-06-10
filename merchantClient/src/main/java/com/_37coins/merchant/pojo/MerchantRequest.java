package com._37coins.merchant.pojo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonInclude(Include.NON_NULL)
public class MerchantRequest {
	
	private BigDecimal amount;
	
	private String callbackUrl;
	
	private Conversion conversion;
	
	private String orderName;
	
	private PaymentDestination payDest;
	
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

	public PaymentDestination getPayDest() {
		return payDest;
	}

	public MerchantRequest setPayDest(PaymentDestination payAddress) {
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

	public Conversion getConversion() {
		return conversion;
	}

	public MerchantRequest setConversion(Conversion conversion) {
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
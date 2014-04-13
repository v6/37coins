package com._37coins.web;

import java.math.BigDecimal;

public class MerchantSession {
	
	private String phoneNumber;
	
	private String otp;
	
	private String sessionToken;
	
	private String action;
	
	private BigDecimal amount;
	
	private String address;
	
	private String cid;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public MerchantSession setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	public String getOtp() {
		return otp;
	}

	public MerchantSession setOtp(String otp) {
		this.otp = otp;
		return this;
	}

	public String getSessionToken() {
		return sessionToken;
	}

	public MerchantSession setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
		return this;
	}

	public String getAction() {
		return action;
	}

	public MerchantSession setAction(String action) {
		this.action = action;
		return this;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public MerchantSession setAmount(BigDecimal amount) {
		this.amount = amount;
		return this;
	}

	public String getAddress() {
		return address;
	}

	public MerchantSession setAddress(String address) {
		this.address = address;
		return this;
	}

	public String getCid() {
		return cid;
	}

	public MerchantSession setCid(String cid) {
		this.cid = cid;
		return this;
	}

}
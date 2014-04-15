package com._37coins.web;


public class MerchantSession {
	
	private String phoneNumber;
	
	private String sessionToken;
	
	private String action;
	
	private String callAction;
	
	private String delivery;
	
	private String deliveryParam;
	
	private String apiToken;
	
	private String apiSecret;


	public String getPhoneNumber() {
		return phoneNumber;
	}

	public MerchantSession setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	public String getCallAction() {
		return callAction;
	}

	public MerchantSession setCallAction(String callAction) {
		this.callAction = callAction;
		return this;
	}

	public String getDelivery() {
		return delivery;
	}

	public MerchantSession setDelivery(String delivery) {
		this.delivery = delivery;
		return this;
	}

	public String getDeliveryParam() {
		return deliveryParam;
	}

	public MerchantSession setDeliveryParam(String deliveryParam) {
		this.deliveryParam = deliveryParam;
		return this;
	}

	public String getApiToken() {
		return apiToken;
	}

	public MerchantSession setApiToken(String apiToken) {
		this.apiToken = apiToken;
		return this;
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public MerchantSession setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
		return this;
	}

}
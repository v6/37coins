package com._37coins.web;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Used for socketIo communication and MerchantResource.
 * 
 * @author johann
 *
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class MerchantSession implements Serializable{
    private static final long serialVersionUID = -7672427263706793882L;

    private String phoneNumber;
	
	private String displayName;
	
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
	
	

	public String getDisplayName() {
		return displayName;
	}

	public MerchantSession setDisplayName(String displayName) {
		this.displayName = displayName;
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
package com._37coins.merchant.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class MerchantResponse {
	
	private String displayName;
	private String gateway;
	private Long timout;
	private String token;
	
	
	public String getDisplayName() {
		return displayName;
	}
	public MerchantResponse setDisplayName(String displayName) {
		this.displayName = displayName;
		return this;
	}
	public String getGateway() {
		return gateway;
	}
	public MerchantResponse setGateway(String gateway) {
		this.gateway = gateway;
		return this;
	}
	public Long getTimout() {
		return timout;
	}
	public MerchantResponse setTimout(Long timout) {
		this.timout = timout;
		return this;
	}
	public String getToken() {
		return token;
	}
	public MerchantResponse setToken(String token) {
		this.token = token;
		return this;
	}

}

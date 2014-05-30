package com._37coins.merchant.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Timeout {
	
	private Long seconds;
	
	private String callbackUrl;

	public Long getSeconds() {
		return seconds;
	}

	public Timeout setSeconds(Long seconds) {
		this.seconds = seconds;
		return this;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public Timeout setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
		return this;
	}

}

package com._37coins.bip38;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class StoreRequest {
	
	private String encPrivKey;
	
	private Integer identifier;
	
	private String uri;
	
	private String password;

	public String getEncPrivKey() {
		return encPrivKey;
	}

	public StoreRequest setEncPrivKey(String encPrivKey) {
		this.encPrivKey = encPrivKey;
		return this;
	}

	public Integer getIdentifier() {
		return identifier;
	}

	public StoreRequest setIdentifier(Integer identifier) {
		this.identifier = identifier;
		return this;
	}

	public String getUri() {
		return uri;
	}

	public StoreRequest setUri(String uri) {
		this.uri = uri;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public StoreRequest setPassword(String password) {
		this.password = password;
		return this;
	}
	
	

}

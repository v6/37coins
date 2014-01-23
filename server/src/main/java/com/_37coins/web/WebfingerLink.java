package com._37coins.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class WebfingerLink {
	
	private String rel;
	private String type;
	private String href;
	
	public String getRel() {
		return rel;
	}
	public WebfingerLink setRel(String rel) {
		this.rel = rel;
		return this;
	}
	public String getType() {
		return type;
	}
	public WebfingerLink setType(String type) {
		this.type = type;
		return this;
	}
	public String getHref() {
		return href;
	}
	public WebfingerLink setHref(String href) {
		this.href = href;
		return this;
	}
}

package com._37coins.web;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class WebfingerResponse {
	
	private String subject;
	
	private Map<String,String> properties;
	
	private List<WebfingerLink> links;

	public String getSubject() {
		return subject;
	}

	public WebfingerResponse setSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public WebfingerResponse setProperties(Map<String, String> properties) {
		this.properties = properties;
		return this;
	}

	public List<WebfingerLink> getLinks() {
		return links;
	}

	public WebfingerResponse setLinks(List<WebfingerLink> links) {
		this.links = links;
		return this;
	}
	

}

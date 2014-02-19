package com._37coins.workflow.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EmailFactor {
	
	private String emailToken;
	private String smsToken;
	private String email;	
	private String cn;
	private String taksToken;
	
	public String getEmailToken() {
		return emailToken;
	}
	public EmailFactor setEmailToken(String emailToken) {
		this.emailToken = emailToken;
		return this;
	}
	public String getSmsToken() {
		return smsToken;
	}
	public EmailFactor setSmsToken(String smsToken) {
		this.smsToken = smsToken;
		return this;
	}
	public String getEmail() {
		return email;
	}
	public EmailFactor setEmail(String email) {
		this.email = email;
		return this;
	}
	public String getCn() {
		return cn;
	}
	public EmailFactor setCn(String cn) {
		this.cn = cn;
		return this;
	}
	public String getTaksToken() {
		return taksToken;
	}
	public EmailFactor setTaksToken(String taksToken) {
		this.taksToken = taksToken;
		return this;
	}
}

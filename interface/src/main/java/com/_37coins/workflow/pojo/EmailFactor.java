package com._37coins.workflow.pojo;

import javax.mail.internet.InternetAddress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EmailFactor {
	
	private String emailToken;
	private String smsToken;
	private InternetAddress email;	
	private String cn;
	
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
	public InternetAddress getEmail() {
		return email;
	}
	public EmailFactor setEmail(InternetAddress email) {
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
}

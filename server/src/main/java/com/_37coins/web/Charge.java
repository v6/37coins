package com._37coins.web;

import java.math.BigDecimal;

import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class Charge {
	
	private BigDecimal amount;
	
	private PhoneNumber source;
	
	private String token;

	public BigDecimal getAmount() {
		return amount;
	}

	public Charge setAmount(BigDecimal amount) {
		this.amount = amount;
		return this;
	}

	public PhoneNumber getSource() {
		return source;
	}

	public Charge setSource(PhoneNumber source) {
		this.source = source;
		return this;
	}

	public String getToken() {
		return token;
	}

	public Charge setToken(String token) {
		this.token = token;
		return this;
	}

}

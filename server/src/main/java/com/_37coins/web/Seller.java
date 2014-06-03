package com._37coins.web;

import java.io.Serializable;

public class Seller implements Serializable{
    private static final long serialVersionUID = 8042302137934743066L;

    private String mobile;
	
	private float price;

	public String getMobile() {
		return mobile;
	}

	public Seller setMobile(String mobile) {
		this.mobile = mobile;
		return this;
	}

	public float getPrice() {
		return price;
	}

	public Seller setPrice(float price) {
		this.price = price;
		return this;
	}

}

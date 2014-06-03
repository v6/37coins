package com._37coins.web;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class Transaction implements Serializable{
    private static final long serialVersionUID = -8244388150818363003L;

    public enum State {
		//REQUESTS
		STARTED("started"),
		CONFIRMED("confirmed"),
		COMPLETED("completed");

		private String text;

		State(String text) {
			this.text = text;
		}

		@JsonValue
		final String value() {
			return this.text;
		}

		public String getText() {
			return this.text;
		}

		@JsonCreator
		public static State fromString(String text) {
			if (text != null) {
				for (State b : State.values()) {
					if (text.equalsIgnoreCase(b.text)) {
						return b;
					}
				}
			}
			return null;
		}
	}
	
	static public String generateKey(){
		return RandomStringUtils.random(1, "adgjmptw")+RandomStringUtils.random(4, "0123456789");
	}
	
	private String key;
	
	private String taskToken;
	
	private State state;
	
	public String getKey() {
		return key;
	}

	public Transaction setKey(String key) {
		this.key = key;
		return this;
	}

	public String getTaskToken() {
		try {
			return URLDecoder.decode(taskToken, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Transaction setTaskToken(String taskToken) {
		try {
			this.taskToken = URLEncoder.encode(taskToken,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return this;
	}

	public State getState() {
		return state;
	}

	public Transaction setState(State state) {
		this.state = state;
		return this;
	}
	
}

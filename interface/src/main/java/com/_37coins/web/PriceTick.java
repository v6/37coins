package com._37coins.web;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PriceTick implements Serializable {
    private static final long serialVersionUID = 3629300805101564200L;
    
    private BigDecimal ask;
	private BigDecimal bid;
	private BigDecimal last;
	private BigDecimal lastFactored;
	private Date timestamp;
	private Float volume_btc;
	private Float volume_percent;
	private String curCode;
	
	@JsonCreator
	public static PriceTick create(String jsonString){
		return null;
	}
	
	public BigDecimal getAsk() {
		return ask;
	}
	public PriceTick setAsk(BigDecimal ask) {
		this.ask = ask;
		return this;
	}
	public BigDecimal getBid() {
		return bid;
	}
	public PriceTick setBid(BigDecimal bid) {
		this.bid = bid;
		return this;
	}
	public BigDecimal getLast() {
		return last;
	}
	public PriceTick setLast(BigDecimal last) {
		this.last = last;
		return this;
	}
	public BigDecimal getLastFactored() {
		return lastFactored;
	}
	public PriceTick setLastFactored(BigDecimal lastFactored) {
		this.lastFactored = lastFactored;
		return this;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public PriceTick setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	public Float getVolume_btc() {
		return volume_btc;
	}
	public PriceTick setVolume_btc(Float volume_btc) {
		this.volume_btc = volume_btc;
		return this;
	}
	public Float getVolume_percent() {
		return volume_percent;
	}
	public PriceTick setVolume_percent(Float volume_percent) {
		this.volume_percent = volume_percent;
		return this;
	}
	public String getCurCode() {
		return curCode;
	}
	public PriceTick setCurCode(String curCode) {
		this.curCode = curCode;
		return this;
	}
	
}

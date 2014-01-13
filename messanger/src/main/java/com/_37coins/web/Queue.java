package com._37coins.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Queue {
	
	private Integer consumers;
	private String name;
	private String vhost;
	private Boolean durable;
	private Boolean auto_delete;
	private String node;
	
	public Integer getConsumers() {
		return consumers;
	}
	public Queue setConsumers(Integer consumers) {
		this.consumers = consumers;
		return this;
	}
	public String getName() {
		return name;
	}
	public Queue setName(String name) {
		this.name = name;
		return this;
	}
	public String getVhost() {
		return vhost;
	}
	public Queue setVhost(String vhost) {
		this.vhost = vhost;
		return this;
	}
	public Boolean getDurable() {
		return durable;
	}
	public Queue setDurable(Boolean durable) {
		this.durable = durable;
		return this;
	}
	public Boolean getAuto_delete() {
		return auto_delete;
	}
	public Queue setAuto_delete(Boolean auto_delete) {
		this.auto_delete = auto_delete;
		return this;
	}
	public String getNode() {
		return node;
	}
	public Queue setNode(String node) {
		this.node = node;
		return this;
	}

}

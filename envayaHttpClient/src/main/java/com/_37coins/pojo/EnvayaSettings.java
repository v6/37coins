package com._37coins.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EnvayaSettings {
    
    private Boolean enabled;
    private String server_url;
    private String phone_number;
    private String password;
    private Integer outgoing_interval;
    private Boolean keep_in_inbox;
    private Boolean call_notifications;
    private Boolean forward_sent;
    private Boolean network_failover;
    private Boolean test_mode;
    private Boolean auto_add_test_number;
    private Boolean ignore_shortcodes;
    private Boolean ignore_non_numeric;
    private Boolean amqp_enabled;
    private Integer amqp_port;
    private Integer amqp_vhost;
    private Boolean amqp_ssl;
    private String amqp_user;
    private String amqp_password;
    private String amqp_queue;
    private Integer amqp_heartbeat;
    private Integer market_version;
    private String market_version_name;
    private Integer settings_version;
    public Boolean getEnabled() {
        return enabled;
    }
    public EnvayaSettings setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    public String getServer_url() {
        return server_url;
    }
    public EnvayaSettings setServer_url(String server_url) {
        this.server_url = server_url;
        return this;
    }
    public String getPhone_number() {
        return phone_number;
    }
    public EnvayaSettings setPhone_number(String phone_number) {
        this.phone_number = phone_number;
        return this;
    }
    public String getPassword() {
        return password;
    }
    public EnvayaSettings setPassword(String password) {
        this.password = password;
        return this;
    }
    public Integer getOutgoing_interval() {
        return outgoing_interval;
    }
    public EnvayaSettings setOutgoing_interval(Integer outgoing_interval) {
        this.outgoing_interval = outgoing_interval;
        return this;
    }
    public Boolean getKeep_in_inbox() {
        return keep_in_inbox;
    }
    public EnvayaSettings setKeep_in_inbox(Boolean keep_in_inbox) {
        this.keep_in_inbox = keep_in_inbox;
        return this;
    }
    public Boolean getCall_notifications() {
        return call_notifications;
    }
    public EnvayaSettings setCall_notifications(Boolean call_notifications) {
        this.call_notifications = call_notifications;
        return this;
    }
    public Boolean getForward_sent() {
        return forward_sent;
    }
    public EnvayaSettings setForward_sent(Boolean forward_sent) {
        this.forward_sent = forward_sent;
        return this;
    }
    public Boolean getNetwork_failover() {
        return network_failover;
    }
    public EnvayaSettings setNetwork_failover(Boolean network_failover) {
        this.network_failover = network_failover;
        return this;
    }
    public Boolean getTest_mode() {
        return test_mode;
    }
    public EnvayaSettings setTest_mode(Boolean test_mode) {
        this.test_mode = test_mode;
        return this;
    }
    public Boolean getAuto_add_test_number() {
        return auto_add_test_number;
    }
    public EnvayaSettings setAuto_add_test_number(Boolean auto_add_test_number) {
        this.auto_add_test_number = auto_add_test_number;
        return this;
    }
    public Boolean getIgnore_shortcodes() {
        return ignore_shortcodes;
    }
    public EnvayaSettings setIgnore_shortcodes(Boolean ignore_shortcodes) {
        this.ignore_shortcodes = ignore_shortcodes;
        return this;
    }
    public Boolean getIgnore_non_numeric() {
        return ignore_non_numeric;
    }
    public EnvayaSettings setIgnore_non_numeric(Boolean ignore_non_numeric) {
        this.ignore_non_numeric = ignore_non_numeric;
        return this;
    }
    public Boolean getAmqp_enabled() {
        return amqp_enabled;
    }
    public EnvayaSettings setAmqp_enabled(Boolean amqp_enabled) {
        this.amqp_enabled = amqp_enabled;
        return this;
    }
    public Integer getAmqp_port() {
        return amqp_port;
    }
    public EnvayaSettings setAmqp_port(Integer amqp_port) {
        this.amqp_port = amqp_port;
        return this;
    }
    public Integer getAmqp_vhost() {
        return amqp_vhost;
    }
    public EnvayaSettings setAmqp_vhost(Integer amqp_vhost) {
        this.amqp_vhost = amqp_vhost;
        return this;
    }
    public Boolean getAmqp_ssl() {
        return amqp_ssl;
    }
    public EnvayaSettings setAmqp_ssl(Boolean amqp_ssl) {
        this.amqp_ssl = amqp_ssl;
        return this;
    }
    public String getAmqp_user() {
        return amqp_user;
    }
    public EnvayaSettings setAmqp_user(String amqp_user) {
        this.amqp_user = amqp_user;
        return this;
    }
    public String getAmqp_password() {
        return amqp_password;
    }
    public EnvayaSettings setAmqp_password(String amqp_password) {
        this.amqp_password = amqp_password;
        return this;
    }
    public String getAmqp_queue() {
        return amqp_queue;
    }
    public EnvayaSettings setAmqp_queue(String amqp_queue) {
        this.amqp_queue = amqp_queue;
        return this;
    }
    public Integer getAmqp_heartbeat() {
        return amqp_heartbeat;
    }
    public EnvayaSettings setAmqp_heartbeat(Integer amqp_heartbeat) {
        this.amqp_heartbeat = amqp_heartbeat;
        return this;
    }
    public Integer getMarket_version() {
        return market_version;
    }
    public EnvayaSettings setMarket_version(Integer market_version) {
        this.market_version = market_version;
        return this;
    }
    public String getMarket_version_name() {
        return market_version_name;
    }
    public EnvayaSettings setMarket_version_name(String market_version_name) {
        this.market_version_name = market_version_name;
        return this;
    }
    public Integer getSettings_version() {
        return settings_version;
    }
    public EnvayaSettings setSettings_version(Integer settings_version) {
        this.settings_version = settings_version;
        return this;
    }
    
    

}

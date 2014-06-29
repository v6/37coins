package com._37coins.pojo;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EnvayaRequest {

    public static EnvayaRequest fromBody(InputStream inputStream) throws UnsupportedEncodingException{
        Map<String, String> map = new HashMap<>();
        String body = convertStreamToString(inputStream);
        String[] pairs = body.split("\\&");
        for (int i=0; i<pairs.length; i++) {
            String[] fields = pairs[i].split("=");
            String name = URLDecoder.decode(fields[0], "UTF-8");
            String value = (fields.length>1)?URLDecoder.decode(fields[1], "UTF-8"):"";
            map.put(name, value);
        }
        EnvayaRequest er = new EnvayaRequest();
        for (Entry<String,String> e : map.entrySet()){
            if (e.getKey().equals(VERSION)) er.setVersion(Integer.parseInt(e.getValue()));
            if (e.getKey().equals(PHONE_NUMBER)) er.setPhoneNumber((e.getValue()));
            if (e.getKey().equals(LOG)) er.setLog((e.getValue()));
            if (e.getKey().equals(NETWORK)) er.setNetwork((e.getValue()));
            if (e.getKey().equals(SETTINGS_VERION)) er.setSettingsVersion(Integer.parseInt(e.getValue()));
            if (e.getKey().equals(NOW)) er.setNow(Long.parseLong(e.getValue()));
            if (e.getKey().equals(BATTERY)) er.setBattery(Integer.parseInt(e.getValue()));
            if (e.getKey().equals(POWER)) er.setPower(Integer.parseInt(e.getValue()));
            if (e.getKey().equals(FROM)) er.setFrom((e.getValue()));
            if (e.getKey().equals(ACTION)) er.setAction(Action.fromString(e.getValue()));
            if (e.getKey().equals(MESSAGE_TYPE)) er.setMessageType(MessageType.fromString(e.getValue()));
            if (e.getKey().equals(MESSAGE)) er.setMessage((e.getValue()));
            if (e.getKey().equals(TIMESTAMP)) er.setTimestamp(Long.parseLong(e.getValue()));
            if (e.getKey().equals(PHONE_ID)) er.setPhoneId(e.getValue());
            if (e.getKey().equals(PHONE_TOKEN)) er.setPhoneToken(e.getValue());
            if (e.getKey().equals(SEND_LIMIT)) er.setSendLimit(Integer.parseInt(e.getValue()));
            if (e.getKey().equals(MMS_PARTS)) throw new RuntimeException("not implemented");
            if (e.getKey().equals(ID)) er.setId(e.getValue());
            if (e.getKey().equals(STATUS)) er.setStatus(Status.fromString(e.getValue()));
            if (e.getKey().equals(ERROR)) er.setError((e.getValue()));
            if (e.getKey().equals(EVENT)) throw new RuntimeException("not implemented");
            if (e.getKey().equals(TO)) er.setTo(e.getValue());
            if (e.getKey().equals(CONSUMER_TAG)) er.setConsumerTag(e.getValue());
        }
        return er;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    public enum MessageType {
        SMS("sms"),
        MMS("mms"),
        CALL("call");

        private String text;

        MessageType(String text) {
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
        public static MessageType fromString(String text) {
            if (text != null) {
                for (MessageType b : MessageType.values()) {
                    if (text.equalsIgnoreCase(b.text)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }
    
    public enum Status {
        QUEUED("queued"),
        FAILED("failed"),
        CANCELLED("cancelled"),
        SENT("sent"),
        POWER_CONNECTED("power_connected"),
        POWER_DISCONNECTED("power_disconnected"),
        BATTERY_LOW("battery_low"),
        BATTERY_OKAY("battery_okay");
        private String text;

        Status(String text) {
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
        public static Status fromString(String text) {
            if (text != null) {
                for (Status b : Status.values()) {
                    if (text.equalsIgnoreCase(b.text)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }
    
    public enum Action {
        OUTGOING("outgoing"),
        INCOMING("incoming"),
        SEND_STATUS("send_status"),
        DEVICE_STATUS("device_status"),
        TEST("test"),
        AMQP_STARTED("amqp_started"),
        FORWARD_SEND("forward_sent");

        private String text;

        Action(String text) {
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
        public static Action fromString(String text) {
            if (text != null) {
                for (Action b : Action.values()) {
                    if (text.equalsIgnoreCase(b.text)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }
    
    public final static String VERSION = "version";
    public final static String PHONE_NUMBER = "phone_number";
    public final static String LOG = "log";
    public final static String NETWORK = "network";
    public final static String SETTINGS_VERION = "settings_version";
    public final static String NOW = "now";
    public final static String BATTERY = "battery";
    public final static String POWER = "power";
    public final static String FROM = "from";
    public final static String ACTION = "action";
    public final static String MESSAGE_TYPE = "message_type"; 
    public final static String MESSAGE = "message";
    public final static String TIMESTAMP = "timestamp";
    public final static String MMS_PARTS = "mms_parts";
    public final static String ID = "id";
    public final static String STATUS = "status";
    public final static String ERROR = "error";
    public final static String EVENT = "event";
    public final static String PHONE_ID = "phone_id";
    public final static String PHONE_TOKEN = "phone_token";
    public final static String SEND_LIMIT = "send_limit";
    public final static String TO = "to";
    public final static String CONSUMER_TAG = "consumer_tag";
    
    private Integer version;
    private String phoneNumber;
    private String log;
    private String network;
    private Integer settingsVersion;
    private Long now;
    private Integer battery;
    private Integer power;
    private Integer sendLimit;
    private Action action;
    private String from;
    private String phoneId;
    private String phoneToken;
    private MessageType messageType;
    private String message;
    private Long timestamp;
    private List<EnvayaMmsPart> mmsParts;
    private String id;
    private Status status;
    private String error;
    private String to;
    private String consumerTag;
    
    
    
    public Integer getVersion() {
        return version;
    }

    public EnvayaRequest setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @JsonProperty(EnvayaRequest.PHONE_NUMBER)
    public EnvayaRequest setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getLog() {
        return log;
    }

    public EnvayaRequest setLog(String log) {
        this.log = log;
        return this;
    }

    public String getNetwork() {
        return network;
    }

    public EnvayaRequest setNetwork(String network) {
        this.network = network;
        return this;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public EnvayaRequest setPhoneId(String phoneId) {
        this.phoneId = phoneId;
        return this;
    }

    public String getPhoneToken() {
        return phoneToken;
    }

    public EnvayaRequest setPhoneToken(String phoneToken) {
        this.phoneToken = phoneToken;
        return this;
    }

    public Integer getSettingsVersion() {
        return settingsVersion;
    }

    @JsonProperty(EnvayaRequest.SETTINGS_VERION)
    public EnvayaRequest setSettingsVersion(Integer settingsVersion) {
        this.settingsVersion = settingsVersion;
        return this;
    }

    public Long getNow() {
        return now;
    }

    public EnvayaRequest setNow(Long now) {
        this.now = now;
        return this;
    }

    public Integer getBattery() {
        return battery;
    }

    public EnvayaRequest setBattery(Integer battery) {
        this.battery = battery;
        return this;
    }

    public Integer getPower() {
        return power;
    }

    public EnvayaRequest setPower(Integer power) {
        this.power = power;
        return this;
    }
    
    public Integer getSendLimit() {
        return sendLimit;
    }

    public EnvayaRequest setSendLimit(Integer sendLimit) {
        this.sendLimit = sendLimit;
        return this;
    }    

    public Action getAction() {
        return action;
    }

    public EnvayaRequest setAction(Action action) {
        this.action = action;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public EnvayaRequest setFrom(String from) {
        this.from = from;
        return this;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    @JsonProperty(EnvayaRequest.MESSAGE_TYPE)
    public EnvayaRequest setMessageType(MessageType messageType) {
        this.messageType = messageType;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public EnvayaRequest setMessage(String message) {
        this.message = message;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public EnvayaRequest setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public List<EnvayaMmsPart> getMmsParts() {
        return mmsParts;
    }

    @JsonProperty(EnvayaRequest.MMS_PARTS)
    public EnvayaRequest setMmsParts(List<EnvayaMmsPart> mmsParts) {
        this.mmsParts = mmsParts;
        return this;
    }

    public String getId() {
        return id;
    }

    public EnvayaRequest setId(String id) {
        this.id = id;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public EnvayaRequest setStatus(Status status) {
        this.status = status;
        return this;
    }

    public String getError() {
        return error;
    }

    public EnvayaRequest setError(String error) {
        this.error = error;
        return this;
    }

    public String getTo() {
        return to;
    }

    public EnvayaRequest setTo(String to) {
        this.to = to;
        return this;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    @JsonProperty(EnvayaRequest.CONSUMER_TAG)
    public EnvayaRequest setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
        return this;
    }

    @JsonIgnore
    public List<NameValuePair> toMap() throws UnsupportedEncodingException{
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if (null!=getAction()) nvps.add(new BasicNameValuePair(EnvayaRequest.ACTION, getAction().getText()));
        if (null!=getBattery()) nvps.add(new BasicNameValuePair(EnvayaRequest.BATTERY, getBattery().toString()));
        if (null!=getNow()) nvps.add(new BasicNameValuePair(EnvayaRequest.NOW, getNow().toString()));
        if (null!=getPower()) nvps.add(new BasicNameValuePair(EnvayaRequest.POWER, getPower().toString()));
        if (null!=getSettingsVersion()) nvps.add(new BasicNameValuePair(EnvayaRequest.SETTINGS_VERION, getSettingsVersion().toString()));
        if (null!=getTimestamp()) nvps.add(new BasicNameValuePair(EnvayaRequest.TIMESTAMP, getTimestamp().toString()));
        if (null!=getVersion()) nvps.add(new BasicNameValuePair(EnvayaRequest.VERSION, getVersion().toString()));
        if (null!=getConsumerTag()) nvps.add(new BasicNameValuePair(EnvayaRequest.CONSUMER_TAG, getConsumerTag()));
        if (null!=getError()) nvps.add(new BasicNameValuePair(EnvayaRequest.ERROR, getError()));
        if (null!=getFrom()) nvps.add(new BasicNameValuePair(EnvayaRequest.FROM, getFrom()));
        if (null!=getId()) nvps.add(new BasicNameValuePair(EnvayaRequest.ID, getId()));
        if (null!=getLog()) nvps.add(new BasicNameValuePair(EnvayaRequest.LOG, getLog()));
        if (null!=getMessage()) nvps.add(new BasicNameValuePair(EnvayaRequest.MESSAGE, getMessage()));
        if (null!=getPhoneId()) nvps.add(new BasicNameValuePair(EnvayaRequest.PHONE_ID, getPhoneId()));
        if (null!=getPhoneToken()) nvps.add(new BasicNameValuePair(EnvayaRequest.PHONE_TOKEN, getPhoneToken()));
        if (null!=getSendLimit()) nvps.add(new BasicNameValuePair(EnvayaRequest.SEND_LIMIT, getSendLimit().toString()));
        if (null!=getMessageType()) nvps.add(new BasicNameValuePair(EnvayaRequest.MESSAGE_TYPE, getMessageType().getText()));
        if (null!=getNetwork()) nvps.add(new BasicNameValuePair(EnvayaRequest.NETWORK, getNetwork()));
        if (null!=getPhoneNumber()) nvps.add(new BasicNameValuePair(EnvayaRequest.PHONE_NUMBER, getPhoneNumber()));
        if (null!=getStatus()) nvps.add(new BasicNameValuePair(EnvayaRequest.STATUS, getStatus().getText()));
        if (null!=getTo()) nvps.add(new BasicNameValuePair(EnvayaRequest.TO, getTo()));
        return nvps;
    }
    
    @JsonIgnore    
    public UrlEncodedFormEntity toBody() throws UnsupportedEncodingException{
        return new UrlEncodedFormEntity(toMap());
    }

}

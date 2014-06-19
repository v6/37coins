package com._37coins.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EnvayaMessage {
    
    private String id;
    private String to;
    private Integer priority;
    private String message;
    
    public String getId() {
        return id;
    }
    public EnvayaMessage setId(String id) {
        this.id = id;
        return this;
    }
    public String getTo() {
        return to;
    }
    public EnvayaMessage setTo(String to) {
        this.to = to;
        return this;
    }
    public Integer getPriority() {
        return priority;
    }
    public EnvayaMessage setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }
    public String getMessage() {
        return message;
    }
    public EnvayaMessage setMessage(String message) {
        this.message = message;
        return this;
    }

}

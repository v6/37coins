package com._37coins.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EnvayaError {
    
    private String message;

    public String getMessage() {
        return message;
    }

    public EnvayaError setMessage(String message) {
        this.message = message;
        return this;
    }

}

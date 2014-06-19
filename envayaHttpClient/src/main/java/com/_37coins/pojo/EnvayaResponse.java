package com._37coins.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EnvayaResponse {
    
    private List<EnvayaEvent> events;
    private EnvayaError error;
    
    public List<EnvayaEvent> getEvents() {
        return events;
    }
    public EnvayaResponse setEvents(List<EnvayaEvent> events) {
        this.events = events;
        return this;
    }
    public EnvayaError getError() {
        return error;
    }
    public EnvayaResponse setError(EnvayaError error) {
        this.error = error;
        return this;
    }

}

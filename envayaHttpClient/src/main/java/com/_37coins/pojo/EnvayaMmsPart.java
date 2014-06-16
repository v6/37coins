package com._37coins.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class EnvayaMmsPart {
    
    private String name;
    private String cid;
    private String type;
    private String filename;
    
    public String getName() {
        return name;
    }
    public EnvayaMmsPart setName(String name) {
        this.name = name;
        return this;
    }
    public String getCid() {
        return cid;
    }
    public EnvayaMmsPart setCid(String cid) {
        this.cid = cid;
        return this;
    }
    public String getType() {
        return type;
    }
    public EnvayaMmsPart setType(String type) {
        this.type = type;
        return this;
    }
    public String getFilename() {
        return filename;
    }
    public EnvayaMmsPart setFilename(String filename) {
        this.filename = filename;
        return this;
    }

}

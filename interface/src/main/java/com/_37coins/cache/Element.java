package com._37coins.cache;



public class Element {
    
    final private Object object;
    final private String key;
    private boolean expired;
    private long creationTime;
    

    public Element(String key, Object object) {
        this.object = object;
        this.key = key;
    }

    public Object getObjectValue() {
        return object;
    }

    public String getKey() {
        return key;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public Object getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

}

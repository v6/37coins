package com._37coins;


public class EnvayaClientException extends Exception{
    private static final long serialVersionUID = 3688690739732190997L;
    
    private Reason reason;

    public EnvayaClientException() {
    }

    public EnvayaClientException(Reason reason) {
        this.reason = reason;
    }

    public EnvayaClientException(Reason reason, Throwable cause) {
        super(cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "ProductsClientException{" +
                "reason=" + reason +
                '}';
    }

    public enum Reason {
        INVALID_URI,
        ERROR_GETTING_RESOURCE,
        ERROR_PARSING,
        AUTHENTICATION_FAILED
    }

}

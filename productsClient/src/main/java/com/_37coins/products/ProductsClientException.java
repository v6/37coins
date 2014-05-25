package com._37coins.products;


public class ProductsClientException extends Exception{
    private static final long serialVersionUID = -8827307266482255341L;
    
    private Reason reason;

    public ProductsClientException() {
    }

    public ProductsClientException(Reason reason) {
        this.reason = reason;
    }

    public ProductsClientException(Reason reason, Throwable cause) {
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

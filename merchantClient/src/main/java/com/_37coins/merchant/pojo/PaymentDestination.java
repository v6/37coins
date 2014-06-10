package com._37coins.merchant.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PaymentDestination {
    
    public enum AddressType {
        BTC, 
        ACCOUNT;
    }
    
    private String address;
    
    private AddressType addressType;

    public String getAddress() {
        return address;
    }

    public PaymentDestination setAddress(String address) {
        this.address = address;
        return this;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public PaymentDestination setAddressType(AddressType addressType) {
        this.addressType = addressType;
        return this;
    }

}

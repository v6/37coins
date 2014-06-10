package com._37coins.helper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import com._37coins.merchant.MerchantClient;
import com._37coins.merchant.MerchantClientException;
import com._37coins.merchant.pojo.Charge;
import com._37coins.merchant.pojo.MerchantResponse;
import com._37coins.merchant.pojo.PaymentDestination;
import com._37coins.merchant.pojo.PaymentDestination.AddressType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class MockMerchantClient extends MerchantClient {
    
    public MockMerchantClient(String uri, String digestToken) {
        super(uri, digestToken);
        // TODO Auto-generated constructor stub
    }

    private BigDecimal cAmount;
    private BigDecimal pAmount;
    
    
    
    @Override
    public MerchantResponse charge(BigDecimal amount, PhoneNumber phoneNumber)
            throws MerchantClientException, NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException {
        cAmount = amount;
        return new MerchantResponse().setToken("ab").setDisplayName("merchant");
    }
    
    @Override
    public MerchantResponse product(BigDecimal amount, PhoneNumber phoneNumber)
            throws MerchantClientException, NoSuchAlgorithmException,
            UnsupportedEncodingException, IOException {
        pAmount = amount;
        return new MerchantResponse().setToken("cd").setDisplayName("merchant");
    }
    
    @Override
    public Charge getCharge(String token) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, URISyntaxException,
            MerchantClientException {
        if (token.equals("ab")){
            return new Charge().setAmount(cAmount).setComment("comment").setPayDest(new PaymentDestination().setAddress("491696941382").setAddressType(AddressType.ACCOUNT));
        }else{
            return new Charge().setAmount(pAmount).setComment("comment").setPayDest(new PaymentDestination().setAddress("491696941382").setAddressType(AddressType.ACCOUNT));
        }
    }

}

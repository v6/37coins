package com._37coins.merchant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.restnucleus.filter.DigestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.merchant.pojo.Charge;
import com._37coins.merchant.pojo.MerchantRequest;
import com._37coins.merchant.pojo.MerchantResponse;
import com._37coins.merchant.pojo.PaymentDestination;
import com._37coins.merchant.pojo.PaymentDestination.AddressType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class MerchantClient {
    public static final String PHONE_REGEX = "^(\\+|\\d)[0-9]{7,16}$";
    private static final String CHARGE_PATH = "/charge";
    private static final String PRODUCT_PATH = "/product";
    private static final Logger log = LoggerFactory.getLogger(MerchantClient.class);

    private HttpClient httpClient;
    private String uri;
    private String digestToken;

    public MerchantClient(String uri, String digestToken){
        this(uri, digestToken, HttpClientFactory.getClientBuilder().build());
    }

    public MerchantClient(String uri, String digestToken, HttpClient httpClient) {
        this.uri = uri;
        this.digestToken = digestToken;
        this.httpClient = httpClient;
    }
    
    protected <K> K parsePayload(HttpResponse response, Class<K> entityClass) throws MerchantClientException {
        try {
            return new ObjectMapper().readValue(response.getEntity().getContent(), entityClass);
        } catch (IOException e){
            log.error("products client error", e);
            throw new MerchantClientException(MerchantClientException.Reason.ERROR_PARSING, e);
        }
    }

    protected <K> K getPayload(HttpRequestBase request, Class<K> entityClass) throws MerchantClientException {
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            log.error("products client error", e);
            throw new MerchantClientException(MerchantClientException.Reason.ERROR_GETTING_RESOURCE, e);
        }

        if (Util.isSucceed(response) && request.getMethod()!="DELETE"){
            return parsePayload(response, entityClass);
        }else if (Util.isSucceed(response)){
            return null;
        }else{
            throw new MerchantClientException(MerchantClientException.Reason.AUTHENTICATION_FAILED);
        }
    }
    
    public MerchantResponse charge(BigDecimal amount, String destination) throws MerchantClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException, NumberParseException {
        return charge(amount, destination, null);
    }
    
    public MerchantResponse charge(BigDecimal amount, PhoneNumber phoneNumber) throws MerchantClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        return request(amount, phoneNumber, null, null, CHARGE_PATH);
    }
    
    public MerchantResponse charge(BigDecimal amount, String destination, String orderName) throws MerchantClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException, NumberParseException {
        if (destination.matches(PHONE_REGEX)) {
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            PhoneNumber phoneNumber = util.parse(destination, "ZZ");
            return request(amount, phoneNumber, null, orderName, CHARGE_PATH);
        }else{
            return request(amount, null, destination, orderName, CHARGE_PATH);
        }
    }
    
    public MerchantResponse product(BigDecimal amount, String destination) throws MerchantClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException, NumberParseException {
        return charge(amount, destination, null);
    }
    
    public MerchantResponse product(BigDecimal amount, PhoneNumber phoneNumber) throws MerchantClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        return request(amount, phoneNumber, null, null, PRODUCT_PATH);
    }
    
    public MerchantResponse product(BigDecimal amount, String destination, String orderName) throws MerchantClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException, NumberParseException {
        if (destination.matches(PHONE_REGEX)) {
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            PhoneNumber phoneNumber = util.parse(destination, "ZZ");
            return request(amount, phoneNumber, null, orderName, PRODUCT_PATH);
        }else{
            return request(amount, null, destination, orderName, PRODUCT_PATH);
        }
    }
    
    protected MerchantResponse request(BigDecimal amount, PhoneNumber phoneNumber, String address, String orderName, String path) throws MerchantClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        HttpPost req = new HttpPost(uri + path);
        MerchantRequest charge = new MerchantRequest().setAmount(amount);
        if (null!=phoneNumber){
            String pn = PhoneNumberUtil.getInstance().format(phoneNumber, PhoneNumberFormat.E164);
            pn = pn.replace("+", "");
            charge.setPayDest(new PaymentDestination().setAddress(pn).setAddressType(AddressType.ACCOUNT));
        }else{
            charge.setPayDest(new PaymentDestination().setAddress(address).setAddressType(AddressType.BTC));
        }
        String reqValue = new ObjectMapper().writeValueAsString(charge);
        StringEntity entity = new StringEntity(reqValue, "UTF-8");
        entity.setContentType("application/json");
        String reqSig = DigestFilter.calculateSignature(
                uri+path,
                DigestFilter.parseJson(reqValue.getBytes()),
                digestToken);
        req.setHeader(DigestFilter.AUTH_HEADER, reqSig);
        req.setEntity(entity);
        MerchantResponse c = getPayload(req, MerchantResponse.class);
        return c;
    }
    
    public Charge getCharge(String token) throws NoSuchAlgorithmException, UnsupportedEncodingException, URISyntaxException, MerchantClientException{
        return get(token, CHARGE_PATH);
    }
    
    public Charge get(String token, String path) throws URISyntaxException, NoSuchAlgorithmException, UnsupportedEncodingException, MerchantClientException  {
        URIBuilder builder = new URIBuilder(uri+path);
        builder.setParameter("token", token);
        HttpGet req = new HttpGet(builder.build());
        String reqSig = DigestFilter.calculateSignature(
                req.getURI().toString(),
                new MultivaluedHashMap<String,String>(),
                digestToken);
        req.setHeader(DigestFilter.AUTH_HEADER, reqSig);
        Charge c = getPayload(req, Charge.class);
        return c;        
    }
    
    public void deleteCharge(String token) throws NoSuchAlgorithmException, UnsupportedEncodingException, URISyntaxException, MerchantClientException{
        delete(token, CHARGE_PATH);
    }
    
    public void delete(String token, String path) throws URISyntaxException, NoSuchAlgorithmException, UnsupportedEncodingException, MerchantClientException  {
        URIBuilder builder = new URIBuilder(uri+path);
        builder.setParameter("token", token);
        HttpDelete req = new HttpDelete(builder.build());
        String reqSig = DigestFilter.calculateSignature(
                req.getURI().toString(),
                new MultivaluedHashMap<String,String>(),
                digestToken);
        req.setHeader(DigestFilter.AUTH_HEADER, reqSig);
        getPayload(req, null);        
    }

}

package com._37coins.products;

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
import org.btc4all.webfinger.HttpClientFactory;
import org.btc4all.webfinger.util.Util;
import org.restnucleus.filter.DigestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Withdrawal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class ProductsClient {
    public static final String PHONE_REGEX = "^(\\+|\\d)[0-9]{7,16}$";
    private static final String CHARGE_PATH = "/charge";
    private static final Logger log = LoggerFactory.getLogger(ProductsClient.class);

    private HttpClient httpClient;
    private String uri;
    private String digestToken;

    public ProductsClient(String uri, String digestToken){
        this(uri, digestToken, HttpClientFactory.getClientBuilder().build());
    }

    protected ProductsClient(String uri, String digestToken, HttpClient httpClient) {
        this.uri = uri;
        this.digestToken = digestToken;
        this.httpClient = httpClient;
    }
    
    protected Withdrawal parsePayload(HttpResponse response) throws ProductsClientException {
        try {
            return new ObjectMapper().readValue(response.getEntity().getContent(), Withdrawal.class);
        } catch (IOException e){
            log.error("products client error", e);
            throw new ProductsClientException(ProductsClientException.Reason.ERROR_PARSING, e);
        }
    }

    protected Withdrawal getPayload(HttpRequestBase request) throws ProductsClientException {
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            log.error("products client error", e);
            throw new ProductsClientException(ProductsClientException.Reason.ERROR_GETTING_RESOURCE, e);
        }

        if (Util.isSucceed(response) && request.getMethod()!="DELETE"){
            return parsePayload(response);
        }else if (Util.isSucceed(response)){
            return null;
        }else{
            throw new ProductsClientException(ProductsClientException.Reason.AUTHENTICATION_FAILED);
        }
    }
    
    public String charge(BigDecimal amount, String destination) throws ProductsClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException, NumberParseException {
        return charge(amount, destination, null);
    }
    
    public String charge(BigDecimal amount, String destination, String orderName) throws ProductsClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException, NumberParseException {
        if (destination.matches(PHONE_REGEX)) {
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            PhoneNumber phoneNumber = util.parse(destination, "ZZ");
            return request(amount, phoneNumber, null, orderName, CHARGE_PATH);
        }else{
            return request(amount, null, destination, orderName, CHARGE_PATH);
        }
    }
    
    public String product(BigDecimal amount, PhoneNumber phoneNumber) throws ProductsClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException {        
        String path = "/product";
        return request(amount, phoneNumber, null, null, path);
    }
    
    public String request(BigDecimal amount, PhoneNumber phoneNumber, String address, String orderName, String path) throws ProductsClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        HttpPost req = new HttpPost(uri + path);
        Withdrawal charge = new Withdrawal().setAmount(amount);
        if (null!=phoneNumber){
            String pn = PhoneNumberUtil.getInstance().format(phoneNumber, PhoneNumberFormat.E164);
            pn = pn.replace("+", "");
            charge.setPayDest(new PaymentAddress().setAddress(pn).setAddressType(PaymentType.ACCOUNT));
        }else{
            charge.setPayDest(new PaymentAddress().setAddress(address).setAddressType(PaymentType.BTC));
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
        Withdrawal c = getPayload(req);
        return c.getTxId();
    }
    
    public Withdrawal getCharge(String token) throws NoSuchAlgorithmException, UnsupportedEncodingException, URISyntaxException, ProductsClientException{
        return get(token, CHARGE_PATH);
    }
    
    public Withdrawal get(String token, String path) throws URISyntaxException, NoSuchAlgorithmException, UnsupportedEncodingException, ProductsClientException  {
        URIBuilder builder = new URIBuilder(uri+path);
        builder.setParameter("token", token);
        HttpGet req = new HttpGet(builder.build());
        String reqSig = DigestFilter.calculateSignature(
                req.getURI().toString(),
                new MultivaluedHashMap<String,String>(),
                digestToken);
        req.setHeader(DigestFilter.AUTH_HEADER, reqSig);
        Withdrawal c = getPayload(req);
        return c;        
    }
    
    public void deleteCharge(String token) throws NoSuchAlgorithmException, UnsupportedEncodingException, URISyntaxException, ProductsClientException{
        delete(token, CHARGE_PATH);
    }
    
    public void delete(String token, String path) throws URISyntaxException, NoSuchAlgorithmException, UnsupportedEncodingException, ProductsClientException  {
        URIBuilder builder = new URIBuilder(uri+path);
        builder.setParameter("token", token);
        HttpDelete req = new HttpDelete(builder.build());
        String reqSig = DigestFilter.calculateSignature(
                req.getURI().toString(),
                new MultivaluedHashMap<String,String>(),
                digestToken);
        req.setHeader(DigestFilter.AUTH_HEADER, reqSig);
        getPayload(req);        
    }

}

package com._37coins;

import static com.jayway.restassured.RestAssured.given;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restnucleus.filter.HmacFilter;

import com._37coins.resources.HealthCheckResource;
import com._37coins.resources.ProductResource;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Withdrawal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class HmacTest {	
    private static EmbeddedJetty embeddedJetty;

    @BeforeClass
    public static void beforeClass() throws Exception {
        embeddedJetty = new EmbeddedJetty(){
        	@Override
        	public String setInitParam(ServletHolder holder) {
        		holder.setInitParameter("javax.ws.rs.Application", "com._37coins.TestApplication");
        		return "src/test/webapp";
        	}
        };
        embeddedJetty.start();
	}
    
    @AfterClass
    public static void afterClass() throws Exception {
        embeddedJetty.stop();
    }
    
    private ObjectMapper mapper;
    
    @Before
    public void testThis(){
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
    }
    
    public String json(Object o) throws IOException{
    	try {
			return new ObjectMapper().writeValueAsString(o);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    @Test
    public void testHealthcheck() throws IOException{
    	given()
		.expect()
			.statusCode(200)
		.when()
			.get(embeddedJetty.getBaseUri() + HealthCheckResource.PATH);
    }
    
    @Test
    public void testAuthFail() throws IOException, NoSuchAlgorithmException{
    	Withdrawal w = new Withdrawal().setAmount(new BigDecimal("0.5")).setComment("bla");
    	String serverUrl = embeddedJetty.getBaseUri() + ProductResource.PATH+"/charge";
		String sig = HmacFilter.calculateSignature(serverUrl, HmacFilter.parseJson(new ObjectMapper().writeValueAsBytes(w)), "james124");
    	given()
    		.contentType(ContentType.JSON)
    		.header("X-Request-Signature", sig)
    		.body(json(w))
		.expect()
			.statusCode(401)
		.when()
			.post(embeddedJetty.getBaseUri() + ProductResource.PATH+"/charge");
    	
    }
    	
    @Test
    public void testAuthPost() throws IOException, NoSuchAlgorithmException{
    	Withdrawal w = new Withdrawal().setAmount(new BigDecimal("0.5")).setComment("bla");
    	String serverUrl = embeddedJetty.getBaseUri() + ProductResource.PATH+"/charge";
    	w.setPayDest(new PaymentAddress().setAddressType(PaymentType.BTC).setAddress("123565"));
		String sig = HmacFilter.calculateSignature(serverUrl, HmacFilter.parseJson(new ObjectMapper().writeValueAsBytes(w)), ProductsServletConfig.hmacToken);
    	Response r = given()
    		.contentType(ContentType.JSON)
    		.header("X-Request-Signature", sig)
    		.body(json(w))
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ProductResource.PATH+"/charge");
    	w = new ObjectMapper().readValue(r.asInputStream(), Withdrawal.class);
    	
    	MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
    	sig = HmacFilter.calculateSignature(serverUrl+"?token="+w.getTxId(), map, ProductsServletConfig.hmacToken);
    	given()
			.header("X-Request-Signature", sig)
			.queryParam("token", w.getTxId())
		.expect()
			.statusCode(200)
		.when()
			.get(embeddedJetty.getBaseUri() + ProductResource.PATH+"/charge");
    	
    	given()
			.header("X-Request-Signature", sig)
			.queryParam("token", w.getTxId())
		.expect()
			.statusCode(204)
		.when()
			.delete(embeddedJetty.getBaseUri() + ProductResource.PATH+"/charge");
    }

}

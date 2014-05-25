package com._37coins;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;

import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com._37coins.products.ProductsClient;
import com._37coins.products.ProductsClientException;
import com._37coins.resources.ProductResource;
import com._37coins.workflow.pojo.Withdrawal;
import com.google.i18n.phonenumbers.NumberParseException;

public class ClientTest {	
    private static EmbeddedJetty embeddedJetty;
    private static ProductsClient client;

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
        client = new ProductsClient(embeddedJetty.getBaseUri() + ProductResource.PATH, "password");
	}
    
    @AfterClass
    public static void afterClass() throws Exception {
        embeddedJetty.stop();
    }
    
    @Test
    public void testAuthFail() throws IOException, NoSuchAlgorithmException, NumberParseException, ProductsClientException, URISyntaxException{
        //try it with a wrong password
        try {
            new ProductsClient(embeddedJetty.getBaseUri() + ProductResource.PATH, "wrongPw")
                .charge(new BigDecimal("0.5"), "+491606941382");
            Assert.assertTrue("exception expected", false);
        }catch(ProductsClientException e){
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testPhone() throws IOException, NoSuchAlgorithmException, NumberParseException, ProductsClientException, URISyntaxException{
        //create
        String token = client.charge(new BigDecimal("0.5"), "+491606941382");
        Assert.assertNotNull(token);
        //retrieve back
        Withdrawal w = client.getCharge(token);
        Assert.assertEquals(new BigDecimal("0.5"), w.getAmount());
        Assert.assertEquals("491606941382", w.getPayDest().getAddress());
    }
    	
    @Test
    public void testAddress() throws IOException, NoSuchAlgorithmException, ProductsClientException, NumberParseException, URISyntaxException{
        //create
        String token = client.charge(new BigDecimal("0.5"), "19xeDDxhahx4f32WtBbPwFMWBq28rrYVoh","bla");
    	//retrieve back
        Withdrawal w = client.getCharge(token);
        Assert.assertEquals(new BigDecimal("0.5"), w.getAmount());
        Assert.assertEquals("19xeDDxhahx4f32WtBbPwFMWBq28rrYVoh", w.getPayDest().getAddress());
        //delete that one
        client.deleteCharge(token);
    }

}

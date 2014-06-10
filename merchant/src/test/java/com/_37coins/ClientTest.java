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

import com._37coins.merchant.MerchantClient;
import com._37coins.merchant.MerchantClientException;
import com._37coins.merchant.pojo.Charge;
import com._37coins.merchant.pojo.MerchantResponse;
import com._37coins.resources.MerchantResource;
import com.google.i18n.phonenumbers.NumberParseException;

public class ClientTest {	
    private static EmbeddedJetty embeddedJetty;
    private static MerchantClient client;

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
        client = new MerchantClient(embeddedJetty.getBaseUri() + MerchantResource.PATH, "test");
	}
    
    @AfterClass
    public static void afterClass() throws Exception {
        embeddedJetty.stop();
    }
    
    @Test
    public void testAuthFail() throws IOException, NoSuchAlgorithmException, NumberParseException, MerchantClientException, URISyntaxException{
        //try it with a wrong password
        try {
            new MerchantClient(embeddedJetty.getBaseUri() + MerchantResource.PATH, "wrongPw")
                .charge(new BigDecimal("0.5"), "+491606941382");
            Assert.assertTrue("exception expected", false);
        }catch(MerchantClientException e){
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testPhone() throws IOException, NoSuchAlgorithmException, NumberParseException, MerchantClientException, URISyntaxException{
        //create
        MerchantResponse mr = client.charge(new BigDecimal("0.5"), "+491606941382");
        Assert.assertNotNull(mr.getToken());
        //retrieve back
        Charge c = client.getCharge(mr.getToken());
        Assert.assertEquals(new BigDecimal("0.5"), c.getAmount());
        Assert.assertEquals("491606941382", c.getPayDest().getAddress());
    }
    	
    @Test
    public void testAddress() throws IOException, NoSuchAlgorithmException, MerchantClientException, NumberParseException, URISyntaxException{
        //create
        MerchantResponse mr = client.charge(new BigDecimal("0.5"), "19xeDDxhahx4f32WtBbPwFMWBq28rrYVoh","bla");
    	//retrieve back
        Charge c = client.getCharge(mr.getToken());
        Assert.assertEquals(new BigDecimal("0.5"), c.getAmount());
        Assert.assertEquals("19xeDDxhahx4f32WtBbPwFMWBq28rrYVoh", c.getPayDest().getAddress());
        //delete that one
        client.deleteCharge(mr.getToken());
    }

}

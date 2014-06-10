package com._37coins;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com._37coins.merchant.MerchantClientException;
import com._37coins.merchant.pojo.MerchantResponse;
import com.google.i18n.phonenumbers.NumberParseException;

/**  @author Johann Barbie */
@RunWith(JUnit4.class)
public class ProductsBasicTest extends AbstractProductsClientTest {

    @Test
    public void testCharge() throws MerchantClientException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException, NumberParseException {
        //
        setUpToRespondWith("valid_charge_request.json");
        MerchantResponse request = client.charge(new BigDecimal("0.05"), "19xeDDxhahx4f32WtBbPwFMWBq28rrYVoh");
        assertNotNull(request.getDisplayName());
        //
        setUpToRespondWith("valid_charge_token.json");
        MerchantResponse token = client.charge(new BigDecimal("0.05"), "+491606941382");
        assertNotNull(token.getToken());
        assertNotNull(token.getDisplayName());
        assertNotNull(token.getGateway());
    }

}
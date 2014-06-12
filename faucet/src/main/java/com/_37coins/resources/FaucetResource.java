package com._37coins.resources;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;
import com._37coins.util.FiatPriceProvider;
import com._37coins.web.PriceTick;
import com._37coins.workflow.pojo.Signup;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * local service, service, exposed to gateways
 *
 * @author johann
 *
 */

@Path(FaucetResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class FaucetResource {
    public final static String PATH = "/callback";
    public static Logger log = LoggerFactory.getLogger(FaucetResource.class);
    
    final private FiatPriceProvider fiatPriceProvider;
    final private Cache cache;
    private String url;
    private HttpClient httpClient; 
    
    @Inject public FaucetResource(ServletRequest request,
            FiatPriceProvider fiatPriceProvider,
            Cache cache, String url) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.fiatPriceProvider = fiatPriceProvider;
        this.cache = cache;
        this.url = url;
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
        httpClient = HttpClients.custom().setSSLSocketFactory(
                sslsf).build();
    }
    
    @POST
    public int receive(Signup signup) throws JsonProcessingException{
        System.out.println(new ObjectMapper().writeValueAsString(signup));
        Element e = cache.get("previous" + signup.getMobile());
        if (e!=null){
            throw new WebApplicationException("payed out before",Response.Status.CONFLICT);
        }
        PriceTick pt = fiatPriceProvider.getLocalCurValue(new BigDecimal("1").setScale(8),new Locale("en","US"));
        BigDecimal amount = new BigDecimal("0.50").setScale(8).divide(pt.getLast().setScale(8),RoundingMode.HALF_UP).multiply(new BigDecimal("100000000")).setScale(0);
        String called = url + "&to="+signup.getDestination().getAddress()+"&amount="+amount.toPlainString()+"&note="+signup.getMobile().replace("+", "");
        HttpGet request = new HttpGet(called);
        try {
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode()<400)
                cache.put(new Element("previous" + signup.getMobile(),true));
            return response.getStatusLine().getStatusCode();
        } catch (IOException ex) {
            log.error("blockcahin client error", ex);
            ex.printStackTrace();
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}

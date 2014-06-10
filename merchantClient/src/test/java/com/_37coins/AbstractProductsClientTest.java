package com._37coins;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;

import com._37coins.helpers.MockHelper;
import com._37coins.helpers.Response;
import com._37coins.merchant.MerchantClient;

/**
 * @author Johann Barbie
 */
public class AbstractProductsClientTest {

    public static final String TEST_URL = "http://localhost:8080/products";

    protected static MerchantClient client;

    protected static MockHelper testHelper;

    protected static HttpClient mockHttpClient;


    @BeforeClass
    public static void setUpClass() {
        testHelper = new MockHelper();
        mockHttpClient = mock(HttpClient.class);
        client = new MerchantClient(TEST_URL, "token", mockHttpClient);
    }

    @Before
    public void setUp() throws Exception {
        reset(mockHttpClient);
    }

    protected void setUpToRespondWith(String filename) {
        try {
            when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(
                    Response.OKResponseWithDataFromFile(filename)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUpToRespondWith(HttpResponse response) {
        try {
            when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void verifyHttpClientExecutedWithArgThat(Matcher<HttpUriRequest> matcher) {
        try {
            verify(mockHttpClient).execute(argThat(matcher));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

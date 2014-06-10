package com._37coins.merchant;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class HttpClientFactory {

    public static HttpClientBuilder getClientBuilder() {
        return HttpClientBuilder.create()
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                        return response.getStatusLine().getStatusCode() == 308 || super.isRedirected(request, response, context);
                    }
                });
    }
}

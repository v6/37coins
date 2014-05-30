package com._37coins.helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class MockHelper {

    public static InputStream getFixtureInputStream(String filename) {
        try {
            return new FileInputStream("src/test/resources/" + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClientBuilder makeAllTrustingClient(HttpClientBuilder httpClientBuilder) {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return httpClientBuilder
                    .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                    .setSslcontext(sc);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClientBuilder createTrustNoOneHttpClient() {
        try {
            return HttpClients
                    .custom()
                    .setSslcontext(
                            SSLContexts
                                    .custom()
                                    .loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType()), new TrustStrategy() {
                                        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                            return false;
                                        }
                                    })
                                    .build()
                    );
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }


}

package com._37coins.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.restnucleus.filter.DigestFilter;

import com._37coins.merchant.pojo.MerchantRequest;
import com._37coins.merchant.pojo.PaymentDestination;
import com._37coins.merchant.pojo.PaymentDestination.AddressType;
import com._37coins.resources.MerchantResource;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MerchantTester {

	/**
	 * @param args
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */

	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
		MerchantRequest request = new MerchantRequest()
			.setAmount(new BigDecimal("0.004"))
			.setPayDest(new PaymentDestination().setAddress("19xeDDxhahx4f32WtBbPwFMWBq28rrYVoh").setAddressType(AddressType.BTC));
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost req = new HttpPost("https://www.37coins.com"+MerchantResource.PATH+"/charge/token");
		String reqValue = new ObjectMapper().writeValueAsString(request);
		System.out.println(reqValue);
		StringEntity entity = new StringEntity(reqValue, "UTF-8");
		entity.setContentType("application/json");
		String reqSig = DigestFilter.calculateSignature(
				"https://www.37coins.com"+MerchantResource.PATH+"/charge/test",
				DigestFilter.parseJson(reqValue.getBytes()),
				"pw");
		req.setHeader(DigestFilter.AUTH_HEADER, reqSig);
		req.setEntity(entity);
		CloseableHttpResponse rsp = httpclient.execute(req);
		System.out.println(convertStreamToString(rsp.getEntity().getContent()));
	}

	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}

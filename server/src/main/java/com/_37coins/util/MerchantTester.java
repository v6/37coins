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
import org.restnucleus.filter.HmacFilter;

import com._37coins.resources.MerchantResource;
import com._37coins.web.MerchantRequest;
import com._37coins.web.PriceTick;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
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
			.setAmount(new BigDecimal("0.002"))
			.setPayDest(new PaymentAddress().setAddress("1CBtg1bs2e7s4mWRGPCUwaSFFH2dDfnHf3").setAddressType(PaymentType.BTC))
			.setOrderName("product")
			.setConversion(new PriceTick().setAsk(new BigDecimal("1000")).setCurCode("EUR"));
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost req = new HttpPost("https://www.37coins.com"+MerchantResource.PATH+"/charge/test");
		String reqValue = new ObjectMapper().writeValueAsString(request);
		StringEntity entity = new StringEntity(reqValue, "UTF-8");
		entity.setContentType("application/json");
		String reqSig = HmacFilter.calculateSignature(
				"https://www.37coins.com"+MerchantResource.PATH+"/charge/test",
				HmacFilter.parseJson(reqValue.getBytes()),
				"pw");
		req.setHeader(HmacFilter.AUTH_HEADER, reqSig);
		req.setEntity(entity);
		CloseableHttpResponse rsp = httpclient.execute(req);
		System.out.println(convertStreamToString(rsp.getEntity().getContent()));
	}

	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}

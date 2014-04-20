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
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.Withdrawal;
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
		Withdrawal withdrawal = new Withdrawal().setAmount(new BigDecimal("0.005")).setPayDest(new PaymentAddress().setAddress("address").setAddressType(PaymentType.BTC));
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost req = new HttpPost("https://www.37coins.com"+MerchantResource.PATH+"/charge/token");
		String reqValue = new ObjectMapper().writeValueAsString(withdrawal);
		StringEntity entity = new StringEntity(reqValue, "UTF-8");
		entity.setContentType("application/json");
		String reqSig = HmacFilter.calculateSignature(
				"https://www.37coins.com"+MerchantResource.PATH+"/charge/token",
				HmacFilter.parseJson(reqValue.getBytes()),
				"secret");
		req.setHeader(HmacFilter.AUTH_HEADER, reqSig);
		req.setEntity(entity);
		CloseableHttpResponse rsp = httpclient.execute(req);
		if (rsp.getStatusLine().getStatusCode()==200){
			System.out.println(new ObjectMapper().writeValueAsString(new ObjectMapper().readValue(rsp.getEntity().getContent(), Withdrawal.class)));
		}else{
			System.out.println("received status: "+rsp.getStatusLine().getStatusCode()+convertStreamToString(rsp.getEntity().getContent()));
		}
	}

	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}

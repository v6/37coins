package com._37coins;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com._37coins.bcJsonRpc.pojo.Transaction;
import com._37coins.web.PriceTick;
import com._37coins.web.Seller;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.MessageAddress;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.Withdrawal;

import freemarker.template.TemplateException;

public class LocalizationTest {
	
	DataSet rsp;
	MessageFactory ef = new MessageFactory();
	
	@Before
	public void start(){
		rsp =  new DataSet()
		.setService("37coins")
		.setLocale(new Locale("en"))
		.setPayload(new PaymentAddress()
			.setAddress("mkGFr3M4HWy3NQm6LcSprcUypghQxoYmVq"))
		.setTo(new MessageAddress()
			.setAddress("test@37coins.com"));
	}
	
	//matches all locales onto what plivo has available
	@Test
	public void testPlivo() {
		Assert.assertEquals("de-DE",MessagingActivitiesImpl.supportedByPlivo(new Locale("de","DE")));//simple map
		Assert.assertEquals("de-DE",MessagingActivitiesImpl.supportedByPlivo(new Locale("de")));//map to closest
		Assert.assertEquals("ru-RU",MessagingActivitiesImpl.supportedByPlivo(new Locale("ru")));//map to closest
		Assert.assertEquals("de-DE",MessagingActivitiesImpl.supportedByPlivo(new Locale("de","LU")));//map to closest
		Assert.assertEquals("arabic",MessagingActivitiesImpl.supportedByPlivo(new Locale("ar","KW")));//map any arabic
		Assert.assertEquals("en-US",MessagingActivitiesImpl.supportedByPlivo(new Locale("en")));//map to closest
		Assert.assertEquals("en-US",MessagingActivitiesImpl.supportedByPlivo(new Locale("en","MT")));//map to closest
		Assert.assertEquals("en-CA",MessagingActivitiesImpl.supportedByPlivo(new Locale("en","CA")));//exact match
		Assert.assertEquals("es-ES",MessagingActivitiesImpl.supportedByPlivo(new Locale("es","BO")));//map to closest
		Assert.assertEquals("es-US",MessagingActivitiesImpl.supportedByPlivo(new Locale("es","US")));//exact match
		Assert.assertEquals("en-US",MessagingActivitiesImpl.supportedByPlivo(new Locale("sr","CS")));//map any other
	}
	
	//matches all locales onto what we have, than makes plivo locale from it
	@Test
	public void testResource() throws IOException, TemplateException{
		//arrabic, available in plivo, but we don't have
		Assert.assertEquals("en-US",MessagingActivitiesImpl.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("ar","KW")))));
		//korean, we have, but not available in plivo
		Assert.assertEquals("en-US",MessagingActivitiesImpl.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("kr","KO")))));
		//swiss german, neither plivo nor we have, default to german
		Assert.assertEquals("de-DE",MessagingActivitiesImpl.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("de","CH")))));
		//general german
		Assert.assertEquals("de-DE",MessagingActivitiesImpl.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("de")))));
		//US spanish to default spanish
		Assert.assertEquals("es-ES",MessagingActivitiesImpl.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("es","US")))));
		//general russian
		Assert.assertEquals("ru-RU",MessagingActivitiesImpl.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("ru","RU")))));
		//absolutely unknown, default to english
		Assert.assertEquals("en-US",MessagingActivitiesImpl.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("sr","CS")))));
	}
	
	public void testAvailable() {
		
	}
	
	@Test
	public void test37coinsCreate() throws IOException, TemplateException {
		rsp.setAction(Action.SIGNUP);
		System.out.println("SIGNUP:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsDeposit() throws IOException, TemplateException {
		rsp.setAction(Action.DEPOSIT_REQ);
		System.out.println("DEPOSIT REQ:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsHelp() throws IOException, TemplateException {
		rsp.setAction(Action.HELP);
		System.out.println("HELP:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
		
	@Test
	public void test37coinsReiceive() throws IOException, TemplateException {
		rsp.setAction(Action.DEPOSIT_CONF)
			.setPayload(new Withdrawal()
				.setAmount(new BigDecimal("0.05")));
		System.out.println("DEPOSIT CONFIRM:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsSend() throws IOException, TemplateException {
		rsp.setAction(Action.WITHDRAWAL_REQ)
			.setPayload(new Withdrawal()
				.setAmount(new BigDecimal("0.01"))
				.setMsgDest(new MessageAddress()
					.setAddress("other@37coins.com")));
		System.out.println("DEPOSIT CONFIRM:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsWithdrawalReq() throws IOException, TemplateException {
		rsp.setAction(Action.WITHDRAWAL_CONF)
			.setPayload(new Withdrawal()
				.setAmount(new BigDecimal("0.01"))
				.setConfKey("something")
				.setConfLink("http://37coins.com/rest/something")
				.setMsgDest(new MessageAddress()
					.setAddress("other@37coins.com")));
		System.out.println("WITHDRAWAL REQUEST:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsBalance() throws IOException, TemplateException {
		rsp.setAction(Action.BALANCE)
			.setPayload(new Withdrawal()
				.setBalance(new BigDecimal("0.05")));
		System.out.println("BALANCE:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void testInsuficcientFunds() throws IOException, TemplateException {
		rsp.setAction(Action.INSUFISSIENT_FUNDS)
			.setPayload(new Withdrawal()
				.setAmount(new BigDecimal("1000.051"))
				.setBalance(new BigDecimal("0.5123456789")));
		String s = ef.constructTxt(rsp);
		Assert.assertTrue(s.contains("1,000.051"));
		Assert.assertTrue(s.contains("0.51234568"));
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void testInsuficcientFundsDe() throws IOException, TemplateException {
		rsp.setAction(Action.INSUFISSIENT_FUNDS)
			.setLocale(new Locale("de"))
			.setPayload(new Withdrawal()
				.setAmount(new BigDecimal("1000.051"))
				.setBalance(new BigDecimal("0.5123456789")));
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue(s.contains("1.000,051"));
		Assert.assertTrue(s.contains("0,51234568"));
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void testTransactions() throws IOException, TemplateException {
		List<Transaction> list = new ArrayList<>();
		list.add(new Transaction().setTime(System.currentTimeMillis()-360000000L).setComment("hallo").setAmount(new BigDecimal("0.4")).setTo("hast@test.com"));
		list.add(new Transaction().setTime(System.currentTimeMillis()-760000000L).setComment("hallo").setAmount(new BigDecimal("0.3")).setTo("hast@test.com"));
		list.add(new Transaction().setTime(System.currentTimeMillis()-960000000L).setComment("hallo").setAmount(new BigDecimal("0.2")).setTo("hast@test.com"));
		list.add(new Transaction().setTime(System.currentTimeMillis()).setComment("hallo").setAmount(new BigDecimal("0.1")).setTo("hast@test.com"));
		rsp.setAction(Action.TRANSACTION)
			.setLocale(new Locale("de"))
			.setPayload(list);
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void testEmptyTransactions() throws IOException, TemplateException {
		List<Transaction> list = null;//new ArrayList<>();
		rsp.setAction(Action.TRANSACTION)
			.setLocale(new Locale("de"))
			.setPayload(list);
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void testPrice() throws IOException, TemplateException {
		rsp.setAction(Action.PRICE)
			.setLocale(new Locale("en"))
			.setPayload(new PriceTick().setLast(new BigDecimal("1000.01")).setCurCode("EUR"));
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void testSell() throws IOException, TemplateException {
		List<Seller> sellers = new ArrayList<>();
		sellers.add(new Seller().setMobile("0160 83572040").setPrice(1.0f));
		sellers.add(new Seller().setMobile("0160 83572041").setPrice(1.1f));
		sellers.add(new Seller().setMobile("0160 83572042").setPrice(1.2f));
		sellers.add(new Seller().setMobile("0160 83572043").setPrice(1.3f));
		sellers.add(new Seller().setMobile("0160 83572044").setPrice(1.4f));
		rsp.setAction(Action.SELL)
			.setLocale(new Locale("en"))
			.setPayload(sellers);
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void testBuy() throws IOException, TemplateException {
		rsp.setAction(Action.BUY)
			.setLocale(new Locale("de"));
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}

}

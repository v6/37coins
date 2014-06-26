package com._37coins;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.joda.money.CurrencyUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com._37coins.bcJsonRpc.pojo.Transaction;
import com._37coins.plivo.Speak;
import com._37coins.plivo.XmlCharacterHandler;
import com._37coins.util.FiatPriceProvider;
import com._37coins.util.ResourceBundleClient;
import com._37coins.util.ResourceBundleFactory;
import com._37coins.web.PriceTick;
import com._37coins.web.Seller;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.MessageAddress;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Withdrawal;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import freemarker.template.TemplateException;

public class LocalizationTest {
	
	DataSet rsp;
	MessageFactory ef;
	FiatPriceProvider fiatPriceProvider;
	
	@Before
	public void start(){
		rsp =  new DataSet()
		.setService("37coins")
		.setLocale(new Locale("en","US"))
		//.setFiatPriceProvider(new FiatPriceProvider(null))
		.setTo(new MessageAddress()
			.setAddress("+491606941382"));
        fiatPriceProvider = new FiatPriceProvider(null, "http://api.bitcoinaverage.com/ticker/global/");
		List<Locale> activeLocales = new ArrayList<>();
		activeLocales.add(new Locale("en"));
		activeLocales.add(new Locale("de"));
		activeLocales.add(new Locale("ru"));
		activeLocales.add(new Locale("es"));
        ResourceBundleClient client = new ResourceBundleClient("http://localhost:9000"+"/scripts/nls/");
		ef = new MessageFactory(null,new ResourceBundleFactory(activeLocales, client, null),1000000,"Bit","#,##0");
	}
	
	//matches all locales onto what plivo has available
	@Test
	public void testPlivo() {
		Assert.assertEquals("de-DE",Speak.supportedByPlivo(new Locale("de","DE").toString()));//simple map
		Assert.assertEquals("de-DE",Speak.supportedByPlivo(new Locale("de").toString()));//map to closest
		Assert.assertEquals("ru-RU",Speak.supportedByPlivo(new Locale("ru").toString()));//map to closest
		Assert.assertEquals("de-DE",Speak.supportedByPlivo(new Locale("de","LU").toString()));//map to closest
		Assert.assertEquals("arabic",Speak.supportedByPlivo(new Locale("ar","KW").toString()));//map any arabic
		Assert.assertEquals("en-US",Speak.supportedByPlivo(new Locale("en").toString()));//map to closest
		Assert.assertEquals("en-US",Speak.supportedByPlivo(new Locale("en","MT").toString()));//map to closest
		Assert.assertEquals("en-CA",Speak.supportedByPlivo(new Locale("en","CA").toString()));//exact match
		Assert.assertEquals("es-ES",Speak.supportedByPlivo(new Locale("es","BO").toString()));//map to closest
		Assert.assertEquals("es-US",Speak.supportedByPlivo(new Locale("es","US").toString()));//exact match
		Assert.assertEquals("en-US",Speak.supportedByPlivo(new Locale("sr","CS").toString()));//map any other
	}
	
    @Test
    public void testXml() throws IOException, TemplateException, JAXBException{
    	DataSet ds = new DataSet().setLocaleString("ru_RU")
    			.setPayload(", 1 ,2 ,3 , 4");
    	String text = ef.getText("VoiceRegister",ds);
    	com._37coins.plivo.Response rv = new com._37coins.plivo.Response().add(new Speak()
			.setText(text)
			.setLanguage(ds.getLocaleString()));
    	JAXBContext jc = JAXBContext.newInstance(com._37coins.plivo.Response.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(CharacterEscapeHandler.class.getName(),new XmlCharacterHandler());
        marshaller.marshal(rv, System.out);

    }
	
	//matches all locales onto what we have, than makes plivo locale from it
	@Test
	public void testResource() throws IOException, TemplateException{
		//arrabic, available in plivo, but we don't have
		Assert.assertEquals("en-US",Speak.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("ar","KW"))).toString()));
		//korean, we have, but not available in plivo
		Assert.assertEquals("en-US",Speak.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("kr","KO"))).toString()));
		//swiss german, neither plivo nor we have, default to german
		Assert.assertEquals("de-DE",Speak.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("de","CH"))).toString()));
		//general german
		Assert.assertEquals("de-DE",Speak.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("de"))).toString()));
		//US spanish to default spanish
		Assert.assertEquals("es-ES",Speak.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("es","US"))).toString()));
		//general russian
		Assert.assertEquals("ru-RU",Speak.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("ru","RU"))).toString()));
		//absolutely unknown, default to english
		Assert.assertEquals("en-US",Speak.supportedByPlivo(ef.getLocale(new DataSet().setLocale(new Locale("sr","CS"))).toString()));
	}
	
	public void testAvailable() {
		
	}
	
	@Test
	public void test37coinsCreate() throws IOException, TemplateException {
		rsp.setAction(Action.SIGNUP);
		System.out.println("SIGNUP:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		rsp.setPayload(new PaymentAddress().setAddress("mkGFr3M4HWy3NQm6LcSprcUypghQxoYmVq"));
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsUnavailable() throws IOException, TemplateException {
		rsp.setAction(Action.UNAVAILABLE);
		System.out.println("UNAVAILABLE:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsDeposit() throws IOException, TemplateException {
		rsp.setAction(Action.DEPOSIT_REQ);
		System.out.println("DEPOSIT REQ:");
		rsp.setPayload(new PaymentAddress().setAddress("mkGFr3M4HWy3NQm6LcSprcUypghQxoYmVq"));
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsHelp() throws IOException, TemplateException {
		rsp.setAction(Action.HELP);
		System.out.println("HELP:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsReiceiveComment() throws IOException, TemplateException {
		rsp.setAction(Action.DEPOSIT_CONF)
			.setPayload(new Withdrawal()
				.setComment("hallo saonuhsanotheusaotehusaouh")
				.setMsgDest(new MessageAddress().setAddress("other@37coins.com"))
				.setBalance(new BigDecimal("1.25"))
				.setAmount(new BigDecimal("0.05")));
		System.out.println("DEPOSIT CONFIRM:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
		
	@Test
	public void test37coinsReiceive() throws IOException, TemplateException {
		rsp.setAction(Action.DEPOSIT_CONF)
			.setPayload(new Withdrawal()
				.setMsgDest(new MessageAddress().setAddress("other@37coins.com"))
				.setBalance(new BigDecimal("1.25"))
				.setAmount(new BigDecimal("0.05")));
		String s = ef.constructTxt(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsReiceiveNoSender() throws IOException, TemplateException {
		rsp.setAction(Action.DEPOSIT_CONF)
			.setPayload(new Withdrawal()
				.setBalance(new BigDecimal("1.25"))
				.setAmount(new BigDecimal("0.05")));
		String s = ef.constructTxt(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsReiceiveNot() throws IOException, TemplateException {
		rsp.setAction(Action.DEPOSIT_NOT)
			.setFiatPriceProvider(fiatPriceProvider)
			.setPayload(new Withdrawal()
				.setBalance(new BigDecimal("1.25"))
				.setAmount(new BigDecimal("0.05")));
		String s = ef.constructTxt(rsp);
		System.out.println(s);
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
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsSendDispName() throws IOException, TemplateException {
		rsp.setAction(Action.WITHDRAWAL_REQ)
			.setPayload(new Withdrawal()
				.setConfKey("a1234")
				.setAmount(new BigDecimal("0.01"))
				.setPayDest(new PaymentAddress()
					.setAddress("1CBtg1bs2e7s4mWRGPCUwaSFFH2dDfnHf3")
					.setAddressType(PaymentType.BTC)
					.setDisplayName("johann")));
		System.out.println("DEPOSIT CONFIRM:");
		String s = ef.constructTxt(rsp);
		Assert.assertTrue(s.contains("johann"));
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsSendAddr() throws IOException, TemplateException {
		rsp.setAction(Action.WITHDRAWAL_REQ)
			.setPayload(new Withdrawal()
				.setConfKey("a1234")
				.setAmount(new BigDecimal("0.01"))
				.setPayDest(new PaymentAddress()
					.setAddress("1CBtg1bs2e7s4mWRGPCUwaSFFH2dDfnHf3")
					.setAddressType(PaymentType.BTC)));
		System.out.println("DEPOSIT CONFIRM:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue(s.contains("1CBtg1"));
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsPayOrder() throws IOException, TemplateException {
		rsp.setAction(Action.WITHDRAWAL_REQ)
			.setPayload(new Withdrawal()
				.setConfKey("a1234")
				.setComment("apple")
				.setAmount(new BigDecimal("0.01"))
				.setPayDest(new PaymentAddress()
					.setAddress("1CBtg1bs2e7s4mWRGPCUwaSFFH2dDfnHf3")
					.setAddressType(PaymentType.BTC)
					.setDisplayName("johann")));
		System.out.println("DEPOSIT CONFIRM:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue(s.contains("johann"));
		Assert.assertTrue(s.contains("apple"));
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsPayAtRate() throws IOException, TemplateException {
		rsp.setAction(Action.WITHDRAWAL_REQ)
			.setFiatPriceProvider(new FiatPriceProvider(new BigDecimal("1000"), CurrencyUnit.EUR))
			.setPayload(new Withdrawal()
				.setConfKey("a1234")
				.setComment("apple")
				.setAmount(new BigDecimal("0.01"))
				.setPayDest(new PaymentAddress()
					.setAddress("1CBtg1bs2e7s4mWRGPCUwaSFFH2dDfnHf3")
					.setAddressType(PaymentType.BTC)
					.setDisplayName("johann")));
		System.out.println("DEPOSIT CONFIRM:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue(s.contains("johann"));
		Assert.assertTrue(s.contains("apple"));
		Assert.assertTrue(s.contains("10EUR")||s.contains("10,00 €")||s.contains("€ 10,00"));
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}	

	
	@Test
	public void test37coinsReq() throws IOException, TemplateException {
		rsp.setAction(Action.CHARGE)
			.setPayload(new Withdrawal()
				.setAmount(new BigDecimal("0.01"))
				.setComment("abc"));
		System.out.println("REQ:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsWithdrawalReq() throws IOException, TemplateException {
		rsp.setAction(Action.WITHDRAWAL_CONF)
			.setFiatPriceProvider(fiatPriceProvider)
			.setPayload(new Withdrawal()
				.setBalance(new BigDecimal("0.23"))
				.setAmount(new BigDecimal("0.01"))
				.setConfKey("something")
				.setConfLink("http://37coins.com/rest/something")
				.setMsgDest(new MessageAddress()
					.setAddress("other@37coins.com")));
		System.out.println("WITHDRAWAL REQUEST:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsBalance() throws IOException, TemplateException {
		rsp.setAction(Action.BALANCE)
		   .setLocale(new Locale("kz","DE"))
			.setPayload(new Withdrawal()
				.setBalance(new BigDecimal("0.05")));
		System.out.println("BALANCE:");
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsBalanceNoTicker() throws IOException, TemplateException {
		rsp =  new DataSet()
			.setService("37coins")
			.setLocale(new Locale("de"))
			.setPayload(new PaymentAddress()
				.setAddress("mkGFr3M4HWy3NQm6LcSprcUypghQxoYmVq"))
			.setTo(new MessageAddress()
				.setAddress("+491606941382"))
			.setAction(Action.BALANCE)
			.setPayload(new Withdrawal()
				.setBalance(new BigDecimal("0.05")));
		String s = ef.constructTxt(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	@Test
	public void test37coinsBalanceNoTickerResponse() throws IOException, TemplateException {
		rsp =  new DataSet()
			.setService("37coins")
			.setLocale(new Locale("de","DE"))
			.setFiatPriceProvider(new FiatPriceProvider(null,"http://somehttperror.net/"))
			.setPayload(new PaymentAddress()
				.setAddress("mkGFr3M4HWy3NQm6LcSprcUypghQxoYmVq"))
			.setTo(new MessageAddress()
				.setAddress("+491606941382"))
			.setAction(Action.BALANCE)
			.setPayload(new Withdrawal()
				.setBalance(new BigDecimal("0.05")));
		String s = ef.constructTxt(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}

	
	@Test
	public void testDestinationUnavailable() throws IOException, TemplateException {
		rsp.setAction(Action.DST_ERROR);
		String s = ef.constructTxt(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
   @Test
    public void testTransactionCanceled() throws IOException, TemplateException {
        rsp.setAction(Action.TX_CANCELED);
        String s = ef.constructTxt(rsp);
        System.out.println(s);
        Assert.assertTrue("SMS to long",s.getBytes().length<140);
    }
    
	
	@Test
	public void testVoice() throws IOException, TemplateException {
		rsp.setAction(Action.VOICE);
		String s = ef.constructTxt(rsp);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	
	@Test
	public void testInsuficcientFunds() throws IOException, TemplateException {
		rsp.setAction(Action.INSUFISSIENT_FUNDS)
			.setLocale(Locale.US)
			.setPayload(new Withdrawal()
				.setAmount(new BigDecimal("1.0511"))
				.setBalance(new BigDecimal("0.5123")));
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue(s.contains("1,051,1"));
		Assert.assertTrue(s.contains("512,3"));
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
		
		rsp.setAction(Action.INSUFISSIENT_FUNDS)
		.setLocale(new Locale("de","DE"))
		.setPayload(new Withdrawal()
			.setAmount(new BigDecimal("1.0511"))
			.setBalance(new BigDecimal("0.5123")));
		s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue(s.contains("1.051.1"));
		Assert.assertTrue(s.contains("512.3"));
		Assert.assertTrue("SMS to long",s.getBytes().length<140);

	}
	
	@Test
	public void testInsuficcientFundsDe() throws IOException, TemplateException {
		rsp.setAction(Action.INSUFISSIENT_FUNDS)
			.setLocale(new Locale("de","DE"))
			.setPayload(new Withdrawal()
				.setAmount(new BigDecimal("5.123456789"))
				.setBalance(new BigDecimal("1.0511")));
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue(s.contains("1.051.1"));
		Assert.assertTrue(s.contains("5.123.457"));
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
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}

	
    @Test
    public void testPriceNoParam() throws IOException, TemplateException {
        rsp.setAction(Action.PRICE)
            .setLocale(new Locale("en","US"))
            .setPayload(new PriceTick().setLast(new BigDecimal("500.01")));
        String s = ef.constructTxt(rsp);
        System.out.println(s);
        Assert.assertTrue("SMS to long",s.getBytes().length<140);
    }
	
	@Test
	public void testPrice() throws IOException, TemplateException {
		rsp.setAction(Action.PRICE)
			.setGwFee(new BigDecimal("0.5"))
			.setLocale(new Locale("en","US"))
			.setPayload(new PriceTick().setLast(new BigDecimal("500.01")).setLastFactored(new BigDecimal("250")).setCurCode("EUR"));
		String s = ef.constructTxt(rsp);
		System.out.println(s);
		Assert.assertTrue("SMS to long",s.getBytes().length<140);
	}
	
	   @Test
	    public void testHelpSend() throws IOException, TemplateException {
	        rsp.setAction(Action.HELP_SEND)
	            .setLocale(new Locale("en","US"));
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


	@Test
	public void test37coinsGatewayAlert() throws IOException, TemplateException {
		rsp.setAction(Action.GW_ALERT);
		System.out.println("GATEWAY ALERT:");
		ef.constructTxt(rsp);
		String s = ef.constructHtml(rsp);
		ef.constructSubject(rsp);
		System.out.println(s);
	}

}

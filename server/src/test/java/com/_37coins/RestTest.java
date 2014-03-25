package com._37coins;

import static com.jayway.restassured.RestAssured.given;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.Form;

import junit.framework.Assert;

import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com._37coins.parse.CommandParser;
import com._37coins.parse.ParserAction;
import com._37coins.parse.ParserClient;
import com._37coins.persistence.dto.Account;
import com._37coins.resources.EnvayaSmsResource;
import com._37coins.resources.HealthCheckResource;
import com._37coins.resources.ParserResource;
import com._37coins.web.PriceTick;
import com._37coins.web.Seller;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.Withdrawal;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;

public class RestTest {	
	static String address = "821038492849";
	static String pw = "test9900";
	static Account acc1;
	
    private static EmbeddedJetty embeddedJetty;

    @BeforeClass
    public static void beforeClass() throws Exception {
        embeddedJetty = new EmbeddedJetty(){
        	@Override
        	public String setInitParam(ServletHolder holder) {
        		holder.setInitParameter("javax.ws.rs.Application", "com._37coins.TestApplication");
        		return "src/test/webapp";
        	}
        };
        embeddedJetty.start();
        // Create the configuration to use for the server.
        InMemoryDirectoryServerConfig config =
             new InMemoryDirectoryServerConfig("dc=37coins,dc=com");
        config.addAdditionalBindCredentials("cn=admin", "test9900");
        InMemoryListenerConfig lc = new InMemoryListenerConfig("test", Inet4Address.getByName("127.0.0.1"), 51382, null,null,null);
        config.setListenerConfigs(lc);

        // Create the directory server instance, populate it with data from the
        // "test-data.ldif" file, and start listening for client connections.
        InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
        ds.importFromLDIF(true, "src/test/resources/test-data.ldif");
        ds.startListening();
	}
    
    @AfterClass
    public static void afterClass() throws Exception {
        embeddedJetty.stop();
    }
    
    private ObjectMapper mapper;
    
    @Before
    public void testThis(){
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
    }
    
    @Test
	public void testParserClient() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser());
		parserClient.start("+821039842742", "+821027423984", "send 0.1 +821039842743", 8087,
		new ParserAction() {
			@Override
			public void handleWithdrawal(DataSet data) {
				ds.setTo(data.getTo());
				ds.setAction(data.getAction());
			}
			@Override
			public void handleResponse(DataSet data) {
				ds.setTo(data.getTo());
				ds.setAction(data.getAction());
			}
			
			@Override
			public void handleDeposit(DataSet data) {
				ds.setTo(data.getTo());
				ds.setAction(data.getAction());
			}
			
			@Override
			public void handleConfirm(DataSet data) {
				ds.setTo(data.getTo());
				ds.setAction(data.getAction());
			}
		});
		parserClient.join();
		Assert.assertFalse(ds.getTo().getGateway().contains("+"));
    }
    
    @Test
	public void testWebInvite() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser());
		parserClient.start("+821039841234", null, Action.SIGNUP.toString(), 8087,
		new ParserAction() {
			@Override
			public void handleResponse(DataSet data) {
				ds.setAction(data.getAction());
				ds.setTo(data.getTo());
				ds.setCn(data.getCn());
			}
			
			@Override
			public void handleWithdrawal(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleDeposit(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleConfirm(DataSet data) {ds.setAction(data.getAction());}
		});
		parserClient.join();
		Assert.assertTrue("unexpected Response: "+ds.getAction().toString(),ds.getAction()==Action.SIGNUP);
		Assert.assertEquals("OZV4N1JS2Z3476NL",ds.getTo().getGateway());
		Assert.assertNotNull(ds.getCn());
    }
    
    @Test
	public void testVoiceReq() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser());
		parserClient.start("+821039841235", "+821027423984", Action.VOICE.toString(), 8087,
		new ParserAction() {
			@Override
			public void handleResponse(DataSet data) {ds.setAction(data.getAction());}			
			@Override
			public void handleWithdrawal(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleDeposit(DataSet data) {
				ds.setAction(data.getAction())
					.setTo(data.getTo())
					.setLocale(data.getLocale())
					.setCn(data.getCn());
			}
			@Override
			public void handleConfirm(DataSet data) {ds.setAction(data.getAction());}
		});
		parserClient.join();
		Assert.assertTrue("unexpected Response: "+ds.getAction().toString(),ds.getAction()==Action.VOICE);
		Assert.assertEquals("OZV4N1JS2Z3476NL",ds.getTo().getGateway());
		Assert.assertEquals("+821039841235",ds.getTo().getAddress());
		Assert.assertEquals(Locale.forLanguageTag("kr"),ds.getLocale());
		Assert.assertNotNull(ds.getCn());
    }
    
    @Test
	public void testCharge() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser());
		parserClient.start("+821039841234", "+821027423984", "req 0.01", 8087,
		new ParserAction() {
			@Override
			public void handleResponse(DataSet data) {
				ds.setAction(data.getAction());
				ds.setTo(data.getTo());
				ds.setCn(data.getCn());
			}
			
			@Override
			public void handleWithdrawal(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleDeposit(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleConfirm(DataSet data) {ds.setAction(data.getAction());}
		});
		parserClient.join();
		Assert.assertTrue("unexpected Response: "+ds.getAction().toString(),ds.getAction()==Action.CHARGE);
		Assert.assertEquals("OZV4N1JS2Z3476NL",ds.getTo().getGateway());
		Assert.assertNotNull(ds.getCn());
    }
    
    @Test
	public void testWebfinger() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser());
		parserClient.start("+821039841234", "+821027423984", "send 0.1 jangkim321@gmail.com", 8087,
		new ParserAction() {
			@Override
			public void handleResponse(DataSet data) {ds.setAction(data.getAction());}
			
			@Override
			public void handleWithdrawal(DataSet data) {
				ds.setAction(data.getAction());
				ds.setPayload(data.getPayload());
				ds.setCn(data.getCn());
			}
			@Override
			public void handleDeposit(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleConfirm(DataSet data) {ds.setAction(data.getAction());}
		});
		parserClient.join();
		Assert.assertTrue("unexpected Response: "+ds.getAction().toString(),ds.getAction()==Action.WITHDRAWAL_REQ);
		Withdrawal w = (Withdrawal)ds.getPayload();
		Assert.assertEquals("19xeDDxhahx4f32WtBbPwFMWBq28rrYVoh",w.getPayDest().getAddress());
		Assert.assertEquals(PaymentAddress.PaymentType.BTC,w.getPayDest().getAddressType());
		Assert.assertNotNull(ds.getCn());
    }
	
	@Test
	public void testSignature() throws NoSuchAlgorithmException, UnsupportedEncodingException{
		given()
		.expect()
			.statusCode(200)
		.when()
			.get(embeddedJetty.getBaseUri() + HealthCheckResource.PATH);
		Form m = new Form();
		m.param("version", "0.1");
		m.param("now","12356789");
		m.param("power","30");
		m.param("action","status");	
		String serverUrl = embeddedJetty.getBaseUri() + EnvayaSmsResource.PATH+"/OZV4N1JS2Z3476NL/sms";
		System.out.println(serverUrl);
		String sig = EnvayaSmsResource.calculateSignature(serverUrl, m.asMap(), pw);
		// fire get successfully
		given()
			.contentType(ContentType.URLENC)
			.header("X-Request-Signature", sig)
			.formParam("version", m.asMap().getFirst("version"))
			.formParam("now", m.asMap().getFirst("now"))
			.formParam("power", m.asMap().getFirst("power"))
			.formParam("action", m.asMap().getFirst("action"))
		.expect()
			.statusCode(200)
		.when()
			.post(serverUrl);
	}
	
	@Test
	public void testUser() throws JsonParseException, JsonMappingException, IOException{
		//ask help
		Response r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "help")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Help");
		List<DataSet> rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",2, rv.size());
		Assert.assertEquals("test@test.com", rv.get(0).getCn());
		Assert.assertEquals("mail@37coins.com", rv.get(0).getTo().getGateway());
		Assert.assertEquals(Action.SIGNUP, rv.get(1).getAction());
		//get btc address
		r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "deposit")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/DepositReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.DEPOSIT_REQ, rv.get(0).getAction());
		//send money, new user, same country
		r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "send 0.01 +821053215679")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",2, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		Withdrawal w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("821053215679", w.getPayDest().getAddress());
		Assert.assertEquals(new BigDecimal("0.01").setScale(8), w.getAmount());
		Assert.assertEquals(new BigDecimal("0.0001").setScale(8), w.getFee());
		Assert.assertEquals("MAILN1JS2Z34MAIL", w.getFeeAccount());
		Assert.assertEquals(Action.SIGNUP, rv.get(1).getAction());
		Assert.assertEquals("OZV4N1JS2Z3476NL", rv.get(1).getTo().getGateway());
		Assert.assertEquals("821053215679", rv.get(1).getCn());
		//say hi
		r = given()
			.formParam("from", "+821043215678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "hi")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/UnknownCommand");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.SIGNUP, rv.get(0).getAction());
		//ask help
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "help")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Help");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",2, rv.size());
		Assert.assertEquals(Action.HELP, rv.get(0).getAction());
		Assert.assertEquals("821012345678", rv.get(0).getCn());
		Assert.assertEquals("OZV4N1JS2Z3476NL", rv.get(0).getTo().getGateway());
		Assert.assertEquals(Action.SIGNUP, rv.get(1).getAction());
		//get price
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "preis")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Price");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.PRICE, rv.get(0).getAction());
		PriceTick pt = (PriceTick)rv.get(0).getPayload();
		Assert.assertEquals("KRW", pt.getCurCode());
		Assert.assertNotNull(pt.getLast());
		//get price
		r = given()
			.formParam("from", "+491601234567")
			.formParam("gateway", "+491602742398")
			.formParam("message", "preis")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Price");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",2, rv.size());
		Assert.assertEquals(Action.PRICE, rv.get(0).getAction());
		pt = (PriceTick)rv.get(0).getPayload();
		Assert.assertEquals("EUR", pt.getCurCode());
		Assert.assertNotNull(pt.getLast());
		//test overuse
		r = given()
			.formParam("from", "+491601234567")
			.formParam("gateway", "+491602742398")
			.formParam("message", "preis")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Price");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.OVERUSE, rv.get(0).getAction());
		//test silence
		r = given()
			.formParam("from", "+491601234567")
			.formParam("gateway", "+491602742398")
			.formParam("message", "preis")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Price");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",0, rv.size());
		//test sell, no offer
		r = given()
			.formParam("from", "+491601234567")
			.formParam("gateway", "+491602742398")
			.formParam("message", "sell")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Sell");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.SELL, rv.get(0).getAction());
		//test buy
		r = given()
			.formParam("from", "+491601234567")
			.formParam("gateway", "+491602742398")
			.formParam("message", "buy 1.0")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Buy");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.BUY, rv.get(0).getAction());
		//test sell
		r = given()
			.formParam("from", "+491601234567")
			.formParam("gateway", "+491602742398")
			.formParam("message", "sell")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Sell");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.SELL, rv.get(0).getAction());
		List<Seller> sellers = (List<Seller>)rv.get(0).getPayload();
		Assert.assertEquals("0160 1234567",sellers.get(0).getMobile());
		//get btc address
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "deposit")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/DepositReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.DEPOSIT_REQ, rv.get(0).getAction());
		//list last transactions
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "tx")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Transactions");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.TRANSACTION, rv.get(0).getAction());
		//get account balance
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "balance")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Balance");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.BALANCE, rv.get(0).getAction());
		//send money from email to mobile
		r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "send 0.01 +821012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("821012345678", w.getPayDest().getAddress());
		Assert.assertEquals("MAILN1JS2Z34MAIL", w.getFeeAccount());
		//send money use dot notation
		r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "send .01 +821012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("821012345678", w.getPayDest().getAddress());
		Assert.assertEquals("MAILN1JS2Z34MAIL", w.getFeeAccount());
		//prevent sending zero
		r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "send 0.0 +821012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.BELOW_FEE, rv.get(0).getAction());
		//send all money to other user
		r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "send all +821012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("821012345678", w.getPayDest().getAddress());
		Assert.assertEquals("MAILN1JS2Z34MAIL", w.getFeeAccount());
		Assert.assertEquals(BigDecimal.ZERO, w.getAmount());
		//use currency code for sending
		r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "send eur20 +821012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("821012345678", w.getPayDest().getAddress());
		Assert.assertTrue(w.getAmount()!=null);
		Assert.assertEquals("MAILN1JS2Z34MAIL", w.getFeeAccount());
		//send money from email to local number
		r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "send 0.01 01012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.FORMAT_ERROR, rv.get(0).getAction());
		Assert.assertEquals("test@test.com", rv.get(0).getTo().getEmail().getAddress());
		//send money, new user, same country
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "send 0.01 01087654321")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",2, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("821087654321", w.getPayDest().getAddress());
		Assert.assertEquals(new BigDecimal("0.01").setScale(8), w.getAmount());
		Assert.assertEquals(new BigDecimal("0.0007").setScale(8), w.getFee());
		Assert.assertEquals("OZV4N1JS2Z3476NL", w.getFeeAccount());
		Assert.assertEquals(Action.SIGNUP, rv.get(1).getAction());
		Assert.assertEquals("OZV4N1JS2Z3476NL", rv.get(1).getTo().getGateway());
		Assert.assertEquals("821087654321", rv.get(1).getCn());
		//restore account
		r = given()
			.formParam("from", "+821087654321")
			.formParam("gateway", "+821027423984")
			.formParam("message", "restore 01012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Restore");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("821087654321", w.getPayDest().getAddress());
		Assert.assertEquals(BigDecimal.ZERO, w.getAmount());
		Assert.assertEquals(new BigDecimal("0.0007").setScale(8), w.getFee());
		Assert.assertEquals("OZV4N1JS2Z3476NL", w.getFeeAccount());
		//check out the TestServletConfig for cached data
		//send money, new user, other country
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "send 0.01 +491087654321")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",2, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		Assert.assertEquals(Action.SIGNUP, rv.get(1).getAction());
		Assert.assertEquals(Locale.GERMANY, rv.get(1).getLocale());
		Assert.assertEquals("DEV4N1JS2Z3476DE", rv.get(1).getTo().getGateway());
		Assert.assertEquals("491087654321", rv.get(1).getCn());
		//send money, new user, other country, no gateway
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "send 0.01 +631087654321")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.DST_ERROR, rv.get(0).getAction());
		//send money, use foreign gateway
		r = given()
			.formParam("from", "+491012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "send 0.01 +631087654321")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",0, rv.size());
		//charge a customer
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "req 0.01")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Charge");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.CHARGE, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		String token = w.getComment();
		//pay charge
		r = given()
			.formParam("from", "+491087654321")
			.formParam("gateway", "+491602742398")
			.formParam("message", "pay "+token)
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Pay");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("821012345678", w.getPayDest().getAddress());
		Assert.assertEquals("DEV4N1JS2Z3476DE", w.getFeeAccount());
		//confirm a transaction
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "conf test")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalConf");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_CONF, rv.get(0).getAction());
		Assert.assertEquals("test", rv.get(0).getPayload());
		//confirm with sign
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "# test")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalConf");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_CONF, rv.get(0).getAction());
		Assert.assertEquals("test", rv.get(0).getPayload());
		//ask help
		r = given()
			.formParam("from", "+821099999999")
			.formParam("gateway", "+821027423984")
			.formParam("message", "bla")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/UnknownCommand");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.SIGNUP, rv.get(0).getAction());
		Assert.assertEquals("821099999999", rv.get(0).getCn());
		Assert.assertEquals("OZV4N1JS2Z3476NL", rv.get(0).getTo().getGateway());
		//ask again
		r = given()
			.formParam("from", "+821099999999")
			.formParam("gateway", "+821027423984")
			.formParam("message", "bla")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/UnknownCommand");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.UNKNOWN_COMMAND, rv.get(0).getAction());
		//to little
		r = given()
			.formParam("from", "+821099999999")
			.formParam("gateway", "+821027423984")
			.formParam("message", "send 0.0001 +821012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.BELOW_FEE, rv.get(0).getAction());
	}

}

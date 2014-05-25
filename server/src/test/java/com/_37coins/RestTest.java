package com._37coins;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Form;

import junit.framework.Assert;

import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restnucleus.dao.Model;
import org.restnucleus.filter.HmacFilter;
import org.restnucleus.test.DbHelper;
import org.restnucleus.test.EmbeddedJetty;

import com._37coins.helper.HelperResource;
import com._37coins.parse.CommandParser;
import com._37coins.parse.ParserAction;
import com._37coins.parse.ParserClient;
import com._37coins.persistence.dao.Account;
import com._37coins.persistence.dao.Gateway;
import com._37coins.resources.AccountResource;
import com._37coins.resources.EnvayaSmsResource;
import com._37coins.resources.GatewayResource;
import com._37coins.resources.HealthCheckResource;
import com._37coins.resources.MerchantResource;
import com._37coins.resources.ParserResource;
import com._37coins.resources.TicketResource;
import com._37coins.web.AccountRequest;
import com._37coins.web.MerchantRequest;
import com._37coins.web.MerchantResponse;
import com._37coins.web.PriceTick;
import com._37coins.web.Seller;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Withdrawal;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class RestTest {
    public static List<Gateway> list = null;
	static String address = "821038492849";
	static String pw = "test9900";
	static Account acc1;
	
    private static EmbeddedJetty embeddedJetty;
    private static GoogleAnalytics ga;

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

        GoogleAnalyticsConfig gac = new GoogleAnalyticsConfig();
		gac.setEnabled(false);
		ga = new GoogleAnalytics(gac,"UA-123456");
	      //prepare data
		Gateway gw = new Gateway().setEmail("extraterrestrialintelligence@gmail.com").setApiSecret("test9900").setMobile("+821027423984").setFee(new BigDecimal("0.0007")).setCn("OZV4N1JS2Z3476NL").setLocale(new Locale("ko","KR"));
        List<Gateway> rv = new ArrayList<>();
        rv.add(gw);
        rv.add(new Gateway().setEmail("johannbarbie@me.com").setApiSecret("test9900").setMobile("+491602742398").setFee(new BigDecimal("0.002")).setCn("DEV4N1JS2Z3476DE").setLocale(new Locale("de","DE")));
        rv.add(new Gateway().setEmail("stefano@mail.com").setApiSecret("test9900").setMobile("+393602742398").setFee(new BigDecimal("0.002")).setCn("ITV4N1JS2Z3476DE").setLocale(new Locale("it","IT")));
        List<Account> ac = new ArrayList<>();
        ac.add(new Account().setMobile("+821039841235").setDisplayName("merchant").setOwner(gw).setApiSecret("test").setApiToken("test"));
        Map<Class<? extends Model>, List<? extends Model>> data = new HashMap<>();
        data.put(Gateway.class, rv);
        data.put(Account.class, ac);
        RestTest.list = rv;
        new DbHelper(embeddedJetty.getDao()).persist(data);
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
    
    public String json(Object o) throws IOException{
    	try {
			return new ObjectMapper().writeValueAsString(o);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    @Test
	public void testParserClient() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser(),ga);
		parserClient.start("+821039842742", "+821027423984", "+821027423984", "send 100 +821039842743", 8087,
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
    	//flush
    	given()
			.contentType(ContentType.JSON)
		.when()
			.post(embeddedJetty.getBaseUri() + HelperResource.PATH+"/init");
    	//run invite
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser(),ga);
		parserClient.start("+821039841234", null, "", Action.SIGNUP.toString(), 8087,
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
		Assert.assertEquals("NZV4N1JS2Z3476NK",ds.getTo().getGateway());
		Assert.assertNotNull(ds.getCn());
    }
    
    @Test
    public void testWebInvitePrefered() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
        //flush
        given()
            .contentType(ContentType.JSON)
        .when()
            .post(embeddedJetty.getBaseUri() + HelperResource.PATH+"/init");
        //run invite
        final DataSet ds = new DataSet();
        ParserClient parserClient = new ParserClient(new CommandParser(),ga);
        parserClient.start("+821039841233", null, "PZV4N1JS2Z3476NM", Action.SIGNUP.toString(), 8087,
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
        Assert.assertEquals("PZV4N1JS2Z3476NM",ds.getTo().getGateway());
        Assert.assertNotNull(ds.getCn());
    }
    
    @Test
	public void testVoiceReq() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser(),ga);
		parserClient.start("+821039841235", "+821027423984", "+821027423984", Action.VOICE.toString(), 8087,
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
		Assert.assertEquals(new Locale("ko","KR"),ds.getLocale());
		Assert.assertNotNull(ds.getCn());
    }

    @Test
	public void testCharge() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser(),ga);
		parserClient.start("+821039841234", "+821027423984", "+821027423984", "req 0.01", 8087,
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
    	ParserClient parserClient = new ParserClient(new CommandParser(),ga);
		parserClient.start("+821039841234", "+821027423984", "+821027423984", "send 1 jangkim321@gmail.com", 8087,
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
	public void testForeightGateway() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser(),ga);
		parserClient.start("+491039841234", "+821027423984", "+821027423984", "send 1 +821123723984", 8087,
		new ParserAction() {
			@Override
			public void handleResponse(DataSet data) {ds.setAction(data.getAction());}
			
			@Override
			public void handleWithdrawal(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleDeposit(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleConfirm(DataSet data) {ds.setAction(data.getAction());}
		});
		parserClient.join();
		Assert.assertTrue("unexpected Response",ds.getAction()==null);
    }
    
    @Test
	public void testPayedNumber() throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException{
    	final DataSet ds = new DataSet();
    	ParserClient parserClient = new ParserClient(new CommandParser(),ga);
		parserClient.start("+3940047374", "+393602742398", "+393602742398", "some shit here", 8087,
		new ParserAction() {
			@Override
			public void handleResponse(DataSet data) {ds.setAction(data.getAction());}
			
			@Override
			public void handleWithdrawal(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleDeposit(DataSet data) {ds.setAction(data.getAction());}
			@Override
			public void handleConfirm(DataSet data) {ds.setAction(data.getAction());}
		});
		parserClient.join();
		Assert.assertTrue("unexpected Response",ds.getAction()==null);
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
    public void testHealthcheck() throws IOException{
    	given()
		.expect()
			.statusCode(200)
		.when()
			.get(embeddedJetty.getBaseUri() + HealthCheckResource.PATH);
    }
    
    @Test
    public void testTicket() throws IOException {
    	//flush
    	given()
			.contentType(ContentType.JSON)
		.when()
			.post(embeddedJetty.getBaseUri() + HelperResource.PATH);
    	//get a ticket
    	Response r = given()
			.contentType(ContentType.JSON)
		.expect()
			.statusCode(200)
			.body("key", equalToIgnoringCase("ticket"))
			.body("value", notNullValue())
		.when()
			.post(embeddedJetty.getBaseUri() + TicketResource.PATH);
    	Map<String,String> rv = new ObjectMapper().readValue(r.asInputStream(), new TypeReference<Map<String,String>>(){});
    	//validate
    	given()
			.contentType(ContentType.JSON)
			.queryParam("ticket", rv.get("value"))
		.expect()
			.statusCode(200)
			.body("value", equalToIgnoringCase("active"))
		.when()
			.get(embeddedJetty.getBaseUri() + TicketResource.PATH);
    	//get another ticket
    	given()
			.contentType(ContentType.JSON)
		.expect()
			.statusCode(200)
			.body("key", equalToIgnoringCase("ticket"))
			.body("value", notNullValue())
		.when()
			.post(embeddedJetty.getBaseUri() + TicketResource.PATH);
    	//validate old ticket
    	given()
			.contentType(ContentType.JSON)
			.queryParam("ticket", rv.get("value"))
		.expect()
			.statusCode(200)
			.body("value", equalToIgnoringCase("active"))
		.when()
			.get(embeddedJetty.getBaseUri() + TicketResource.PATH);
    	//get a third ticket
    	given()
			.contentType(ContentType.JSON)
		.expect()
			.statusCode(200)
			.body("key", equalToIgnoringCase("ticket"))
			.body("value", notNullValue())
		.when()
			.post(embeddedJetty.getBaseUri() + TicketResource.PATH);
    	//get blocked
    	given()
			.contentType(ContentType.JSON)
		.expect()
			.statusCode(400)
		.when()
			.post(embeddedJetty.getBaseUri() + TicketResource.PATH);
    }
    
    @Test
    public void testEmailIsFree() throws IOException{
    	//test taken
    	String rv = given()
    		.contentType(ContentType.JSON)
    		.queryParam("email", "johannbarbie@me.com")
		.expect()
			.statusCode(200)
		.when()
			.get(embeddedJetty.getBaseUri() + AccountResource.PATH+"/check").asString();
    	Assert.assertEquals(rv, "false");
    	//test invalid
    	rv = given()
			.contentType(ContentType.JSON)
			.queryParam("email", "test@bla")
		.expect()
			.statusCode(200)
		.when()
			.get(embeddedJetty.getBaseUri() + AccountResource.PATH+"/check").asString();
    	Assert.assertEquals(rv, "false");
    	//test taken
    	rv = given()
    		.contentType(ContentType.JSON)
    		.queryParam("email", "test2@bp.org")
		.expect()
			.statusCode(200)
		.when()
			.get(embeddedJetty.getBaseUri() + AccountResource.PATH+"/check").asString();
    	Assert.assertEquals(rv, "true");
    }
    
    @Test
    public void testSignup() throws IOException {
    	//flush
    	given()
			.contentType(ContentType.JSON)
		.when()
			.post(embeddedJetty.getBaseUri() + HelperResource.PATH);
    	//data
    	AccountRequest request = new AccountRequest()
        		.setPassword("password");
    	//try without ticket
    	given()
			.body(json(request))
			.contentType(ContentType.JSON)
		.expect()
			.statusCode(417)
		.when()
			.post(embeddedJetty.getBaseUri() + AccountResource.PATH);
    	//get a ticket
    	Response r = given()
			.contentType(ContentType.JSON)
		.expect()
			.statusCode(200)
			.body("value", notNullValue())
		.when()
			.post(embeddedJetty.getBaseUri() + TicketResource.PATH);
    	Map<String,String> rv = new ObjectMapper().readValue(r.asInputStream(), new TypeReference<Map<String,String>>(){});
    	//try with bad email
    	given()
			.body(json(request.setEmail("test@bla").setTicket(rv.get("value"))))
			.contentType(ContentType.JSON)
		.expect()
			.statusCode(400)
		.when()
			.post(embeddedJetty.getBaseUri() + AccountResource.PATH);
    	//get a ticket
    	r = given()
			.contentType(ContentType.JSON)
		.expect()
			.statusCode(200)
			.body("value", notNullValue())
		.when()
			.post(embeddedJetty.getBaseUri() + TicketResource.PATH);
    	rv = new ObjectMapper().readValue(r.asInputStream(), new TypeReference<Map<String,String>>(){});
    	//register
    	given()
    		.body(json(request.setTicket(rv.get("value")).setEmail("test3@37coins.com")))
    		.contentType(ContentType.JSON)
		.expect()
			.statusCode(204)
		.when()
			.post(embeddedJetty.getBaseUri() + AccountResource.PATH);
    	//fetch email content
    	String token = given().contentType(ContentType.JSON).when()
			.get(embeddedJetty.getBaseUri() + HelperResource.PATH).asString();
    	//confirm account creation
    	given()
    		.body(json(new AccountRequest().setToken(token)))
    		.contentType(ContentType.JSON)
		.expect()
			.statusCode(204)
		.when()
			.post(embeddedJetty.getBaseUri() + AccountResource.PATH+"/create");
    	//login in see if it works
        given()
            .auth().basic("test3@37coins.com", "password")
            .contentType(ContentType.JSON)
        .expect()
            .statusCode(200)
        .when()
            .get(embeddedJetty.getBaseUri() + GatewayResource.PATH);    	
    }
    
    /**
     * make sure module is started with: mvn jetty:run -Denvironment=test -DhmacToken=
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void testMerchant() throws JsonParseException, JsonMappingException, IOException, NoSuchAlgorithmException{
    	MerchantRequest req = new MerchantRequest().setAmount(new BigDecimal("0.5")).setOrderName("bla");
    	String serverUrl = embeddedJetty.getBaseUri() + MerchantResource.PATH + "/charge/test";
    	req.setPayDest(new PaymentAddress().setAddressType(PaymentType.BTC).setAddress("123565"));
		String sig = HmacFilter.calculateSignature(serverUrl, HmacFilter.parseJson(new ObjectMapper().writeValueAsBytes(req)), MessagingServletConfig.hmacToken);
    	Response r = given()
    		.contentType(ContentType.JSON)
    		.header("X-Request-Signature", sig)
    		.body(json(req))
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + MerchantResource.PATH+"/charge/test");
    	MerchantResponse mr = new ObjectMapper().readValue(r.asInputStream(), MerchantResponse.class);
    	System.out.println(new ObjectMapper().writeValueAsString(mr));
    	Assert.assertNotNull(mr.getDisplayName());
    	Assert.assertNotNull(mr.getToken());
    }
	
	@Test
	public void testUser() throws JsonParseException, JsonMappingException, IOException{
    	//flush
    	given()
			.contentType(ContentType.JSON)
		.when()
			.post(embeddedJetty.getBaseUri() + HelperResource.PATH+"/init");
		//ask help
		Response r = given()
			.formParam("from", "+821027423983")
			.formParam("gateway", "+821027423984")
			.formParam("message", "help")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Help");
		List<DataSet> rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",2, rv.size());
		Assert.assertEquals("821027423983", rv.get(0).getCn());
		Assert.assertEquals("OZV4N1JS2Z3476NL", rv.get(0).getTo().getGateway());
		Assert.assertEquals(Action.SIGNUP, rv.get(1).getAction());
		//get btc address
		r = given()
			.formParam("from", "+821027423983")
			.formParam("gateway", "+821027423984")
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
			.formParam("from", "+821027423983")
			.formParam("gateway", "+821027423984")
			.formParam("message", "send 10 +821053215679")
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
		Assert.assertEquals(new BigDecimal("0.0007").setScale(8), w.getFee());
		Assert.assertEquals("OZV4N1JS2Z3476NL", w.getFeeAccount());
		Assert.assertEquals(Action.SIGNUP, rv.get(1).getAction());
		Assert.assertEquals("NZV4N1JS2Z3476NK", rv.get(1).getTo().getGateway());
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
			.formParam("from", "+491601234567")
			.formParam("gateway", "+491602742398")
			.formParam("message", "preis 5eur")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Price");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",2, rv.size());
		Assert.assertEquals(Action.PRICE, rv.get(0).getAction());
		PriceTick pt = (PriceTick)rv.get(0).getPayload();
		Assert.assertEquals("EUR", pt.getCurCode());
		Assert.assertNotNull(pt.getLastFactored());
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
		pt = (PriceTick)rv.get(0).getPayload();
		Assert.assertEquals("KRW", pt.getCurCode());
		Assert.assertNotNull(pt.getLast());
		Assert.assertNull(pt.getLastFactored());
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
			.formParam("gwCn", "OZV4N1JS2Z3476NL")
			.formParam("message", "balance")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/Balance");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.BALANCE, rv.get(0).getAction());
		//prevent sending zero
		r = given()
			.formParam("from", "+821027423983")
			.formParam("gateway", "+821027423984")
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
			.formParam("from", "+821027423983")
			.formParam("gateway", "+821027423984")
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
		Assert.assertEquals("OZV4N1JS2Z3476NL", w.getFeeAccount());
		Assert.assertEquals(BigDecimal.ZERO, w.getAmount());
		//use currency code for sending
		r = given()
			.formParam("from", "+821027423983")
			.formParam("gateway", "+821027423984")
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
		Assert.assertEquals("OZV4N1JS2Z3476NL", w.getFeeAccount());
		//send money, new user, same country
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "send 10 01087654321")
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
			.formParam("gwCn", "OZV4N1JS2Z3476NL")
			.formParam("message", "send 10 +491607654321")
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
		Assert.assertEquals("491607654321", rv.get(1).getCn());
		//send money, new user, other country, no gateway
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "send 10 +639177639690")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.DST_ERROR, rv.get(0).getAction());
		//charge a customer
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("gwCn", "OZV4N1JS2Z3476NL")
			.formParam("message", "req 10")
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
			.formParam("from", "+491607654321")
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
			.formParam("message", "conf a1234")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalConf");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_CONF, rv.get(0).getAction());
		Assert.assertEquals("a1234", rv.get(0).getPayload());
		//confirm with sign
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "a1234")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalConf");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_CONF, rv.get(0).getAction());
		Assert.assertEquals("a1234", rv.get(0).getPayload());
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
			.formParam("message", "send 0.1 +821012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.BELOW_FEE, rv.get(0).getAction());
	}

}

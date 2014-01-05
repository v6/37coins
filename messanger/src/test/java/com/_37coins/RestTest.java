package com._37coins;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.Withdrawal;
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
	public void testParserClient() throws NoSuchAlgorithmException, UnsupportedEncodingException{
    	ParserClient parserClient = new ParserClient(new CommandParser());
		parserClient.start("+821039842742", "+821027423984", "send 0.1 +821039842743", 8087,
		new ParserAction() {
			@Override
			public void handleWithdrawal(DataSet data) {
				//save the transaction id to db
				try {
					System.out.println(new ObjectMapper().writeValueAsString(data));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				Assert.assertFalse(data.getTo().getGateway().contains("+"));
			}
			@Override
			public void handleResponse(DataSet data) {
				try {
					System.out.println(new ObjectMapper().writeValueAsString(data));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				Assert.assertFalse(data.getTo().getGateway().contains("+"));
			}
			
			@Override
			public void handleDeposit(DataSet data) {
				try {
					System.out.println(new ObjectMapper().writeValueAsString(data));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				Assert.assertFalse(data.getTo().getGateway().contains("+"));
			}
			
			@Override
			public void handleConfirm(DataSet data) {
				try {
					System.out.println(new ObjectMapper().writeValueAsString(data));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				Assert.assertFalse(data.getTo().getGateway().contains("+"));
			}
		});
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
			.formParam("message", "send 0.01 test2@test.com")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",2, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		Withdrawal w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("test2@test.com", w.getPayDest().getAddress());
		Assert.assertEquals(new BigDecimal("0.01").setScale(8), w.getAmount());
		Assert.assertEquals(new BigDecimal("0.0001").setScale(8), w.getFee());
		Assert.assertEquals("MAILN1JS2Z34MAIL", w.getFeeAccount());
		Assert.assertEquals(Action.SIGNUP, rv.get(1).getAction());
		Assert.assertEquals("mail@37coins.com", rv.get(1).getTo().getGateway());
		Assert.assertEquals("test2@test.com", rv.get(1).getCn());
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
		//send money from mobile to email
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "send 0.01 test@test.com")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReq");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("test@test.com", w.getPayDest().getAddress());
		Assert.assertEquals("OZV4N1JS2Z3476NL", w.getFeeAccount());
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
		//request money from email to mobile
		r = given()
			.formParam("from", "test@test.com")
			.formParam("gateway", "mail@37coins.com")
			.formParam("message", "request 0.01 +821012345678")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReqOther");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ_OTHER, rv.get(0).getAction());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("test@test.com", w.getPayDest().getAddress());
		Assert.assertEquals("OZV4N1JS2Z3476NL", w.getFeeAccount());
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
		Assert.assertEquals("+491027423984", rv.get(1).getTo().getGateway());
		Assert.assertEquals("491087654321", rv.get(1).getCn());
		//request money, existing user
		r = given()
			.formParam("from", "+821012345678")
			.formParam("gateway", "+821027423984")
			.formParam("message", "request 0.01 +491087654321")
		.expect()
			.statusCode(200)
		.when()
			.post(embeddedJetty.getBaseUri() + ParserResource.PATH+"/WithdrawalReqOther");
		rv = mapper.readValue(r.asInputStream(), new TypeReference<List<DataSet>>(){});
		Assert.assertEquals("size expected",1, rv.size());
		Assert.assertEquals(Action.WITHDRAWAL_REQ_OTHER, rv.get(0).getAction());
		Assert.assertEquals(49, rv.get(0).getTo().getPhoneNumber().getCountryCode());
		w = (Withdrawal)rv.get(0).getPayload();
		Assert.assertEquals("821012345678", w.getPayDest().getAddress());
		Assert.assertEquals(82, w.getMsgDest().getPhoneNumber().getCountryCode());
		Assert.assertEquals(new BigDecimal("0.01").setScale(8), w.getAmount());
		Assert.assertEquals(new BigDecimal("0.002").setScale(8), w.getFee());
		Assert.assertEquals("DEV4N1JS2Z3476DE", w.getFeeAccount());
		Assert.assertEquals("491087654321", rv.get(0).getCn());
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

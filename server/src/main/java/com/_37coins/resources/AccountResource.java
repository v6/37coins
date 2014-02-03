package com._37coins.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.BasicAccessAuthFilter;
import com._37coins.MessageFactory;
import com._37coins.MessagingServletConfig;
import com._37coins.parse.CommandParser;
import com._37coins.parse.ParserAction;
import com._37coins.parse.ParserClient;
import com._37coins.sendMail.MailServiceClient;
import com._37coins.web.AccountPolicy;
import com._37coins.web.AccountRequest;
import com._37coins.web.GatewayUser;
import com._37coins.web.PasswordRequest;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import freemarker.template.TemplateException;

@Path(AccountResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {
	public final static String PATH = "/account";
	public static Logger log = LoggerFactory.getLogger(AccountResource.class);
	
	private final Cache cache;
	
	final private InitialLdapContext ctx;
	
	private final NonTxWorkflowClientExternalFactoryImpl nonTxFactory;
	
	private final HttpServletRequest httpReq;
	
	private final AccountPolicy accountPolicy;
	
	private final ParserClient parserClient;
	
	private final MailServiceClient mailClient;
	
	private final MessageFactory msgFactory;
	
	private int localPort;

	@Inject
	public AccountResource(Cache cache,
			ServletRequest request,
			AccountPolicy accountPolicy,
			MailServiceClient mailClient,
			MessageFactory msgFactory,
			NonTxWorkflowClientExternalFactoryImpl nonTxFactory,
			ParserClient parserClient){
		this.cache = cache;
		httpReq = (HttpServletRequest)request;
		this.ctx = (InitialLdapContext)httpReq.getAttribute("ctx");
		this.accountPolicy = accountPolicy;
		this.mailClient = mailClient;
		this.msgFactory = msgFactory;
		this.parserClient = parserClient;
		this.nonTxFactory = nonTxFactory;
		localPort = httpReq.getLocalPort();
	}
	
	
	private String getRemoteAddress(){
		String addr = httpReq.getHeader("X-Forwarded-For");
		if (null==addr || addr.length()<7){
			addr = httpReq.getRemoteAddr();
		}
		return addr;
	}
	
	/**
	 * allow front-end to notify user about taken account 
	 * @param email
	 */
	@GET
	@Path("/check")
	public String checkEmail(@QueryParam("email") String email){
		//check it's a valid email
		if (!AccountPolicy.isValidEmail(email)){
			return "false"; //email not valid
		}
		//how to avoid account fishing?
		Element e = cache.get(getRemoteAddress());
		if (e!=null){
			if (e.getHitCount()>50){
				return "false"; //to many requests
			}
		}
		//check it's not taken already
		try{
			ctx.setRequestControls(null);
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setTimeLimit(1);
			NamingEnumeration<?> namingEnum = null;
			namingEnum = ctx.search("ou=gateways,"+MessagingServletConfig.ldapBaseDn, "(&(objectClass=person)(mail="+email+"))", searchControls);
			if (namingEnum.hasMore()){
				return "false";//email used
			}
		} catch (IllegalStateException | NamingException e1) {
			e1.printStackTrace();
			return "false";//ldap error
		}
		return "true";
	}
	
	
	@POST
	@Path("/invite")
	public Map<String, String> invite(GatewayUser gu){
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		try{
			PhoneNumber pn = phoneUtil.parse(gu.getMobile(), "ZZ");
			String mobile = phoneUtil.format(pn, PhoneNumberFormat.E164);
			parserClient.start(mobile, null, Action.SIGNUP.toString(), localPort,
			new ParserAction() {
				@Override
				public void handleWithdrawal(DataSet data) {
				}
				@Override
				public void handleResponse(DataSet data) {
					if (data.getAction()==Action.SIGNUP){
						nonTxFactory.getClient(data.getAction()+"-"+data.getCn()).executeCommand(data);
					}else{
						throw new WebApplicationException("no gateway or number issue",
								javax.ws.rs.core.Response.Status.BAD_REQUEST);
					}
				}
				@Override
				public void handleDeposit(DataSet data) {
				}
				@Override
				public void handleConfirm(DataSet data) {
				}
			});
		}catch(NumberParseException e){
			throw new WebApplicationException("no gateway or number issue",
					javax.ws.rs.core.Response.Status.BAD_REQUEST);			
		}
		for (int i = 0;i<15;i++){
			Element e = cache.get("address"+gu.getMobile().replace("+", ""));
			if (null==e){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					throw new WebApplicationException(e1,Response.Status.INTERNAL_SERVER_ERROR);
				}
			}else{
				Map<String,String> rv = new HashMap<>();
				rv.put("address", (String)e.getObjectValue());
			}
		}
		throw new WebApplicationException("no timely response",
				javax.ws.rs.core.Response.Status.NOT_FOUND);
	}
	
	
	@DELETE
	@Path("/gateways")
	public void deleteGateways(){
		NamingEnumeration<?> namingEnum = null;
		try {
			ctx.setRequestControls(null);
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setTimeLimit(1000);
			searchControls.setReturningAttributes(new String[]{"mail","mobile","createTimestamp","cn"});
			namingEnum = ctx.search("ou=gateways,"
					+ MessagingServletConfig.ldapBaseDn,
					"(objectClass=person)", searchControls);
			
			while (namingEnum.hasMore()) {
				Attributes atts = ((SearchResult) namingEnum.next())
						.getAttributes();
				String cn = (String) atts.get("cn").get();
				//ignore system accounts
				if (cn.length()<15)
					continue;
				String mail = (null!=atts.get("mail"))?(String) atts.get("mail").get():null;
				String mobile = (null!=atts.get("mobile"))?(String) atts.get("mobile").get():null;
				String createTimestamp = (null!=atts.get("createTimestamp"))?(String) atts.get("createTimestamp").get():null;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
				Date createdDate = sdf.parse(createTimestamp);
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.DAY_OF_YEAR,-7);
				//ignore all new accounts
				if (createdDate.after(cal.getTime()))
					continue;
				if (null==mobile){
					//phone never verified
					deleteAccounts(cn);
					sendDeleteEmail(mail);
				}else{
					NamingEnumeration<?> children = ctx.search("ou=accounts,"
							+ MessagingServletConfig.ldapBaseDn,
							"(&(objectClass=person)(manager=cn="+cn+",ou=gateways,"+MessagingServletConfig.ldapBaseDn+"))", searchControls);
					if (!children.hasMore()){
						deleteAccounts(cn);
						sendDeleteEmail(mail);
					}
					children.close();
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (null!=namingEnum){
				try {
					namingEnum.close();
				} catch (NamingException e) {
				}
			}
		}
	}
	
	private void deleteAccounts(String cn) throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, NamingException, IOException{
		ConnectionFactory factory = new ConnectionFactory();
		Connection conn = null;
		Channel channel = null;
		factory.setUri(MessagingServletConfig.queueUri);
		conn = factory.newConnection();
		channel = conn.createChannel();
		try{
			channel.queueDelete(cn);
		}catch(Exception e){
		}finally{
			try{
			channel.close();
			conn.close();
			}catch(Exception ex){}
		}
		ctx.unbind("cn="+cn+",ou=gateways,"+MessagingServletConfig.ldapBaseDn);
	}
	
	/**
	 * an account-request is validated, and cached, then email is send
	 * @param accountRequest
	 */
	@POST
	public void register(AccountRequest accountRequest){
		// no ticket, no service
		if (null==cache.get("ticket"+accountRequest.getTicket())){
			log.debug("ticket required for this operation.");
			throw new WebApplicationException("ticket required for this operation.", Response.Status.EXPECTATION_FAILED);
		}
		//#############validate email#################
		//check regex
		if (null==accountRequest.getEmail() || !AccountPolicy.isValidEmail(accountRequest.getEmail())){
			log.debug("send a valid email plz :D");
			throw new WebApplicationException("send a valid email plz :D", Response.Status.BAD_REQUEST);
		}
		//check it's not taken already
		try{
			ctx.setRequestControls(null);
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setTimeLimit(1);
			NamingEnumeration<?> namingEnum = null;
			namingEnum = ctx.search("ou=gateways,"+MessagingServletConfig.ldapBaseDn, "(&(objectClass=person)(mail="+accountRequest.getEmail()+"))", searchControls);
			if (namingEnum.hasMore()){
				throw new WebApplicationException("email taken already.", Response.Status.CONFLICT);
			}
		} catch (IllegalStateException | NamingException e1) {
			e1.printStackTrace();
			throw new WebApplicationException(e1, Response.Status.INTERNAL_SERVER_ERROR);
		}
		if (accountPolicy.isEmailMxLookup()){
			//check db for active email with same domain
			String hostName = accountRequest.getEmail().substring(accountRequest.getEmail().indexOf("@") + 1, accountRequest.getEmail().length());
			try{
				ctx.setRequestControls(null);
				SearchControls searchControls = new SearchControls();
				searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				searchControls.setTimeLimit(1000);
				NamingEnumeration<?> namingEnum = ctx.search(MessagingServletConfig.ldapBaseDn, "(&(objectClass=person)(email=*@"+hostName+"))", searchControls);
				if (!namingEnum.hasMore()){
					//check host mx record
					boolean isValidMX = false;
					try{
						isValidMX = AccountPolicy.isValidMX(accountRequest.getEmail());
					}catch(Exception e){
						System.out.println("EmailRes.->check: "+ accountRequest.getEmail() + " not valid due: " + e.getMessage());
					}
					if(!isValidMX ){
						throw new WebApplicationException("This email's hostname does not have mx record.", Response.Status.BAD_REQUEST);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		//################validate password############
		boolean isValid = accountPolicy.validatePassword(accountRequest.getPassword());
		if (!isValid){
			log.debug("password does not pass account policy");
			throw new WebApplicationException("password does not pass account policy", Response.Status.BAD_REQUEST);
		}
		//put it into cache, and wait for email validation
		String token = RandomStringUtils.random(14, "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789");
		try {
			sendCreateEmail(accountRequest.getEmail() ,token);
		} catch (MessagingException | IOException| TemplateException e1) {
			e1.printStackTrace();
			throw new WebApplicationException(e1,Response.Status.INTERNAL_SERVER_ERROR);
		}
		AccountRequest ar = new AccountRequest()
			.setEmail(accountRequest.getEmail())
			.setPassword(accountRequest.getPassword());
		cache.put(new Element("create"+token,ar));
	}
	
	/**
	 * an account-request is taken from cache and put into the database
	 * @param accountRequest
	 */
	@POST
	@Path("/create")
	public void createAccount(AccountRequest accountRequest){
		Element e = cache.get("create"+accountRequest.getToken());
		if (null!=e){
			accountRequest = (AccountRequest)e.getObjectValue();
			//build a new user and same
			Attributes attributes=new BasicAttributes();
			Attribute objectClass=new BasicAttribute("objectClass");
			objectClass.add("inetOrgPerson");
			attributes.put(objectClass);
			Attribute sn=new BasicAttribute("sn");
			Attribute cn=new BasicAttribute("cn");
			String cnString = RandomStringUtils.random(16, "ABCDEFGHJKLMNPQRSTUVWXYZ123456789");
			sn.add(cnString);
			cn.add(cnString);
			attributes.put(sn);
			attributes.put(cn);
			attributes.put("mail", accountRequest.getEmail());
			attributes.put("userPassword", accountRequest.getPassword());
			try{
				ctx.createSubcontext("cn="+cnString+",ou=gateways,"+MessagingServletConfig.ldapBaseDn, attributes);
			}catch(NamingException ex){
				throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
			}
			cache.remove("create"+accountRequest.getToken());
		}else{
			throw new WebApplicationException("not found or expired", Response.Status.NOT_FOUND);
		}
	}
	
	/**
	 * a password-request is validated, cached and email send out
	 * @param pwRequest
	 */
	@POST
	@Path("/password/request")
	public void requestPwReset(PasswordRequest pwRequest){
		// no ticket, no service
		Element e = cache.get("ticket"+pwRequest.getTicket());
		if (null==e){
			throw new WebApplicationException("ticket required for this request.", Response.Status.BAD_REQUEST);
		}else{
			if (e.getHitCount()>3){
				cache.remove("ticket"+pwRequest.getTicket());
				throw new WebApplicationException("to many requests", Response.Status.BAD_REQUEST);
			}
		}
		//fetch account by email, then send email
		String dn = null;
		try {
			Attributes atts = BasicAccessAuthFilter.searchUnique("(&(objectClass=person)(mail="+pwRequest.getEmail()+"))", ctx).getAttributes();
			dn = "cn="+atts.get("cn").get()+",ou=gateways,"+MessagingServletConfig.ldapBaseDn;
		} catch (IllegalStateException | NamingException e1) {
			e1.printStackTrace();
			throw new WebApplicationException("account not found", Response.Status.NOT_FOUND);
		}
		String token = RandomStringUtils.random(14, "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789");
		try {
			sendResetEmail(pwRequest.getEmail(), token);
		} catch (MessagingException | IOException| TemplateException e1) {
			e1.printStackTrace();
			throw new WebApplicationException(e1,Response.Status.INTERNAL_SERVER_ERROR);
		}
		PasswordRequest pwr = new PasswordRequest().setToken(token).setDn(dn);
		cache.put(new Element("reset"+token, pwr));
	}
	
	/**
	 * a password-request is taken from the cache and executed, then account-changes persisted
	 * @param pwRequest
	 */
	@POST
	@Path("/password/reset")
	public void reset(PasswordRequest pwRequest){
		Element e = cache.get("reset"+pwRequest.getToken());
		if (null!=e){
			String newPw = pwRequest.getPassword();
			boolean isValid = accountPolicy.validatePassword(newPw);
			if (!isValid)
				throw new WebApplicationException("password does not pass account policy", Response.Status.BAD_REQUEST);
			pwRequest = (PasswordRequest)e.getObjectValue();
			
			Attributes toModify = new BasicAttributes();
			toModify.put("userPassword", newPw);
			try{
				ctx.modifyAttributes(pwRequest.getDn(), DirContext.REPLACE_ATTRIBUTE, toModify);
			}catch(Exception ex){
				ex.printStackTrace();
				throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
			}

			cache.remove("reset"+pwRequest.getToken());
		}else{
			throw new WebApplicationException("not found or expired", Response.Status.NOT_FOUND);
		}
	}
	
	private void sendResetEmail(String email, String token) throws AddressException, MessagingException, IOException, TemplateException{
		DataSet ds = new DataSet()
			.setLocale(Locale.ENGLISH)
			.setAction(Action.RESET)
			.setPayload(MessagingServletConfig.basePath+"#confReset/"+token);
		mailClient.send(
			msgFactory.constructSubject(ds), 
			email,
			MessagingServletConfig.senderMail, 
			msgFactory.constructTxt(ds),
			msgFactory.constructHtml(ds));
	}
	
	private void sendDeleteEmail(String email) throws AddressException, MessagingException, IOException, TemplateException{
		DataSet ds = new DataSet()
			.setLocale(Locale.ENGLISH)
			.setAction(Action.ACCOUNT_DELETE);
		mailClient.send(
			msgFactory.constructSubject(ds), 
			email,
			MessagingServletConfig.senderMail, 
			msgFactory.constructTxt(ds),
			msgFactory.constructHtml(ds));
	}
	
	private void sendCreateEmail(String email, String token) throws AddressException, MessagingException, IOException, TemplateException{
		DataSet ds = new DataSet()
			.setLocale(Locale.ENGLISH)
			.setAction(Action.REGISTER)
			.setPayload(MessagingServletConfig.basePath+"#confSignup/"+token);
		mailClient.send(
			msgFactory.constructSubject(ds), 
			email,
			MessagingServletConfig.senderMail, 
			msgFactory.constructTxt(ds),
			msgFactory.constructHtml(ds));
	}
	
}

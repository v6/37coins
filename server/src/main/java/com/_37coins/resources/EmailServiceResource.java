package com._37coins.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.RandomStringUtils;

import com._37coins.BasicAccessAuthFilter;
import com._37coins.MessagingServletConfig;
import com._37coins.sendMail.MailTransporter;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.EmailFactor;
import com._37coins.workflow.pojo.MessageAddress;
import com._37coins.workflow.pojo.MessageAddress.MsgType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;

@Path(EmailServiceResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class EmailServiceResource {
	public final static String PATH = "/email";
	
	private final MailTransporter mt;
	private final Cache cache;
	private final HttpServletRequest httpReq;
	final private InitialLdapContext ctx;
	final private Locale locale;
	
	@Inject
	public EmailServiceResource(MailTransporter mt,
			ServletRequest request,
			Cache cache){
		this.mt = mt;
		this.cache = cache;
		httpReq = (HttpServletRequest)request;
		this.ctx = (InitialLdapContext)httpReq.getAttribute("ctx");
		String acceptLng = httpReq.getHeader("Accept-Language");
		locale = DataSet.parseLocaleString(acceptLng);
	}
	
	@POST
	@Path("/consume")
	public void cosume(EmailFactor emailFactor){
		if (null==emailFactor.getTaksToken()||null==emailFactor.getCn()){
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		Attributes atts = null;
		List<String> otp = null;
		String mail = null;
		String sanitizedCn = BasicAccessAuthFilter.escapeDN(emailFactor.getCn());
		try {
			atts = ctx.getAttributes("cn="+sanitizedCn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn,new String[]{"mail","description"});
			String description = (atts.get("description")!=null)?(String)atts.get("description").get():null;
			mail = (atts.get("mail")!=null)?(String)atts.get("mail").get():null;
			if (null==description || description.length()<6){
				throw new WebApplicationException("otp not set up.", Response.Status.PRECONDITION_FAILED);
			}
			try {
				otp = new ObjectMapper().readValue(description, new TypeReference<List<String>>(){});
			} catch (IOException e) {
				e.printStackTrace();
				throw new WebApplicationException("otp not set up.", Response.Status.PRECONDITION_FAILED);
			}
		} catch (IllegalStateException | NamingException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
		if (null==otp || null==mail){
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
		boolean found = false;
		for (String s : otp){
			if(s.equals(emailFactor.getTaksToken())){
				found = true;
				break;
			}
		}
		if (found){
			otp.remove(emailFactor.getTaksToken());
			try {
				String otpString = new ObjectMapper().writeValueAsString(otp);
				Attributes toModify = new BasicAttributes();
		    	toModify.put("description", otpString);
		    	ctx.modifyAttributes("cn="+sanitizedCn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn, DirContext.REPLACE_ATTRIBUTE, toModify);
			} catch (JsonProcessingException | NamingException ex) {
				ex.printStackTrace();
				throw new WebApplicationException(ex,Response.Status.INTERNAL_SERVER_ERROR);
			}
			if (otp.size()<4){
				renewOTP(emailFactor.setEmail(mail));
			}
		}else{
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}
	
	
	@POST
	@Path("/verify")
	public EmailFactor sendVerification(EmailFactor emailFactor){
		if (null==emailFactor.getEmail()||null==emailFactor.getCn()||null==emailFactor.getEmailToken())
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		String sanitizedMail = BasicAccessAuthFilter.escapeLDAPSearchFilter(emailFactor.getEmail());
		//check it's not verified already
		try{
			ctx.setRequestControls(null);
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setTimeLimit(1);
			NamingEnumeration<?> namingEnum = null;
			namingEnum = ctx.search(MessagingServletConfig.ldapBaseDn, "(&(objectClass=person)(mail="+sanitizedMail+"))", searchControls);
			if (namingEnum.hasMore()){
				throw new WebApplicationException("email taken already.", Response.Status.CONFLICT);
			}
		} catch (IllegalStateException | NamingException e1) {
			e1.printStackTrace();
			throw new WebApplicationException(e1, Response.Status.INTERNAL_SERVER_ERROR);
		}
		
		DataSet ds = new DataSet()
			//.setAction(Action.EMAIL_VER)
			.setLocale(locale)
			.setTo(new MessageAddress()
					.setAddressType(MsgType.EMAIL))
			.setPayload(emailFactor.getEmailToken());
		try {
			ds.getTo().setEmail(new InternetAddress(sanitizedMail));
			mt.sendMessage(ds);
		} catch (IOException | TemplateException| MessagingException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
		String emailServiceToken = RandomStringUtils.random(10, "abcdefghijkmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ123456789");
		cache.put(new Element("emailService"+emailServiceToken,emailFactor));
		return new EmailFactor().setEmailToken(emailServiceToken);
	}
	
	@POST
	@Path("/confirm")
	public void confirm(EmailFactor token){
		Element e = cache.get("emailService"+token.getEmailToken());
		if (null == e ){
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		EmailFactor ef = (EmailFactor) e.getObjectValue();
		try{
	    	Attributes toModify = new BasicAttributes();
	    	String sanitizedMail = BasicAccessAuthFilter.escapeLDAPSearchFilter(ef.getEmail());
	    	toModify.put("mail", sanitizedMail);
	    	String sanitizedCn = BasicAccessAuthFilter.escapeDN(ef.getCn());
	    	ctx.modifyAttributes("cn="+sanitizedCn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn, DirContext.REPLACE_ATTRIBUTE, toModify);
		}catch(NamingException ex){
			ex.printStackTrace();
			throw new WebApplicationException(ex,Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@POST
	@Path("/renew")
	public void renewOTP(EmailFactor emailFactor){
		String sanitizedMail = BasicAccessAuthFilter.escapeLDAPSearchFilter(emailFactor.getEmail());
		String sanitizedCn = BasicAccessAuthFilter.escapeDN(emailFactor.getCn());
		//read from ldap and verify request
		Attributes atts = null;
		try {
			atts = ctx.getAttributes("cn="+sanitizedCn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn,new String[]{"mail"});
			String mail = (String)atts.get("mail").get();
			if (!mail.equals(sanitizedMail)){
				throw new IllegalStateException("mail is "+mail + ", but requested "+sanitizedMail);
			}
		} catch (IllegalStateException | NamingException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.NOT_FOUND);
		}
		//generate otp
		List<String> otp = new ArrayList<>();
		for (int i = 0; i< 30;i++){
			otp.add(RandomStringUtils.random(4, "0123456789"));
		}
		//update ldap
		try {
			String otpString = new ObjectMapper().writeValueAsString(otp);
			Attributes toModify = new BasicAttributes();
	    	toModify.put("description", otpString);
	    	ctx.modifyAttributes("cn="+sanitizedCn+",ou=accounts,"+MessagingServletConfig.ldapBaseDn, DirContext.REPLACE_ATTRIBUTE, toModify);
		} catch (JsonProcessingException | NamingException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex,Response.Status.INTERNAL_SERVER_ERROR);
		}
		//prepare email data
		DataSet ds = new DataSet()
		//.setAction(Action.EMAIL)
		.setLocale(locale)
		.setTo(new MessageAddress()
				.setAddressType(MsgType.EMAIL))
		.setPayload(otp);
		//sed email
		try {
			ds.getTo().setEmail(new InternetAddress(sanitizedMail));
			mt.sendMessage(ds);
		} catch (IOException | TemplateException| MessagingException e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}

package com._37coins.resources;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessagingServletConfig;
import com._37coins.cache.Cache;
import com._37coins.cache.Element;

@Path(TicketResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public class TicketResource {
	public final static String PATH = "/ticket";
	public final static String TICKET_SCOPE = "account";
	public final static String TICKET_CHARS = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789";
	public final static String REQUEST_SCOPE = "remoteHost";
	public final static int TICKET_LENGTH = 14;
	public static Logger log = LoggerFactory.getLogger(TicketResource.class);
		
	private final Cache cache;
		
	private final HttpServletRequest httpReq;

	@Inject
	public TicketResource(Cache cache,
				ServletRequest request){
			this.cache = cache;
			httpReq = (HttpServletRequest)request;
	}
	
	/**
	 * a ticket is a token to execute critical or expensive code, like sending email.
	 * a ticket will be given out a few times free, then limited by turing tests.
	 */
	@POST
	public Pair<String,String> getTicket(){
	    String token = TicketResource.REQUEST_SCOPE+TicketResource.getRemoteAddress(httpReq);
	    long c = cache.incr(token);
	    if (c>=3){
				//TODO: implement turing test
				throw new WebApplicationException("to many requests", Response.Status.BAD_REQUEST);
		}else if (c==0){
			cache.put(new Element(token,TicketResource.getRemoteAddress(httpReq)));
		}
		String ticket = RandomStringUtils.random(TICKET_LENGTH, TICKET_SCOPE);
		cache.put(new Element(TICKET_SCOPE+ticket,ticket));
		return Pair.of("ticket",ticket);
	}
	
	/**
	 * recaptcha
	 */
	@POST
	@Path("/captcha")
	public Pair<String,String> recaptcha(@FormParam("chal") String challenge,
			@FormParam("resp") String response){
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey(MessagingServletConfig.captchaSecKey);
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(TicketResource.getRemoteAddress(httpReq), challenge, response);
        if (reCaptchaResponse.isValid()) {
        	cache.remove(TicketResource.getRemoteAddress(httpReq));
    		String ticket = RandomStringUtils.random(TICKET_LENGTH, TICKET_SCOPE);
    		cache.put(new Element(TICKET_SCOPE+ticket,ticket));
    		return Pair.of("ticket",ticket);
        } else {
          	throw new WebApplicationException("error", Response.Status.BAD_REQUEST);
        }
	}
	
	
	public static String getRemoteAddress(HttpServletRequest httpReq){
		String addr = httpReq.getHeader("X-Forwarded-For");
		if (null==addr || addr.length()<7){
			addr = httpReq.getRemoteAddr();
		}
		return addr;
	}
	
	public static void consume(Cache cache, String ticket){
		cache.remove(TICKET_SCOPE+ticket);
	}
	
	@GET
	public Pair<String,String> validateToken(@QueryParam("ticket") String ticket){
		Element e = cache.getQuiet(TICKET_SCOPE+ticket);
		if (null!=e){
			return Pair.of("status", "active");
		}else{
			return Pair.of("status", "inactive");
		}
	}

}

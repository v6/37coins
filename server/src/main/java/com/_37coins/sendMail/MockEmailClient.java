package com._37coins.sendMail;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com._37coins.cache.Cache;
import com._37coins.cache.Element;

public class MockEmailClient implements MailServiceClient {
	
	private final Cache cache;
	
	public MockEmailClient(Cache cache){
		this.cache = cache;
	}

	@Override
	public void send(String subject, String receiver, String sender,
			String text, String html) throws AddressException,
			MessagingException {
		// TODO Auto-generated method stub
		System.out.println(text);
		String token = text.substring(text.lastIndexOf("/")+1, text.length());
		System.out.println(token);
		cache.put(new Element("testHelper",token));
	}

}

package com._37coins.resources;

import java.io.IOException;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.MessageFactory;
import com._37coins.plivo.Speak;
import com._37coins.plivo.XmlCharacterHandler;
import com._37coins.workflow.pojo.DataSet;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import freemarker.template.TemplateException;

@Path(PlivoResource.PATH)
@Produces(MediaType.APPLICATION_XML)
public class PlivoResource {
	public static Logger log = LoggerFactory.getLogger(PlivoResource.class);
	public final static String PATH = "/plivo";
	
	private final MessageFactory msgFactory;
	
	private Marshaller marshaller;
	
	@Inject public PlivoResource(
			ServletRequest request,
			MessageFactory msgFactory) {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		this.msgFactory = msgFactory;
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(com._37coins.plivo.Response.class);
			this.marshaller = jc.createMarshaller();
	        marshaller.setProperty(CharacterEscapeHandler.class.getName(),new XmlCharacterHandler());
		} catch (JAXBException e) {
			log.error("jaxb exception",e);
			e.printStackTrace();
		}
		
	}
	
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/answer/{locale}")
	public Response answer(
			@PathParam("locale") String locale){
		com._37coins.plivo.Response rv = null;
		DataSet ds = new DataSet().setLocaleString(locale);
		try {
			rv = new com._37coins.plivo.Response()
				.add(new Speak().setText(msgFactory.getText("VoiceHello",ds)+" "+msgFactory.getText("VoiceSetup",ds)).setLanguage(locale));
		} catch (IOException | TemplateException e) {
			log.error("plivo answer exception",e);
			e.printStackTrace();
		}
		try {
			StringWriter sw = new StringWriter();
			marshaller.marshal(rv, sw);
			return Response.ok(sw.toString(), MediaType.APPLICATION_XML).build();
		} catch (JAXBException e) {
			return null;
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/hangup")
	public void hangup(){
		System.out.println("done: "+System.currentTimeMillis());
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/ring")
	public void ring(){
		System.out.println("ring: "+System.currentTimeMillis());
	}
	

}

package com._37coins.envaya;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com._37coins.MessageFactory;
import com._37coins.workflow.pojo.DataSet;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.inject.Inject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import freemarker.template.TemplateException;

public class QueueClient {
	public static Logger log = LoggerFactory.getLogger(QueueClient.class);
	
	private Connection connection = null;
	private Channel channel = null;
	final MessageFactory msgFactory;
	
	@Inject
	public QueueClient(MessageFactory msgFactory){
		this.msgFactory = msgFactory;
	}
	
	private void connect(String uri, String exchangeName)  throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(uri);
		connection = factory.newConnection();
		channel = connection.createChannel();
	}
	
	public void send(DataSet rsp, String uri, String gateway, String exchangeName, String id) throws IOException, TemplateException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
		MDC.put("hostName", rsp.getTo().getGateway());
		MDC.put("mobile", PhoneNumberUtil.getInstance().format(rsp.getTo().getPhoneNumber(), PhoneNumberFormat.E164));
		MDC.put("event", "outgoing");
		MDC.put("message_type", "sms");
		MDC.put("message_action", rsp.getAction().getText());
		log.info("sending outgoing message");
		if (null==connection || !connection.isOpen()){
			connect(uri, exchangeName);
		}
		String message = StringEscapeUtils.escapeJava(msgFactory.constructTxt(rsp));
		String msg = "{\"event\":\"send\",\"messages\":[{\"id\":\""+id+"\",\"to\":\""+rsp.getTo().getAddress()+"\",\"message\":\""+message+"\"}]}";
		channel.basicPublish(exchangeName,gateway,null, msg.getBytes());
	}
	
	public void close(){
		try {
			channel.close();
		} catch (IOException e) {
		} finally{
			try {
				connection.close();
			} catch (IOException e) {
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}

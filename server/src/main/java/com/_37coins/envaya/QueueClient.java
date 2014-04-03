package com._37coins.envaya;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com._37coins.MessageFactory;
import com._37coins.workflow.pojo.DataSet;
import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.GoogleAnalytics;
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
	private final GoogleAnalytics ga;
	
	@Inject
	public QueueClient(MessageFactory msgFactory,
			GoogleAnalytics ga){
		this.ga = ga;
		this.msgFactory = msgFactory;
	}
	
	private void connect(String uri, String exchangeName)  throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(uri);
		connection = factory.newConnection();
		channel = connection.createChannel();
	}
	
	public void send(DataSet rsp, String uri, String gateway, String exchangeName, String id) throws IOException, TemplateException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
		String from = PhoneNumberUtil.getInstance().format(rsp.getTo().getPhoneNumber(), PhoneNumberFormat.E164);
		MDC.put("hostName", rsp.getTo().getGateway());
		MDC.put("mobile", from);
		MDC.put("event", "outgoing");
		MDC.put("message_type", "sms");
		MDC.put("message_action", rsp.getAction().getText());
		log.info("sending outgoing message");
		MDC.clear();
		String uuid = UUID.nameUUIDFromBytes(from.getBytes()).toString();
		uuid = uuid.substring(0, 14)+"4"+uuid.substring(15);//violates google analytics terms, as it is not randomA
		ga.postAsync(new EventHit("amqp", "outgoing", rsp.getAction().getText(), 0).clientId(uuid));
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

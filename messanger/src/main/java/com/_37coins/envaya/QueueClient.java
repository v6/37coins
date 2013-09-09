package com._37coins.envaya;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com._37coins.pojo.SendAction;
import com.google.inject.Inject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import freemarker.template.TemplateException;

public class QueueClient {
	
	private Connection connection = null;
	private Channel channel = null;
	final MessageFactory msgFactory;
	final SendAction sendAction;
	
	@Inject
	public QueueClient(MessageFactory msgFactory, SendAction sendAction){
		this.msgFactory = msgFactory;
		this.sendAction = sendAction;
	}
	
	private void connect(String uri, String exchangeName)  throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(uri);
		connection = factory.newConnection();
		channel = connection.createChannel();
	}
	
	public void send(Map<String, Object> cmd, String uri, String gateway, String exchangeName, String id) throws IOException, TemplateException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
		if (null==connection || !connection.isOpen()){
			connect(uri, exchangeName);
		}
		String message = StringEscapeUtils.escapeJava(msgFactory.construct(cmd, sendAction));
		String msg = "{\"event\":\"send\",\"messages\":[{\"id\":\""+id+"\",\"to\":\""+cmd.get("msgAddress")+"\",\"message\":\""+message+"\"}]}";
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

package com._37coins.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com._37coins.workflow.pojo.DataSet;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import freemarker.template.TemplateException;

public class ConfigTester {
	
	private Connection connection = null;
	private Channel channel = null;
	
	private void connect(String uri, String exchangeName)  throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(uri);
		connection = factory.newConnection();
		channel = connection.createChannel();
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, KeyManagementException, TemplateException, URISyntaxException {
		ConfigTester ct = new ConfigTester();
		ct.send(null, "url", "cn","amq.direct", null);
	}
	
	public void send(DataSet rsp, String uri, String gateway, String exchangeName, String id) throws IOException, TemplateException, KeyManagementException, NoSuchAlgorithmException, URISyntaxException {
		if (null==connection || !connection.isOpen()){
			connect(uri, exchangeName);
		}
		String msg = "{\"event\":\"settings\",\"settings\":{\"server_url\":\"https://www.37coins.com/envayasms/cn/sms\"}}";
		channel.basicPublish(exchangeName,gateway,null, msg.getBytes());
	}

}

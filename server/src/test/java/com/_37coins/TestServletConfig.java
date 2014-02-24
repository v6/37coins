package com._37coins;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContextEvent;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.restnucleus.log.SLF4JTypeListener;

import com._37coins.envaya.QueueClient;
import com._37coins.parse.AbuseFilter;
import com._37coins.parse.CommandParser;
import com._37coins.parse.InterpreterFilter;
import com._37coins.parse.ParserAccessFilter;
import com._37coins.parse.ParserClient;
import com._37coins.parse.ParserFilter;
import com._37coins.util.FiatPriceProvider;
import com._37coins.web.GatewayUser;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.WithdrawalWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.EmailFactor;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class TestServletConfig extends GuiceServletContextListener {
	
	public static Injector injector;
	
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		// TODO Auto-generated method stub
		super.contextInitialized(servletContextEvent);
		Cache cache = injector.getInstance(Cache.class);
		Set<GatewayUser> gw = new HashSet<>();
		gw.add(new GatewayUser().setMobile("+491606789123").setId("DEV4N1JS2Z3476DE"));
		gw.add(new GatewayUser().setMobile("+821027423984").setId("OZV4N1JS2Z3476NL"));
		cache.put(new Element("gateways",gw));
		cache.put(new Element("emailVersmsemail",new EmailFactor().setTaskToken("taskToken").setEmailToken("bla")));
	}

	@Override
	protected Injector getInjector() {
		final String restUrl = "http://localhost:8080";
		 injector = Guice.createInjector(new ServletModule(){
	            @Override
	            protected void configureServlets(){
	            	filter("/envayasms/*").through(DirectoryFilter.class);
	            	filter("/parser/*").through(ParserAccessFilter.class); //make sure no-one can access those urls
	            	filter("/parser/*").through(ParserFilter.class); //read message into dataset
	            	filter("/parser/*").through(AbuseFilter.class);    //prohibit overuse
	            	filter("/parser/*").through(DirectoryFilter.class); //allow directory access
	            	filter("/parser/*").through(InterpreterFilter.class); //do semantic stuff
	            	bindListener(Matchers.any(), new SLF4JTypeListener());
	            	bind(ParserClient.class);
	        	}
				
				@Provides
				@Singleton
				@SuppressWarnings("unused")
				public CommandParser getMessageProcessor() {
				  return new CommandParser();
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				public JndiLdapContextFactory provideLdapClientFactory(){
					JndiLdapContextFactory jlc = new JndiLdapContextFactory();
					jlc.setUrl(MessagingServletConfig.ldapUrl);
					jlc.setAuthenticationMechanism("simple");
					jlc.setSystemUsername(MessagingServletConfig.ldapUser);
					jlc.setSystemPassword(MessagingServletConfig.ldapPw);
					return jlc;
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				public NonTxWorkflowClientExternalFactoryImpl getDWorkflowClientExternal(
				    AmazonSimpleWorkflow workflowClient) {
				  return new NonTxWorkflowClientExternalFactoryImpl(
				      workflowClient, restUrl);
				}
				@Provides @Singleton @SuppressWarnings("unused")
				public WithdrawalWorkflowClientExternalFactoryImpl getSWorkflowClientExternal(
				    AmazonSimpleWorkflow workflowClient) {
				  return new WithdrawalWorkflowClientExternalFactoryImpl(
				      workflowClient, restUrl);
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				AmazonSimpleWorkflow getSimpleWorkflowClient() {
				  return new AmazonSimpleWorkflowClient();
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				public MessageFactory provideMessageFactory() {
					return new MessageFactory();
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				public FiatPriceProvider provideFiatPrices(Cache cache){
					return new FiatPriceProvider(cache);
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				public SocketIOServer provideSocket(){
				 	Configuration config = new Configuration();
				    config.setPort(8081);
				    SocketIOServer server = new SocketIOServer(config);
				    return server;
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				public QueueClient provideMessageFactory(MessageFactory mf) {
					return new QueueClient(mf);
				}	
		       	@Provides @Singleton @SuppressWarnings("unused")
	        	public Cache provideCache(){
	        		//Create a singleton CacheManager using defaults
	        		CacheManager manager = CacheManager.create();
	        		//Create a Cache specifying its configuration.
	        		Cache testCache = new Cache(new CacheConfiguration("cache", 1000)
	        		    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
	        		    .eternal(false)
	        		    .timeToLiveSeconds(7200)
	        		    .timeToIdleSeconds(3600)
	        		    .diskExpiryThreadIntervalSeconds(0));
	        		  manager.addCache(testCache);
	        		  return testCache;
	        	}
			});
		return injector;
	}

}

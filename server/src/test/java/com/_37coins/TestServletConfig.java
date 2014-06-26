package com._37coins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.restnucleus.PersistenceConfiguration;
import org.restnucleus.filter.CorsFilter;
import org.restnucleus.filter.DigestFilter;
import org.restnucleus.filter.PersistenceFilter;
import org.restnucleus.filter.QueryFilter;
import org.restnucleus.log.SLF4JTypeListener;

import com._37coins.cache.Cache;
import com._37coins.envaya.QueueClient;
import com._37coins.helper.MockMerchantClient;
import com._37coins.helper.WrapFilter;
import com._37coins.merchant.MerchantClient;
import com._37coins.parse.AbuseFilter;
import com._37coins.parse.CommandParser;
import com._37coins.parse.InterpreterFilter;
import com._37coins.parse.ParserClient;
import com._37coins.parse.ParserFilter;
import com._37coins.sendMail.MailServiceClient;
import com._37coins.sendMail.MockEmailClient;
import com._37coins.util.FiatPriceProvider;
import com._37coins.util.ResourceBundleClient;
import com._37coins.util.ResourceBundleFactory;
import com._37coins.web.AccountPolicy;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.WithdrawalWorkflowClientExternalFactoryImpl;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.maxmind.geoip.LookupService;

public class TestServletConfig extends GuiceServletContextListener {
	
	public static Injector injector;
	private ServletContext servletContext;
	
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
	}

	@Override
	protected Injector getInjector() {
		final String restUrl = "http://localhost:8087";
		 injector = Guice.createInjector(new ServletModule(){
	            @Override
	            protected void configureServlets(){
	                filter("/*").through(CorsFilter.class);
	                filter("/*").through(GuiceShiroFilter.class);
	                filter("/api/*").through(QueryFilter.class);
	                filter("/api/*").through(PersistenceFilter.class);
	            	filter("/envayasms/*").through(PersistenceFilter.class);
	            	filter("/parser/*").through(WrapFilter.class);
	            	filter("/parser/*").through(ParserFilter.class); //read message into dataset
	            	filter("/parser/*").through(AbuseFilter.class);    //prohibit overuse
	            	filter("/parser/*").through(PersistenceFilter.class); //allow directory access
	            	filter("/parser/*").through(InterpreterFilter.class); //do semantic stuff
	            	filter("/account*").through(PersistenceFilter.class); //allow directory access
	            	filter("/merchant/*").through(PersistenceFilter.class);
	            	filter("/email/*").through(PersistenceFilter.class); //allow directory access
	            	bindListener(Matchers.any(), new SLF4JTypeListener());
	            	bind(ParserClient.class);
	            	bind(QueueClient.class);
	            	bind(WrapFilter.class);
	        	}
				
				@Provides
				@Singleton
				@SuppressWarnings("unused")
				public CommandParser getMessageProcessor(ResourceBundleFactory rbf) {
				  return new CommandParser(rbf);
				}
				
	            @Provides @Singleton @SuppressWarnings("unused")
	            public ParserFilter getParserFilter(FiatPriceProvider fiatPriceProvider) {
	                return new ParserFilter(fiatPriceProvider, MessagingServletConfig.unitFactor, MessagingServletConfig.unitName);
	            }
				
	            @Provides @Singleton @SuppressWarnings("unused")
	            public DigestFilter getDigestFilter(){
	                return new DigestFilter(MessagingServletConfig.digestToken);
	            }
				
				@Provides @Singleton @SuppressWarnings("unused")
				MerchantClient provideMerchantClient(){
				    return new MockMerchantClient("bla","bla");
				}
				
                @Provides @Singleton @SuppressWarnings("unused")
                CorsFilter provideCorsFilter(){
                    return new CorsFilter("*");
                }
				
	            @Provides @Singleton @SuppressWarnings("unused")
	            PersistenceManagerFactory providePersistence(){
	                PersistenceConfiguration pc = new PersistenceConfiguration();
	                pc.createEntityManagerFactory();
	                return pc.getPersistenceManagerFactory();
	            }
				
				@Provides @Singleton @SuppressWarnings("unused")
				AccountPolicy providePolicy(){
					return new AccountPolicy().setEmailMxLookup(false);
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
	            public ResourceBundleClient getResourceBundleClient(){
	                ResourceBundleClient client = new ResourceBundleClient(MessagingServletConfig.resPath+"/scripts/nls/");
	                return client;
	            }
	            
	            @Provides @Singleton @SuppressWarnings("unused")
	            public ResourceBundleFactory getResourceBundle(com._37coins.cache.Cache cache, ResourceBundleClient client){
	                return new ResourceBundleFactory(MessagingServletConfig.activeLocales, client, cache);
	            }
				
				@Provides @Singleton @SuppressWarnings("unused")
				public MessageFactory provideMessageFactory(ResourceBundleFactory rbf) {
					return new MessageFactory(null,rbf,1000,"mBTC","#,##0.###");
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				public FiatPriceProvider provideFiatPrices(Cache cache){
	                return new FiatPriceProvider(cache, restUrl + "/helper");
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				MailServiceClient getMailClient(Cache cache){
					return new MockEmailClient(cache);
				}
				
				@Provides @Singleton @SuppressWarnings("unused")
				public GoogleAnalytics getGoogleAnalytics(){
					GoogleAnalyticsConfig gac = new GoogleAnalyticsConfig();
					gac.setEnabled(false);
					GoogleAnalytics ga = new GoogleAnalytics(gac,"UA-123456");
					return ga;
	        	}
				
				@Provides @Singleton @SuppressWarnings("unused")
				LookupService getLookupService(){
					LookupService cl = null;
					ClassLoader loader = null;
					try {
						if (null==servletContext){
							File file = new File(MessageFactory.LOCAL_RESOURCE_PATH+"../classes");
							URL[] urls = {file.toURI().toURL()};
							loader = new URLClassLoader(urls);
						}else{
							loader = MessageFactory.class.getClassLoader();
						}
						File file = new File(loader.getResource("maxmind/GeoIP.dat").getFile());
						cl = new LookupService(file, LookupService.GEOIP_MEMORY_CACHE);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return cl;
				}
				
		       	@Provides @Singleton @SuppressWarnings("unused")
	        	public Cache provideCache(){
	        		//Create a singleton CacheManager using defaults
	        		CacheManager manager = CacheManager.create();
	        		//Create a Cache specifying its configuration.
	        		net.sf.ehcache.Cache testCache = new net.sf.ehcache.Cache(new CacheConfiguration("cache", 1000)
	        		    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
	        		    .eternal(false)
	        		    .timeToLiveSeconds(7200)
	        		    .timeToIdleSeconds(3600)
	        		    .diskExpiryThreadIntervalSeconds(0));
	        		  manager.addCache(testCache);
	        		  
	        		  return new EhCacheWrapper(testCache);
	        	}
			},new MessagingShiroWebModule(this.servletContext));
		return injector;
	}

}

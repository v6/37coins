package com._37coins;

import java.util.Random;

import javax.inject.Named;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.restnucleus.PersistenceConfiguration;
import org.restnucleus.filter.DigestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class MerchantServletConfig extends GuiceServletContextListener {
	
	public static String digestToken;
	public static String plivoKey;
	public static String plivoSecret;
	public static String basePath;
	public static Logger log = LoggerFactory.getLogger(MerchantServletConfig.class);
	public static Injector injector;
	private ServletContext servletContext;
	static {
		digestToken = System.getProperty("digestToken");
		plivoKey = System.getProperty("plivoKey");
		plivoSecret = System.getProperty("plivoSecret");
		basePath = System.getProperty("pBasePath");
	}
    
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
		log.info("ServletContextListener started");
	}
	
    @Override
    protected Injector getInjector(){
        injector = Guice.createInjector(new ServletModule(){
            @Override
            protected void configureServlets(){
            	filter("/product*").through(DigestFilter.class);
            	//filter("/pwallet*").through(PersistenceFilter.class);
            	//filter("/pwallet/claim").through(HmacFilter.class);
        	}
            
			@Provides @Singleton @SuppressWarnings("unused")
			PersistenceManagerFactory providePersistence(){
				PersistenceConfiguration pc = new PersistenceConfiguration();
				pc.createEntityManagerFactory();
				return pc.getPersistenceManagerFactory();
			}

    	    @Provides @Singleton @SuppressWarnings("unused")
    		public Random provideRandom(){
    			return new Random();
        	}
			
        	@Provides @Singleton @SuppressWarnings("unused")
        	public String provideHmacToken(){
        		return MerchantServletConfig.digestToken;
        	}
        	
			@Provides @Singleton @SuppressWarnings("unused")
			public MessageFactory provideMessageFactory() {
				return new MessageFactory(servletContext);
			}
        
            @Named("day")
        	@Provides @Singleton @SuppressWarnings("unused")
        	public Cache provideCache(){
        		//Create a singleton CacheManager using defaults
        		CacheManager manager = CacheManager.create();
        		//Create a Cache specifying its configuration.
        		Cache testCache = new Cache(new CacheConfiguration("day", 1000)
        		    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
        		    .eternal(false)
        		    .timeToLiveSeconds(79200) //22 hours, reset daily
        		    .timeToIdleSeconds(57600) //16hours, a really long biz day 
        		    .diskExpiryThreadIntervalSeconds(0));
        		  manager.addCache(testCache);
        		  return testCache;
        	}
        
			@Named("hour")
	    	@Provides @Singleton @SuppressWarnings("unused")
	    	public Cache provideHourCache(){
	    		//Create a singleton CacheManager using defaults
	    		CacheManager manager = CacheManager.create();
	    		//Create a Cache specifying its configuration.
	    		Cache testCache = new Cache(new CacheConfiguration("hour", 1000)
	    		    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
	    		    .eternal(false)
	    		    .timeToLiveSeconds(7200)
	    		    .timeToIdleSeconds(3600)
	    		    .diskExpiryThreadIntervalSeconds(0));
	    		manager.addCache(testCache);
	    		return testCache;
	    	}});
        return injector;
    }
	
    @Override
	public void contextDestroyed(ServletContextEvent sce) {
		super.contextDestroyed(sce);
		log.info("ServletContextListener destroyed");
	}

}

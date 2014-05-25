package com._37coins;

import javax.inject.Named;
import javax.servlet.ServletContextEvent;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.restnucleus.filter.CorsFilter;
import org.restnucleus.filter.DigestFilter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class TestServletConfig extends GuiceServletContextListener {
	
	public static Injector injector;
	
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		// TODO Auto-generated method stub
		super.contextInitialized(servletContextEvent);
	}

	@Override
	protected Injector getInjector() {
		 injector = Guice.createInjector(new ServletModule(){
	            @Override
	            protected void configureServlets(){
	            	filter("/*").through(CorsFilter.class);
	            	filter("/charges*").through(DigestFilter.class);
	        	}	            
	            
	        	@Provides @Singleton @SuppressWarnings("unused")
	        	public String provideHmacToken(){
	        		return ProductsServletConfig.digestToken;
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
		    	}
			});
		return injector;
	}

}

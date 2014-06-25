package com._37coins;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.inject.Named;
import javax.servlet.ServletContextEvent;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.spy.memcached.MemcachedClient;

import org.restnucleus.filter.CorsFilter;
import org.restnucleus.filter.DigestFilter;

import com._37coins.cache.MemCacheWrapper;
import com._37coins.util.ResourceBundleClient;
import com._37coins.util.ResourceBundleFactory;
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
                public DigestFilter getDigestFilter(){
                    return new DigestFilter(MerchantServletConfig.digestToken);
                }
                   
                @Provides @Singleton @SuppressWarnings("unused")
                CorsFilter provideCorsFilter(){
                    return new CorsFilter("*");
                }
	            
	        	@Provides @Singleton @SuppressWarnings("unused")
	        	public String provideHmacToken(){
	        		return MerchantServletConfig.digestToken;
	        	}
	            
	            @Provides @Singleton @SuppressWarnings("unused")
	            public MessageFactory getMessageFactory(ResourceBundleFactory rbf){
	                return new MessageFactory(null,rbf,1000,"mBTC","#,##0.###");
	            }
	            
	            @Provides @Singleton @SuppressWarnings("unused")
                public ResourceBundleClient getResourceBundleClient(){
                    ResourceBundleClient client = new ResourceBundleClient(MerchantServletConfig.resPath+"/scripts/nls/");
                    return client;
                }
	            
                @Provides @Singleton @SuppressWarnings("unused")
                public ResourceBundleFactory getResourceBundle(com._37coins.cache.Cache cache, ResourceBundleClient client){
                    return new ResourceBundleFactory(MerchantServletConfig.activeLocales, client, cache);
                }
                
                @Provides @Singleton @SuppressWarnings("unused")
                public com._37coins.cache.Cache provideMemcached() throws IOException{
                    MemcachedClient client = new MemcachedClient(new InetSocketAddress(MerchantServletConfig.cacheHost, 11211));
                    com._37coins.cache.Cache cache = new MemCacheWrapper(client, 3600);
                    return cache;
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

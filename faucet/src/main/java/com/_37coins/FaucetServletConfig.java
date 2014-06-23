package com._37coins;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletContextEvent;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.cache.Cache;
import com._37coins.util.FiatPriceProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class FaucetServletConfig extends GuiceServletContextListener {
	public static String basePath;
	public static String envayaToken;
	public static String blockchainUrl;
	public static String faucetPath;
	public static Logger log = LoggerFactory.getLogger(FaucetServletConfig.class);
	public static Injector injector;

	static {
        basePath = System.getProperty("basePath");
        envayaToken = System.getProperty("envayaToken");
        faucetPath = System.getProperty("faucetPath");
        try {
            blockchainUrl = URLDecoder.decode(System.getProperty("blockchainUrl"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	}
    
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		super.contextInitialized(servletContextEvent);
		log.info("ServletContextListener started");
	}
	
    @Override
    protected Injector getInjector(){
        injector = Guice.createInjector(new ServletModule(){
            @Override
            protected void configureServlets(){
            	filter("/callback").through(DigestFilter.class);
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
            @Provides @Singleton @SuppressWarnings("unused")
            String provideUrl(){
                return FaucetServletConfig.blockchainUrl;
            }
            @Provides @Singleton @SuppressWarnings("unused")
            DigestFilter provideDigest(){
                return new DigestFilter(FaucetServletConfig.envayaToken, FaucetServletConfig.faucetPath);
            }
            @Provides @Singleton @SuppressWarnings("unused")
            FiatPriceProvider provideFiatPrices(Cache cache){
                return new FiatPriceProvider(cache, "http://api.bitcoinaverage.com/ticker/global/");
            }});
        return injector;
    }
	
    @Override
	public void contextDestroyed(ServletContextEvent sce) {
		super.contextDestroyed(sce);
		log.info("ServletContextListener destroyed");
	}

}

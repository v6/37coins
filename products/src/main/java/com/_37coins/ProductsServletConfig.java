package com._37coins;

import java.util.Calendar;
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

import com.google.bitcoin.core.BlockChain;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.PeerGroup;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.discovery.DnsDiscovery;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.store.MemoryBlockStore;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class ProductsServletConfig extends GuiceServletContextListener {
	
	public static String hmacToken;
	public static String plivoKey;
	public static String plivoSecret;
	public static String basePath;
	public static Logger log = LoggerFactory.getLogger(ProductsServletConfig.class);
	public static Injector injector;
	private ServletContext servletContext;
	private PeerGroup peerGroup;
	private BlockChain chain;
	private BlockStore blockStore;
	private Wallet wallet;
	static {
		hmacToken = System.getProperty("hmacToken");
		plivoKey = System.getProperty("plivoKey");
		plivoSecret = System.getProperty("plivoSecret");
		basePath = System.getProperty("pBasePath");
	}
    
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
		
		System.out.println("Connecting ...");
		wallet = injector.getInstance(Wallet.class);
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
        	public PeerGroup providePeerGroup(){
		NetworkParameters params = MainNetParams.get();
		blockStore = new MemoryBlockStore(params);
		try {
					chain = new BlockChain(params, blockStore);
				} catch (BlockStoreException e) {
					e.printStackTrace();
				}
            	peerGroup = new PeerGroup(MainNetParams.get());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		long now = cal.getTimeInMillis() / 1000;
		peerGroup.setFastCatchupTimeSecs(now);
        		peerGroup.setUserAgent("bip38 claimer", "0.1");
        		peerGroup.addPeerDiscovery(new DnsDiscovery(MainNetParams.get()));
		return peerGroup;
		}

	    @Provides @Singleton @SuppressWarnings("unused")
		public Wallet provideWallet(PeerGroup peerGroup){
		NetworkParameters params = MainNetParams.get();
		wallet=new Wallet(params);
			String dk;
		peerGroup.addWallet(wallet);
		peerGroup.startAsync();
		//peerGroup.downloadBlockChain();
		return wallet;
	    }

	    @Provides @Singleton @SuppressWarnings("unused")
		public Random provideRandom(){
			return new Random();
        	}
			
        	@Provides @Singleton @SuppressWarnings("unused")
        	public String provideHmacToken(){
        		return ProductsServletConfig.hmacToken;
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
    	peerGroup.stopAndWait();
		super.contextDestroyed(sce);
		log.info("ServletContextListener destroyed");
	}

}

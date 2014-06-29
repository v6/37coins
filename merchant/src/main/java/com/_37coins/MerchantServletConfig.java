package com._37coins;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.inject.Named;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.RandomStringUtils;
import org.restnucleus.PersistenceConfiguration;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.filter.CorsFilter;
import org.restnucleus.filter.DigestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.cache.MemCacheWrapper;
import com._37coins.parse.CommandParser;
import com._37coins.parse.ParserClient;
import com._37coins.sendMail.AmazonEmailClient;
import com._37coins.sendMail.MailServiceClient;
import com._37coins.util.ResourceBundleClient;
import com._37coins.util.ResourceBundleFactory;
import com._37coins.web.MerchantSession;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.call.Call;
import com.plivo.helper.exception.PlivoException;

public class MerchantServletConfig extends GuiceServletContextListener {
    public static AWSCredentials awsCredentials = null;
    public static String domainName;
    public static String endpoint;
	public static String digestToken;
	public static String plivoKey;
	public static String plivoSecret;
	public static String basePath;
	public static String cacheHost;
	public static String amqpUser;
    public static String amqpPassword;
    public static String senderMail;
    public static String amqpHost;
    public static String gaTrackingId;
    public static String resPath;
    public static List<Locale> activeLocales;
	public static Logger log = LoggerFactory.getLogger(MerchantServletConfig.class);
	public static Injector injector;
	public SocketIOServer server;
	private ServletContext servletContext;
    private ServiceLevelThread slt;
	static {
	    if (null!=System.getProperty("accessKey")){
           awsCredentials = new BasicAWSCredentials(
               System.getProperty("accessKey"),
               System.getProperty("secretKey"));
        }
        domainName = System.getProperty("swfDomain");
        endpoint = System.getProperty("endpoint");
		digestToken = System.getProperty("digestToken");
		plivoKey = System.getProperty("plivoKey");
		plivoSecret = System.getProperty("plivoSecret");
		basePath = System.getProperty("basePath");
		cacheHost = System.getProperty("cacheHost");
		senderMail = System.getProperty("senderMail");
        amqpUser = System.getProperty("amqpUser");
        amqpPassword = System.getProperty("amqpPassword");
        amqpHost = System.getProperty("amqpHost");
        resPath = System.getProperty("resPath");
        gaTrackingId = System.getProperty("gaTrackingId");
        String locales = System.getProperty("activeLocales");
        List<String> localeList = Arrays.asList(locales.split(","));
        activeLocales = new ArrayList<>();
        for (String localeString : localeList){
            Locale locale = DataSet.parseLocaleString(localeString);
            activeLocales.add(locale);
        }
	}
    
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
	    servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
		final Injector i = getInjector();
	    server = i.getInstance(SocketIOServer.class);
        server.addJsonObjectListener(MerchantSession.class, new DataListener<MerchantSession>() {
            @Override
            public void onData(SocketIOClient client, MerchantSession data, AckRequest ackRequest) {
                com._37coins.cache.Cache cache = i.getInstance(com._37coins.cache.Cache.class);
                if (null==data.getSessionToken()){
                    //add to monitoring list
                    //if exceeded limit, kick
                    client.sendJsonObject(new MerchantSession().setAction("unauthenticated"));
                    client.disconnect();
                    return;
                }
                //verify session
                Object e = cache.get("account"+data.getSessionToken());
                if (null==e){
                    //add to monitoring list
                    //if exceeded limit, kick
                    client.sendJsonObject(new MerchantSession().setAction("unauthenticated"));
                    client.disconnect();
                    return;
                }else{
                    client.sendJsonObject(new MerchantSession().setAction("authenticated"));
                }
                if (data.getAction().equals("subscribe")){
                    client.joinRoom(data.getSessionToken());
                    client.sendJsonObject(new MerchantSession().setAction("subscribed"));
                    return;
                }
                if (data.getAction().equals("logout")){
                    client.sendJsonObject(new MerchantSession().setAction("disconnected"));
                    client.disconnect();
                    return;
                }
                if (data.getAction().equals("verify")){
                    //verify data
                    String phone = data.getPhoneNumber();
                PhoneNumber phoneNumber=null;
                    if (null!=phone){
                        try {
                            phoneNumber = PhoneNumberUtil.getInstance().parse(phone, "ZZ");
                            phone = PhoneNumberUtil.getInstance().format(phoneNumber,PhoneNumberFormat.E164);
                        } catch (NumberParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    if (phone==null){
                        client.sendJsonObject(new MerchantSession().setAction("error"));
                        return;
                    }
                    String delivery = (data.getDelivery()==null||data.getDelivery().length()<2)?"display":data.getDelivery();
                    String deliveryParam = (data.getDeliveryParam()==null||data.getDeliveryParam().length()<2)?null:data.getDelivery();
                    if (!delivery.equals("display")&&(deliveryParam==null||deliveryParam.length()<2)){
                        client.sendJsonObject(new MerchantSession().setAction("error"));
                        return;
                    }
                    
                    //create tan
                    MerchantSession ms = new MerchantSession()
                        .setDelivery(delivery)
                        .setDeliveryParam(deliveryParam)
                        .setPhoneNumber(phone)
                        .setCallAction(data.getCallAction())
                        .setSessionToken(RandomStringUtils.random(4, "0123456789"));
                    cache.put(new com._37coins.cache.Element("merchant"+data.getSessionToken(),ms));
                    //initialize call
                    try{
                        RestAPI restAPI = new RestAPI(MerchantServletConfig.plivoKey, MerchantServletConfig.plivoSecret, "v1");
                        
                        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    String from = PhoneNumberUtil.getInstance().format(phoneUtil.getExampleNumberForType(phoneUtil.getRegionCodeForCountryCode(phoneNumber.getCountryCode()), PhoneNumberType.MOBILE), PhoneNumberFormat.E164);
                    params.put("from", from.substring(0,from.length()-4)+"3737");
                        params.put("to", phone);
                        params.put("answer_url", MerchantServletConfig.basePath + "/plivo/merchant/req/"+data.getSessionToken()+"/"+ms.getSessionToken()+"/"+Locale.US.toString());
                        params.put("hangup_url", MerchantServletConfig.basePath + "/plivo/merchant/hangup/"+data.getSessionToken());
                        Call response = restAPI.makeCall(params);
                        if (response.serverCode != 200 && response.serverCode != 201 && response.serverCode !=204){
                            throw new PlivoException(response.message);
                        }
                    }catch(PlivoException ex){
                        ex.printStackTrace();
                        client.sendJsonObject(new MerchantSession().setAction("error"));
                    }
                    client.sendJsonObject(new MerchantSession().setAction("started").setSessionToken(ms.getSessionToken()));
                    return;
                }                               
            }
        });
        server.start();
        if (null==System.getProperty("environment")||!System.getProperty("environment").equals("test")){
            //handle service level thread
            slt = i.getInstance(ServiceLevelThread.class);
            slt.start();
        }
		log.info("ServletContextListener started");
	}
	
    @Override
    protected Injector getInjector(){
        injector = Guice.createInjector(new ServletModule(){
            @Override
            protected void configureServlets(){
                filter("/*").through(CorsFilter.class);
            	filter("/product*").through(DigestFilter.class);
            	bind(ParserClient.class);
        	}
            
            @Provides @Singleton @SuppressWarnings("unused")
            public CorsFilter provideCorsFilter(){
                return new CorsFilter("*");
            }
            
            @Provides @Singleton @SuppressWarnings("unused")
            public DigestFilter getDigestFilter(){
                return new DigestFilter(MerchantServletConfig.digestToken);
            }
            
			@Provides @Singleton @SuppressWarnings("unused")
			public PersistenceManagerFactory providePersistence(){
				PersistenceConfiguration pc = new PersistenceConfiguration();
				pc.createEntityManagerFactory();
				return pc.getPersistenceManagerFactory();
			}

            @Provides @RequestScoped  @SuppressWarnings("unused")
            public GenericRepository providePersistenceManager(PersistenceManagerFactory pmf){
                GenericRepository dao = new GenericRepository(pmf);
                dao.getPersistenceManager();
                return dao;
            }
			
	        @Provides @Singleton @SuppressWarnings("unused")
	        public CommandParser getMessageProcessor(ResourceBundleFactory rbf) {
	            return new CommandParser(rbf);
	        }
	        
            @Provides @Singleton @SuppressWarnings("unused")
            public MessageFactory getMessageFactory(ResourceBundleFactory rbf){
                return new MessageFactory(servletContext, rbf, 1000, "mBTC","#,##0.###");
            }
			
            @Provides @Singleton @SuppressWarnings("unused")
            public SocketIOServer provideSocket(){
                Configuration config = new Configuration();
                config.setPort(8081);
                SocketIOServer server = new SocketIOServer(config);
                return server;
            }
            

            @Provides @Singleton @SuppressWarnings("unused")
            public GoogleAnalytics getGoogleAnalytics(){
                GoogleAnalytics ga = new GoogleAnalytics(MerchantServletConfig.gaTrackingId);
                return ga;
            }
            
            @Provides @Singleton @SuppressWarnings("unused")
            AmazonSimpleWorkflow getSimpleWorkflowClient() {
                AmazonSimpleWorkflow rv = null;
                if (null!=awsCredentials){
                    rv = new AmazonSimpleWorkflowClient(awsCredentials);
                }else{
                    rv = new AmazonSimpleWorkflowClient();
                }
                rv.setEndpoint(endpoint);
                return rv;
            }
            
            @Provides @Singleton @SuppressWarnings("unused")
            public NonTxWorkflowClientExternalFactoryImpl getDWorkflowClientExternal(
                    AmazonSimpleWorkflow workflowClient) {
                return new NonTxWorkflowClientExternalFactoryImpl(
                        workflowClient, domainName);
            }

    	    @Provides @Singleton @SuppressWarnings("unused")
    		public Random provideRandom(){
    			return new Random();
        	}
        	
            @Provides @Singleton @SuppressWarnings("unused")
            MailServiceClient getMailClient(){
                if (awsCredentials==null){
                    return new AmazonEmailClient(
                            new AmazonSimpleEmailServiceClient());
                }else{
                    return new AmazonEmailClient(
                            new AmazonSimpleEmailServiceClient(awsCredentials));
                }
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
            
            @Provides @Singleton @SuppressWarnings("unused")
            public com._37coins.cache.Cache provideMemcached() throws IOException{
                MemcachedClient client = new MemcachedClient(new InetSocketAddress(MerchantServletConfig.cacheHost, 11211));
                com._37coins.cache.Cache cache = new MemCacheWrapper(client, 3600);
                return cache;
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
        if (null!=server){
            server.stop();
        }
        if (null!=slt){
            slt.kill();
        }
		super.contextDestroyed(sce);
		log.info("ServletContextListener destroyed");
	}

}

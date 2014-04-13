package com._37coins;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import me.moocar.logbackgelf.GelfAppender;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.restnucleus.filter.CorsFilter;
import org.restnucleus.log.SLF4JTypeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachable;

import com._37coins.bizLogic.NonTxWorkflowImpl;
import com._37coins.bizLogic.WithdrawalWorkflowImpl;
import com._37coins.envaya.QueueClient;
import com._37coins.envaya.ServiceLevelThread;
import com._37coins.imap.JavaPushMailAccount;
import com._37coins.parse.AbuseFilter;
import com._37coins.parse.CommandParser;
import com._37coins.parse.InterpreterFilter;
import com._37coins.parse.ParserAccessFilter;
import com._37coins.parse.ParserClient;
import com._37coins.parse.ParserFilter;
import com._37coins.sendMail.AmazonEmailClient;
import com._37coins.sendMail.MailServiceClient;
import com._37coins.sendMail.SmtpEmailClient;
import com._37coins.util.FiatPriceProvider;
import com._37coins.web.AccountPolicy;
import com._37coins.web.MerchantSession;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.WithdrawalWorkflowClientExternalFactoryImpl;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.maxmind.geoip.LookupService;

public class MessagingServletConfig extends GuiceServletContextListener {
	public static AWSCredentials awsCredentials = null;
	public static String domainName;
	public static String msgActListName = "message-activities-tasklist";
	public static String eposActListName = "epos-activities-tasklist";
	public static String endpoint;
	public static String senderMail;
	public static String smtpHost;
	public static String smtpUser;
	public static String smtpPassword;
	public static String imapHost;
	public static final int IMAP_PORT = 993;
	public static final boolean IMAP_SSL = true;
	public static String imapUser;
	public static String imapPassword;
	public static String basePath;
	public static String srvcPath;
	public static String queueUri;
	public static String amqpUser;
	public static String amqpPassword;
	public static String amqpHost;
	public static String plivoKey;
	public static String plivoSecret;
	public static String resPath;
	public static String merchantResPath;
	public static String ldapUrl;
	public static String ldapUser;
	public static String ldapPw;
	public static String ldapBaseDn;
	public static String captchaPubKey;
	public static String captchaSecKey;
	public static String elasticSearchHost;
	public static String paymentsPath;
	public static String gaTrackingId;
	public static String hmacToken;
	public static Logger log = LoggerFactory.getLogger(MessagingServletConfig.class);
	public static Injector injector;
	public static int localPort;
	static {
		if (null!=System.getProperty("accessKey")){
		awsCredentials = new BasicAWSCredentials(
				System.getProperty("accessKey"),
				System.getProperty("secretKey"));
		}
		domainName = System.getProperty("swfDomain");
		endpoint = System.getProperty("endpoint");
		senderMail = System.getProperty("senderMail");
		smtpHost = System.getProperty("smtpHost");
		smtpUser = System.getProperty("smtpUser");
		smtpPassword = System.getProperty("smtpPassword");
		//EMAIL SETTINGS
		imapHost = System.getProperty("imapHost");
		imapUser = System.getProperty("imapUser");
		imapPassword = System.getProperty("imapPassword");
		basePath = System.getProperty("basePath");
		srvcPath = System.getProperty("srvcPath");
		queueUri = System.getProperty("queueUri");
		amqpUser = System.getProperty("amqpUser");
		amqpPassword = System.getProperty("amqpPassword");
		amqpHost = System.getProperty("amqpHost");
		plivoKey = System.getProperty("plivoKey");
		plivoSecret = System.getProperty("plivoSecret");
		resPath = System.getProperty("resPath");
		merchantResPath = System.getProperty("merchantResPath");
		ldapUrl = System.getProperty("ldapUrl");
		ldapUser = System.getProperty("ldapUser");
		ldapPw = System.getProperty("ldapPw");
		ldapBaseDn = System.getProperty("ldapBaseDn");
		captchaPubKey = System.getProperty("captchaPubKey");
		captchaSecKey = System.getProperty("captchaSecKey");
		elasticSearchHost = System.getProperty("elasticSearchHost");
		paymentsPath = System.getProperty("paymentsPath");
		gaTrackingId = System.getProperty("gaTrackingId");
		hmacToken = System.getProperty("hmacToken");
	}
	
	private ServletContext servletContext;
	private ActivityWorker msgActivityWorker;
	private ActivityWorker eposActivityWorker;
	private WorkflowWorker depositWorker;
	private WorkflowWorker withdrawalWorker;
	private JavaPushMailAccount jPM;
	public SocketIOServer server;
	private ServiceLevelThread slt;
    
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContext = servletContextEvent.getServletContext();
		if (null==System.getProperty("environment")||!System.getProperty("environment").equals("test")){
			prepareLogging();
			//handle uncaught exceptions
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					log.error(t.getName(), e);
				}
			});
		}
		super.contextInitialized(servletContextEvent);
		final Injector i = getInjector();
		msgActivityWorker = i.getInstance(Key.get(ActivityWorker.class,
				Names.named("messaging")));
		msgActivityWorker.start();
		eposActivityWorker = i.getInstance(Key.get(ActivityWorker.class,
				Names.named("epos")));
		eposActivityWorker.start();
		depositWorker = i.getInstance(Key.get(WorkflowWorker.class,
				Names.named("nonTx")));
		depositWorker.start();
		withdrawalWorker = i.getInstance(Key.get(WorkflowWorker.class,
				Names.named("withdrawal")));
		withdrawalWorker.start();
		// set up receiving mails
		if (null!=imapUser){
			jPM = new JavaPushMailAccount(imapUser, imapHost, IMAP_PORT, IMAP_SSL);
			jPM.setCredentials(imapUser, imapPassword);
			jPM.setMessageCounterListerer(i.getInstance(EmailListener.class));
			jPM.run();
		}
		if (null==System.getProperty("environment")||!System.getProperty("environment").equals("test")){
			//handle service level thread
			slt = i.getInstance(ServiceLevelThread.class);
			slt.start();
		}
		
		server = i.getInstance(SocketIOServer.class);
		server.addJsonObjectListener(MerchantSession.class, new DataListener<MerchantSession>() {
	        @Override
	        public void onData(SocketIOClient client, MerchantSession data, AckRequest ackRequest) {
	        	Cache cache = i.getInstance(Cache.class);
	        	if (null==data.getSessionToken()){
	        		//if no session token available, start authentication
	        		//verify phone number
	        		if (null==data.getPhoneNumber()){
	        			client.sendJsonObject(new MerchantSession().setAction("failed"));
	        			client.disconnect();
	        			return;
	        		}
		        	try{
		    			//initialize plivo call
		        		String random = RandomStringUtils.random(4, "0123456789");
		        		client.sendJsonObject(new MerchantSession()
		        			.setOtp(random)
		        			.setAction("started"));
		    		}catch(Exception ex){
		    			log.info(client.getRemoteAddress()+" call initialization failed",ex);
		    			ex.printStackTrace();
		    			client.sendJsonObject(new MerchantSession().setAction("failed"));
		    			//add to monitoring list
		    			//if exceeded limit, kick
		    		}
	        	}else{
	        		//verify session
	        		Element e = cache.get("merchant"+data.getSessionToken());
	        		if (null==e){
	        			//add to monitoring list
	        			//if exceeded limit, kick
	        			client.sendJsonObject(new MerchantSession().setAction("failed"));
	        			client.disconnect();
	        			return;
	        		}
	        		MerchantSession session = (MerchantSession)e.getObjectValue();
	        		client.joinRoom(session.getPhoneNumber());
	        		//validate session data
	        		
	        		if (null!=data.getAction() && data.getAction().equals("charge") && null != data.getAmount()){
	        			//received charge from merchant BackView
	        			//initialize workflow
	        		}
	        		
	        		if (null!=data.getAction() && data.getAction().equals("txns")){
	        			//received request for previous transaction
	        			//initialize workflow
	        		}
	        		
	        		if (null!=data.getAction() && data.getAction().equals("logout")){
	        			cache.remove("merchant"+data.getSessionToken());
	        			client.sendJsonObject(new MerchantSession().setAction("logout"));
	        			client.disconnect();
	        		}
	        		
	        		if (null!=data.getAction() && data.getAction().equals("getState")){
	        			Element elem = cache.get("merchantState"+data.getSessionToken());
	        			if (null!=elem){
	        				client.sendJsonObject((MerchantSession)elem.getObjectValue());
	        			}
	        		}
	        	}
	        }
	    });
		server.start();
		log.info("ServletContextListener started");
	}
	
	private void prepareLogging(){
	    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)lc.getLogger(Logger.ROOT_LOGGER_NAME);
	    logger.setLevel(Level.INFO);
	    AppenderAttachable<ILoggingEvent> appenderAttachable = 
	    		   (AppenderAttachable<ILoggingEvent>) logger;
	    appenderAttachable.detachAndStopAllAppenders();
	    GelfAppender ga = new GelfAppender();
	    ga.setGraylog2ServerHost(elasticSearchHost);
	    ga.setGraylog2ServerPort(12201);
	    ga.setGraylog2ServerVersion("0.9.6");
	    ga.setChunkThreshold(1000);
	    ga.setUseLoggerName(true);
	    ga.setMessagePattern("%m%rEx");
	    ga.setShortMessagePattern("%.-100(%m%rEx)");
		ga.setIncludeFullMDC(true);
		ga.setContext(lc);
		ga.start();
		appenderAttachable.addAppender(ga);
	}
	
    @Override
    protected Injector getInjector(){
        injector = Guice.createInjector(new ServletModule(){
            @Override
            protected void configureServlets(){
            	filter("/*").through(CorsFilter.class);
            	filter("/*").through(GuiceShiroFilter.class);
            	filter("/envayasms/*").through(DirectoryFilter.class);
            	filter("/.well-known*").through(DirectoryFilter.class);
            	filter("/api/*").through(DirectoryFilter.class);
            	filter("/parser/*").through(ParserAccessFilter.class); //make sure no-one can access those urls
            	filter("/parser/*").through(ParserFilter.class); //read message into dataset
            	filter("/parser/*").through(AbuseFilter.class);    //prohibit overuse
            	filter("/parser/*").through(DirectoryFilter.class); //allow directory access
            	filter("/parser/*").through(InterpreterFilter.class); //do semantic stuff
            	filter("/account*").through(DirectoryFilter.class); //allow directory access
            	filter("/email/*").through(DirectoryFilter.class); //allow directory access
            	filter("/plivo/*").through(DirectoryFilter.class); //allow directory access
            	filter("/data/*").through(DirectoryFilter.class); //allow directory access
            	bindListener(Matchers.any(), new SLF4JTypeListener());
        		bind(MessagingActivitiesImpl.class);
        		bind(EposActivitiesImpl.class);
        		bind(ParserClient.class);
        		bind(QueueClient.class);
        	}
			
			@Provides
			@Singleton
			@SuppressWarnings("unused")
			public CommandParser getMessageProcessor() {
				return new CommandParser(servletContext);
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			MailServiceClient getMailClient(){
				if (null!=smtpHost){
					return new SmtpEmailClient(smtpHost, smtpUser, smtpPassword);
				}else{
					if (awsCredentials==null){
						return new AmazonEmailClient(
								new AmazonSimpleEmailServiceClient());
					}else{
						return new AmazonEmailClient(
								new AmazonSimpleEmailServiceClient(awsCredentials));
					}					
				}
			}


			@Provides
			@Named("nonTx")
			@Singleton
			@SuppressWarnings("unused")
			public WorkflowWorker getDepositWorker(AmazonSimpleWorkflow swfClient) {
				WorkflowWorker workflowWorker = new WorkflowWorker(swfClient,
						domainName, "deposit-workflow-tasklist");
				try {
					workflowWorker
							.addWorkflowImplementationType(NonTxWorkflowImpl.class);
				} catch (InstantiationException | IllegalAccessException e) {
					log.error("deposit worker exception",e);
					e.printStackTrace();
				}
				return workflowWorker;
			}

			@Provides
			@Named("withdrawal")
			@Singleton
			@SuppressWarnings("unused")
			public WorkflowWorker getWithdrawalWorker(AmazonSimpleWorkflow swfClient) {
				WorkflowWorker workflowWorker = new WorkflowWorker(swfClient,
						domainName, "withdrawal-workflow-tasklist");
				try {
					workflowWorker
							.addWorkflowImplementationType(WithdrawalWorkflowImpl.class);
				} catch (InstantiationException | IllegalAccessException e) {
					log.error("withdrawal worker exception",e);
					e.printStackTrace();
				}
				return workflowWorker;
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			public NonTxWorkflowClientExternalFactoryImpl getDWorkflowClientExternal(
					AmazonSimpleWorkflow workflowClient) {
				return new NonTxWorkflowClientExternalFactoryImpl(
						workflowClient, domainName);
			}

			@Provides @Singleton @SuppressWarnings("unused")
			public GoogleAnalytics getGoogleAnalytics(){
				GoogleAnalytics ga = new GoogleAnalytics(MessagingServletConfig.gaTrackingId);
				return ga;
        	}

			@Provides @Singleton @SuppressWarnings("unused")
			public WithdrawalWorkflowClientExternalFactoryImpl getSWorkflowClientExternal(
					AmazonSimpleWorkflow workflowClient) {
				return new WithdrawalWorkflowClientExternalFactoryImpl(
						workflowClient, domainName);
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			AccountPolicy providePolicy(){
				return new AccountPolicy()
					.setEmailMxLookup(true);
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
			public SocketIOServer provideSocket(){
			 	Configuration config = new Configuration();
			    config.setPort(8081);
			    SocketIOServer server = new SocketIOServer(config);
			    return server;
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			FiatPriceProvider provideFiatPrices(Cache cache){
				return new FiatPriceProvider(cache);
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
			
			@Provides @Singleton @SuppressWarnings("unused") @Named("messaging")
			public ActivityWorker getMsgActivityWorker(AmazonSimpleWorkflow swfClient, 
					MessagingActivitiesImpl activitiesImpl) {
				ActivityWorker activityWorker = new ActivityWorker(swfClient, domainName,
						msgActListName);
				try {
					activityWorker.addActivitiesImplementation(activitiesImpl);
				} catch (InstantiationException | IllegalAccessException
						| SecurityException | NoSuchMethodException e) {
					log.error("msg activity exception",e);
					e.printStackTrace();
				}
				return activityWorker;
			}
			
			@Provides @Singleton @SuppressWarnings("unused") @Named("epos")
			public ActivityWorker getEposActivityWorker(AmazonSimpleWorkflow swfClient, 
					EposActivitiesImpl activitiesImpl) {
				ActivityWorker activityWorker = new ActivityWorker(swfClient, domainName,
						eposActListName);
				try {
					activityWorker.addActivitiesImplementation(activitiesImpl);
				} catch (InstantiationException | IllegalAccessException
						| SecurityException | NoSuchMethodException e) {
					log.error("epos activity exception",e);
					e.printStackTrace();
				}
				return activityWorker;
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			public MessageFactory provideMessageFactory() {
				return new MessageFactory(servletContext);
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			public JndiLdapContextFactory provideLdapClientFactory(){
				JndiLdapContextFactory jlc = new JndiLdapContextFactory();
				jlc.setUrl(ldapUrl);
				jlc.setAuthenticationMechanism("simple");
				jlc.setSystemUsername(ldapUser);
				jlc.setSystemPassword(ldapPw);
				return jlc;
			}
			
			@Provides @Singleton @SuppressWarnings("unused")
			public Client provideElasticSearch(){
				Settings settings = ImmutableSettings.settingsBuilder()
						.put("cluster.name", "graylog2").build();
				return new TransportClient(settings)
						.addTransportAddress(new InetSocketTransportAddress(
								MessagingServletConfig.elasticSearchHost, 9300));
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
        	}},new MessagingShiroWebModule(this.servletContext));
        return injector;
    }
	
    @Override
	public void contextDestroyed(ServletContextEvent sce) {
    	if (null!=jPM)
    		jPM.disconnect();
		try {
			msgActivityWorker.shutdownAndAwaitTermination(1, TimeUnit.MINUTES);
            System.out.println("Activity Worker Exited.");
		}catch (InterruptedException e) {
            e.printStackTrace();
        }
		if (null==System.getProperty("environment")||!System.getProperty("environment").equals("test")){
			Client elasticSearch = injector.getInstance(Client.class);
			elasticSearch.close();
		}
		if (null!=slt){
			slt.kill();
		}
		if (null!=server){
			server.stop();
		}
		super.contextDestroyed(sce);
		log.info("ServletContextListener destroyed");
	}

}

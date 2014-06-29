package com._37coins;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import me.moocar.logbackgelf.GelfAppender;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.spy.memcached.MemcachedClient;

import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.restnucleus.PersistenceConfiguration;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.filter.CorsFilter;
import org.restnucleus.filter.DigestFilter;
import org.restnucleus.filter.QueryFilter;
import org.restnucleus.log.SLF4JTypeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachable;

import com._37coins.bizLogic.NonTxWorkflowImpl;
import com._37coins.bizLogic.WithdrawalWorkflowImpl;
import com._37coins.cache.Cache;
import com._37coins.cache.MemCacheWrapper;
import com._37coins.envaya.QueueClient;
import com._37coins.imap.JavaPushMailAccount;
import com._37coins.merchant.MerchantClient;
import com._37coins.parse.AbuseFilter;
import com._37coins.parse.CommandParser;
import com._37coins.parse.InterpreterFilter;
import com._37coins.parse.ParserClient;
import com._37coins.parse.ParserFilter;
import com._37coins.sendMail.AmazonEmailClient;
import com._37coins.sendMail.MailServiceClient;
import com._37coins.sendMail.SmtpEmailClient;
import com._37coins.util.FiatPriceProvider;
import com._37coins.util.ResourceBundleClient;
import com._37coins.util.ResourceBundleFactory;
import com._37coins.web.AccountPolicy;
import com._37coins.workflow.NonTxWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.WithdrawalWorkflowClientExternalFactoryImpl;
import com._37coins.workflow.pojo.DataSet;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.RequestScoped;
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
	public static String cacheHost;
	public static String plivoKey;
	public static String plivoSecret;
	public static String resPath;
	public static String merchantResPath;
	public static String captchaPubKey;
	public static String captchaSecKey;
	public static String elasticSearchHost;
	public static String paymentsPath;
	public static String gaTrackingId;
	public static String digestToken;
	public static String adminCns;
	public static String s3Path;
	public static int unitFactor;
	public static String unitName;
	public static String unitFormat;
	public static String tickerPath;
	public static List<Locale> activeLocales;
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
		cacheHost = System.getProperty("cacheHost");
		plivoKey = System.getProperty("plivoKey");
		plivoSecret = System.getProperty("plivoSecret");
		resPath = System.getProperty("resPath");
		merchantResPath = System.getProperty("merchantResPath");
		captchaPubKey = System.getProperty("captchaPubKey");
		captchaSecKey = System.getProperty("captchaSecKey");
		elasticSearchHost = System.getProperty("elasticSearchHost");
		paymentsPath = System.getProperty("paymentsPath");
		gaTrackingId = System.getProperty("gaTrackingId");
		digestToken = System.getProperty("hmacToken");
		adminCns = System.getProperty("adminCns");
		s3Path = System.getProperty("s3Path");
		tickerPath = System.getProperty("tickerPath");
	    unitFactor = (null!=System.getProperty("unitFactor"))?Integer.parseInt(System.getProperty("unitFactor")):1000;
	    unitName = (null!=System.getProperty("unitName"))?System.getProperty("unitName"):"Bit";
	    unitFormat = (null!=System.getProperty("unitFormat"))?System.getProperty("unitFormat"):"#,##0";
		String locales = System.getProperty("activeLocales");
		List<String> localeList = Arrays.asList(locales.split(","));
		activeLocales = new ArrayList<>();
		for (String localeString : localeList){
		    Locale locale = DataSet.parseLocaleString(localeString);
		    activeLocales.add(locale);
		}
	}
	
	private ServletContext servletContext;
	private ActivityWorker msgActivityWorker;
	private WorkflowWorker depositWorker;
	private WorkflowWorker withdrawalWorker;
	private JavaPushMailAccount jPM;
    
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
            	filter("/*").through(HttpsEnforcerFilter.class);
            	filter("/*").through(GuiceShiroFilter.class);
            	filter("/envayasms/*").through(EnvayaFilter.class);
            	filter("/api/*").through(QueryFilter.class);
            	filter("/parser/*").through(DigestFilter.class); //make sure no-one can access those urls
            	filter("/parser/*").through(ParserFilter.class); //read message into dataset
            	filter("/parser/*").through(AbuseFilter.class);    //prohibit overuse
            	filter("/parser/*").through(InterpreterFilter.class); //do semantic stuff
            	bindListener(Matchers.any(), new SLF4JTypeListener());
        		bind(MessagingActivitiesImpl.class);
        		bind(QueueClient.class);
        	}
			
			@Provides @Singleton @SuppressWarnings("unused")
			public CommandParser getMessageProcessor(ResourceBundleFactory rbf) {
				return new CommandParser(rbf);
			}
			
	        @Provides @Singleton @SuppressWarnings("unused")
            public ParserFilter getParserFilter(FiatPriceProvider fiatPriceProvider) {
                return new ParserFilter(fiatPriceProvider, MessagingServletConfig.unitFactor, MessagingServletConfig.unitName);
            }
			
	        @Provides @Singleton @SuppressWarnings("unused")
            public EnvayaFilter getEnvayaFilter(PersistenceManagerFactory pmf) {
                return new EnvayaFilter(MessagingServletConfig.basePath, pmf);
            }
			
	        @Provides @SuppressWarnings("unused")
            public ParserClient getParserClient(CommandParser commandParser, GoogleAnalytics ga) {
                return new ParserClient(commandParser, ga, MessagingServletConfig.digestToken);
            }
			
            @Provides @Singleton @SuppressWarnings("unused")
            MerchantClient provideProductsClient(){
                return new MerchantClient(MessagingServletConfig.paymentsPath, MessagingServletConfig.digestToken);
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
			
			@Provides @Singleton @SuppressWarnings("unused")
			public MessageFactory getMessageFactory(ResourceBundleFactory rbf){
			    return new MessageFactory(servletContext, rbf,MessagingServletConfig.unitFactor,MessagingServletConfig.unitName, MessagingServletConfig.unitFormat);
			}
			
            @Provides @Singleton @SuppressWarnings("unused")
            CorsFilter provideCorsFilter(){
                return new CorsFilter("*");
            }

			@Provides @Named("nonTx") @Singleton @SuppressWarnings("unused")
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
			
			@Provides @Singleton @SuppressWarnings("unused")
			public DigestFilter getDigestFilter(){
			    return new DigestFilter(MessagingServletConfig.digestToken);
			}

			@Provides @Named("withdrawal") @Singleton @SuppressWarnings("unused")
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
			FiatPriceProvider provideFiatPrices(Cache cache){
				return new FiatPriceProvider(cache, tickerPath);
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
			            
            @Provides @RequestScoped  @SuppressWarnings("unused")
            public GenericRepository providePersistenceManager(PersistenceManagerFactory pmf){
                GenericRepository dao = new GenericRepository(pmf);
                dao.getPersistenceManager();
                return dao;
            }
			
	        @Provides @Singleton @SuppressWarnings("unused")
            public PersistenceManagerFactory providePersistence(){
                PersistenceConfiguration pc = new PersistenceConfiguration();
                pc.createEntityManagerFactory();
                return pc.getPersistenceManagerFactory();
            }
	        
            
            @Provides @Singleton @SuppressWarnings("unused")
            public ResourceBundleClient getResourceBundleClient(){
                ResourceBundleClient client = new ResourceBundleClient(MessagingServletConfig.resPath+"/scripts/nls/");
                return client;
            }
            
            @Provides @Singleton @SuppressWarnings("unused")
            public ResourceBundleFactory getResourceBundle(@Named("local") com._37coins.cache.Cache cache, ResourceBundleClient client){
                return new ResourceBundleFactory(MessagingServletConfig.activeLocales, client, cache);
            }
			
			@Provides @Singleton @SuppressWarnings("unused")
			public Client provideElasticSearch(){
				Settings settings = ImmutableSettings.settingsBuilder()
						.put("cluster.name", "graylog2").build();
				return new TransportClient(settings)
						.addTransportAddress(new InetSocketTransportAddress(
								MessagingServletConfig.elasticSearchHost, 9300));
			}
			
	        @Named("local")
            @Provides @Singleton @SuppressWarnings("unused")
            public Cache provideHourCache(){
                //Create a singleton CacheManager using defaults
                CacheManager manager = CacheManager.create();
                //Create a Cache specifying its configuration.
                net.sf.ehcache.Cache localCache = new net.sf.ehcache.Cache(new CacheConfiguration("hour", 1000)
                    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                    .eternal(false)
                    .timeToLiveSeconds(7200)
                    .timeToIdleSeconds(3600)
                    .diskExpiryThreadIntervalSeconds(0));
                manager.addCache(localCache);
                Cache cache = new EhCacheWrapper(localCache);
                return cache;
            }
        
        	@Provides @Singleton @SuppressWarnings("unused")
        	public Cache provideCache() throws IOException{
        	    MemcachedClient client = new MemcachedClient(new InetSocketAddress(MessagingServletConfig.cacheHost, 11211));
        	    Cache cache = new MemCacheWrapper(client, 3600);
                return cache;
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
		super.contextDestroyed(sce);
		log.info("ServletContextListener destroyed");
	}

}

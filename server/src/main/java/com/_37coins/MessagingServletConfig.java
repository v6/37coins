package com._37coins;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import me.moocar.logbackgelf.GelfAppender;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.restnucleus.PersistenceConfiguration;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;
import org.restnucleus.filter.CorsFilter;
import org.restnucleus.filter.PersistenceFilter;
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
import com._37coins.envaya.ServiceLevelThread;
import com._37coins.imap.JavaPushMailAccount;
import com._37coins.ldap.CryptoUtils;
import com._37coins.ldap.JdoRequestHandler;
import com._37coins.merchant.MerchantClient;
import com._37coins.parse.AbuseFilter;
import com._37coins.parse.CommandParser;
import com._37coins.parse.InterpreterFilter;
import com._37coins.parse.ParserAccessFilter;
import com._37coins.parse.ParserClient;
import com._37coins.parse.ParserFilter;
import com._37coins.persistence.dao.Gateway;
import com._37coins.resources.TicketResource;
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
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
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
import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.call.Call;
import com.plivo.helper.exception.PlivoException;
import com.unboundid.ldap.listener.LDAPListener;
import com.unboundid.ldap.listener.LDAPListenerConfig;

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
		hmacToken = System.getProperty("hmacToken");
	}
	
	private ServletContext servletContext;
	private ActivityWorker msgActivityWorker;
	private WorkflowWorker depositWorker;
	private WorkflowWorker withdrawalWorker;
	private JavaPushMailAccount jPM;
	public SocketIOServer server;
	private ServiceLevelThread slt;
	private LDAPListener listener;
    
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
		if (null==System.getProperty("environment")||!System.getProperty("environment").equals("test")){
			//handle service level thread
			slt = i.getInstance(ServiceLevelThread.class);
			slt.start();
		}
		
		server = i.getInstance(SocketIOServer.class);
		server.addJsonObjectListener(MerchantSession.class, new DataListener<MerchantSession>() {
	        @Override
	        public void onData(SocketIOClient client, MerchantSession data, AckRequest ackRequest) {
	            MemcachedClient cache = i.getInstance(MemcachedClient.class);
	        	if (null==data.getSessionToken()){
        			//add to monitoring list
        			//if exceeded limit, kick
        			client.sendJsonObject(new MerchantSession().setAction("unauthenticated"));
        			client.disconnect();
        			return;
	        	}
        		//verify session
        		Object e = cache.get(TicketResource.TICKET_SCOPE+data.getSessionToken());
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
        			cache.add("merchant"+data.getSessionToken(),3600,ms);
        			//initialize call
        			try{
	    				RestAPI restAPI = new RestAPI(MessagingServletConfig.plivoKey, MessagingServletConfig.plivoSecret, "v1");
	    				
	    				LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
					PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
					String from = PhoneNumberUtil.getInstance().format(phoneUtil.getExampleNumberForType(phoneUtil.getRegionCodeForCountryCode(phoneNumber.getCountryCode()), PhoneNumberType.MOBILE), PhoneNumberFormat.E164);
				    params.put("from", from.substring(0,from.length()-4)+"3737");
	    			    params.put("to", phone);
	    			    params.put("answer_url", MessagingServletConfig.basePath + "/plivo/merchant/req/"+data.getSessionToken()+"/"+ms.getSessionToken()+"/"+Locale.US.toString());
	    			    params.put("hangup_url", MessagingServletConfig.basePath + "/plivo/merchant/hangup/"+data.getSessionToken());
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
		//start ldap
		listener = i.getInstance(LDAPListener.class);
		try {
            listener.startListening();
        } catch (IOException e) {
            e.printStackTrace();
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
            	filter("/envayasms/*").through(PersistenceFilter.class);
            	filter("/.well-known*").through(PersistenceFilter.class);
            	filter("/api/*").through(PersistenceFilter.class);
            	filter("/parser/*").through(ParserAccessFilter.class); //make sure no-one can access those urls
            	filter("/parser/*").through(ParserFilter.class); //read message into dataset
            	filter("/parser/*").through(AbuseFilter.class);    //prohibit overuse
            	filter("/parser/*").through(PersistenceFilter.class); //allow directory access
            	filter("/parser/*").through(InterpreterFilter.class); //do semantic stuff
            	filter("/account*").through(PersistenceFilter.class); //allow directory access
            	filter("/email/*").through(PersistenceFilter.class); //allow directory access
            	filter("/plivo/*").through(PersistenceFilter.class); //allow directory access
            	filter("/data/*").through(PersistenceFilter.class); //allow directory access
            	filter("/merchant/*").through(PersistenceFilter.class);
            	filter("/healthcheck/*").through(PersistenceFilter.class); //allow directory access
            	bindListener(Matchers.any(), new SLF4JTypeListener());
        		bind(MessagingActivitiesImpl.class);
        		bind(ParserClient.class);
        		bind(QueueClient.class);
        	}
			
			@Provides @Singleton @SuppressWarnings("unused")
			public CommandParser getMessageProcessor() {
				return new CommandParser(servletContext);
			}
			
            @Provides @Singleton @SuppressWarnings("unused")
            MerchantClient provideProductsClient(){
                return new MerchantClient(MessagingServletConfig.paymentsPath, MessagingServletConfig.hmacToken);
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
			public LDAPListener getLdapListener(GenericRepository dao){
			    RNQuery q = new RNQuery().addFilter("cn", amqpUser);
			    Gateway g = dao.queryEntity(q, Gateway.class, false);
			    if (null==g){
			        String pw=null;
		            try{
		                pw = CryptoUtils.getSaltedPassword(amqpPassword.getBytes());
		            }catch(NoSuchAlgorithmException ex){
		                throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
		            }
			        g = new Gateway()
    	                .setEmail(senderMail)
    	                .setCn(amqpUser)
    	                .setPassword(pw);
    	            dao.add(g);
			    }
			    LDAPListenerConfig config = new LDAPListenerConfig(2389, new JdoRequestHandler(dao));
			    LDAPListener listener = new LDAPListener(config);
			    return listener;
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
			
			@Provides @Singleton @SuppressWarnings("unused")
			public MessageFactory provideMessageFactory() {
				return new MessageFactory(servletContext);
			}
			
	        @Provides @Singleton @SuppressWarnings("unused")
            PersistenceManagerFactory providePersistence(){
                PersistenceConfiguration pc = new PersistenceConfiguration();
                pc.createEntityManagerFactory();
                return pc.getPersistenceManagerFactory();
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
		if (null!=slt){
			slt.kill();
		}
		if (null!=server){
			server.stop();
		}
		if (null!=listener){
		    listener.shutDown(true);
		}
		super.contextDestroyed(sce);
		log.info("ServletContextListener destroyed");
	}

}

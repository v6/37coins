package com._37coins;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.restnucleus.PersistenceConfiguration;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.ldap.CryptoUtils;
import com._37coins.ldap.JdoRequestHandler;
import com._37coins.persistence.dao.Gateway;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.unboundid.ldap.listener.LDAPListener;
import com.unboundid.ldap.listener.LDAPListenerConfig;

public class LdapServletConfig extends GuiceServletContextListener {
	public static String cacheHost;
    public static String amqpUser;
    public static String amqpPassword;
    public static String senderMail;
    public static String amqpHost;
	public static Logger log = LoggerFactory.getLogger(LdapServletConfig.class);
	public static Injector injector;
	private ServletContext servletContext;
	private LDAPListener listener;
	static {
		cacheHost = System.getProperty("cacheHost");
	    senderMail = System.getProperty("senderMail");
        amqpUser = System.getProperty("amqpUser");
        amqpPassword = System.getProperty("amqpPassword");
        amqpHost = System.getProperty("amqpHost");
	}
    
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
		final Injector i = getInjector();
        //start ldap
        listener = i.getInstance(LDAPListener.class);
        try {
            listener.startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
		log.info("ServletContextListener started");
	}
	
    @Override
    protected Injector getInjector(){
        injector = Guice.createInjector(new ServletModule(){
            @Override
            protected void configureServlets(){
        	}
            
			@Provides @Singleton @SuppressWarnings("unused")
			PersistenceManagerFactory providePersistence(){
				PersistenceConfiguration pc = new PersistenceConfiguration();
				pc.createEntityManagerFactory();
				return pc.getPersistenceManagerFactory();
			}

            @Provides @Singleton @SuppressWarnings("unused")
            public LDAPListener getLdapListener(GenericRepository dao, PersistenceManagerFactory pmf){
                RNQuery q = new RNQuery().addFilter("cn", LdapServletConfig.amqpUser);
                Gateway g = dao.queryEntity(q, Gateway.class, false);
                if (null==g){
                    String pw=null;
                    try{
                        pw = CryptoUtils.getSaltedPassword(LdapServletConfig.amqpPassword.getBytes());
                    }catch(NoSuchAlgorithmException ex){
                        throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
                    }
                    g = new Gateway()
                        .setEmail(LdapServletConfig.senderMail)
                        .setCn(LdapServletConfig.amqpUser)
                        .setPassword(pw);
                    dao.add(g);
                }
                LDAPListenerConfig config = new LDAPListenerConfig(2389, new JdoRequestHandler(pmf));
                LDAPListener listener = new LDAPListener(config);
                return listener;
            }});
        return injector;
    }
	
    @Override
	public void contextDestroyed(ServletContextEvent sce) {
        if (null!=listener){
            listener.shutDown(true);
        }
		super.contextDestroyed(sce);
		log.info("ServletContextListener destroyed");
	}

}

package com._37coins;

import javax.servlet.ServletContext;

import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.session.mgt.SessionManager;

import com.google.inject.Key;
import com.google.inject.binder.AnnotatedBindingBuilder;


public class MessagingShiroWebModule extends ShiroWebModule {

	public MessagingShiroWebModule(ServletContext servletContext) {
		super(servletContext);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void configureShiroWeb() {
		bindRealm().to(AuthorizingRealm.class).asEagerSingleton();
		bind(CredentialsMatcher.class).to(CustomCredentialsMatcher.class);
		bind(CustomCredentialsMatcher.class);
		bind(Authenticator.class).toInstance(new ModularRealmAuthenticator());
		Key<BasicAccessAuthFilter> customFilter = Key.get(BasicAccessAuthFilter.class);
		addFilterChain("/api/**", customFilter);
	}
	
	@Override 
    protected void bindSessionManager(final AnnotatedBindingBuilder<SessionManager> bind) {
            bind.to(WebSessionManager.class); 
    } 
	
}

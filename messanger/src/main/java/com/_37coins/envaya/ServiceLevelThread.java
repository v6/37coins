package com._37coins.envaya;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashSet;
import java.util.Locale.Builder;
import java.util.Set;

import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com._37coins.MessagingServletConfig;
import com._37coins.web.GatewayUser;
import com._37coins.web.Queue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class ServiceLevelThread extends Thread {
	public static Logger log = LoggerFactory
			.getLogger(ServiceLevelThread.class);
	final private InitialLdapContext ctx;
	final private Cache cache;
	boolean isActive = true;

	@Inject
	public ServiceLevelThread(JndiLdapContextFactory jlc, Cache cache)
			throws IllegalStateException, NamingException {
		this.cache = cache;
		AuthenticationToken at = new UsernamePasswordToken(
				MessagingServletConfig.ldapUser, MessagingServletConfig.ldapPw);
		ctx = (InitialLdapContext) jlc.getLdapContext(at.getPrincipal(),
				at.getCredentials());
	}

	@Override
	public void run() {
		while (isActive) {
			Set<GatewayUser> rv = new HashSet<GatewayUser>();
			NamingEnumeration<?> namingEnum = null;
			try {
				ctx.setRequestControls(null);
				SearchControls searchControls = new SearchControls();
				searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				searchControls.setTimeLimit(1000);
				namingEnum = ctx.search("ou=gateways,"
						+ MessagingServletConfig.ldapBaseDn,
						"(objectClass=person)", searchControls);
				while (namingEnum.hasMore()) {
					Attributes atts = ((SearchResult) namingEnum.next())
							.getAttributes();
					String mobile = (atts.get("mobile") != null) ? (String) atts
							.get("mobile").get() : null;
					String cn = (String) atts.get("cn").get();
					BigDecimal fee = (atts.get("description") != null) ? new BigDecimal(
							(String) atts.get("description").get()) : null;
					if (null != mobile && null != fee) {
						PhoneNumberUtil phoneUtil = PhoneNumberUtil
								.getInstance();
						PhoneNumber pn = phoneUtil.parse(mobile, "ZZ");
						String cc = phoneUtil.getRegionCodeForCountryCode(pn
								.getCountryCode());
						GatewayUser gu = new GatewayUser()
								.setMobile(
										PhoneNumberUtil.getInstance().format(
												pn, PhoneNumberFormat.E164))
								.setFee(fee)
								.setLocale(new Builder().setRegion(cc).build())
								.setId(cn);
						rv.add(gu);
					}
				}
			} catch (Exception ex) {
				log.error("ldap connection failed", ex);
				continue;
			} finally {
				if (null != namingEnum)
					try {
						namingEnum.close();
					} catch (NamingException e1) {
					}
			}

			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(
					MessagingServletConfig.amqpHost, 15672),
					new UsernamePasswordCredentials(
							MessagingServletConfig.amqpUser,
							MessagingServletConfig.amqpPassword));
			HttpClient client = HttpClientBuilder.create()
					.setDefaultCredentialsProvider(credsProvider).build();
			Set<GatewayUser> active = new HashSet<GatewayUser>();
			for (GatewayUser gu : rv) {
				try {
					HttpGet someHttpGet = new HttpGet("http://"
							+ MessagingServletConfig.amqpHost
							+ ":15672/api/queues/%2f/" + gu.getId());
					URI uri = new URIBuilder(someHttpGet.getURI()).build();
					HttpRequestBase request = new HttpGet(uri);
					HttpResponse response = client.execute(request);
					if (new ObjectMapper().readValue(
							response.getEntity().getContent(), Queue.class)
							.getConsumers() > 0) {
						MDC.put("hostName", gu.getId());
						MDC.put("mobile", gu.getMobile());
						MDC.put("event", "check");
						MDC.put("Online", "true");
						log.debug("{} online", gu.getId());
						MDC.clear();
						active.add(gu);
					} else {
						MDC.put("hostName", gu.getId());
						MDC.put("mobile", gu.getMobile());
						MDC.put("event", "check");
						MDC.put("Online", "false");
						log.debug("{} offline", gu.getId());
						MDC.clear();
					}
				} catch (Exception ex) {
					log.error("AMQP connection failed", ex);
				}
			}
			cache.put(new Element("gateways", active));

			try {
				Thread.sleep(59000L);
			} catch (InterruptedException e) {
				log.error("gateway statistics stopping");
				isActive = false;
			}
		}
	}

	public void kill() {
		isActive = false;
		this.interrupt();
	}

}

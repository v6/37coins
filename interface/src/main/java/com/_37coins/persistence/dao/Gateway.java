package com._37coins.persistence.dao;

import java.util.Locale;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

import org.restnucleus.dao.Model;

@PersistenceCapable
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
public class Gateway extends Model {
	private static final long serialVersionUID = -1031604697212697657L;
    public static String getHostName(String email){
        String hostName = email.substring(email.indexOf("@") + 1, email.length());
        return hostName;
    }
	
	@Persistent
	@Index
	@NotNull
	private String cn;

	@Persistent
	@NotNull
	@Unique
	@Index
	private String email;
	
    @Persistent
    @NotNull
    private String hostName;
	
	@Persistent
	@Index
	private Integer countryCode;
	
	@Persistent
	private String password;
	
	@Persistent
	@Index
	@Unique
	private String mobile;
	
	@Persistent
	private String apiToken;
	
	@Persistent
	private String apiSecret;
	
	@Persistent
	private Locale locale;
	
	@Persistent
	private GatewaySettings settings;

	public String getMobile() {
        return mobile;
    }

    public Gateway setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    public Gateway setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public String getEmail() {
		return email;
	}

	public String getHostName() {
        return hostName;
    }

    public Gateway setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public String getCn() {
        return cn;
    }

    public Gateway setCn(String cn) {
        this.cn = cn;
        return this;
    }

    public Gateway setEmail(String email) {
		this.email = email;
		this.hostName = getHostName(email);
		return this;
	}

	public String getApiToken() {
        return apiToken;
    }

    public Gateway setApiToken(String apiToken) {
        this.apiToken = apiToken;
        return this;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public Gateway setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
        return this;
    }
	
	public Integer getCountryCode() {
		return countryCode;
	}

	public Gateway setCountryCode(Integer countryCode) {
		this.countryCode = countryCode;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public Gateway setPassword(String password) {
		this.password = password;
		return this;
	}

    public GatewaySettings getSettings() {
        return settings;
    }

    public Gateway setSettings(GatewaySettings settings) {
        this.settings = settings;
        return this;
    }

    @Override
	public void update(Model newInstance) {
		Gateway n = (Gateway) newInstance;
		if (null != n.getApiSecret())this.setApiSecret(n.getApiSecret());
		if (null != n.getApiToken())this.setApiToken(n.getApiToken());
		if (null != n.getCountryCode())this.setCountryCode(n.getCountryCode());
		if (null != n.getCn())this.setCn(n.getCn());
		if (null != n.getEmail())this.setEmail(n.getEmail());
		if (null != n.getLocale())this.setLocale(n.getLocale());
		if (null != n.getMobile())this.setMobile(n.getMobile());
		if (null != n.getSettings())this.setSettings(n.getSettings());
	}

}

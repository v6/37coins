package com._37coins.persistence.dao;

import java.util.Locale;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;

import org.restnucleus.dao.Model;

@PersistenceCapable
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
public class Account extends Model {
	private static final long serialVersionUID = -792538125194459327L;
	public static final int PIN_MAX_WRONG = 3;
	
	@Persistent
	private Locale locale;
	
	@Persistent
	private String password;
	
	@Persistent
	@Index
	@Unique
	private String mobile;
	
	@Persistent
	@Index
	private int countryCode;
	
	@Persistent
	@Index
	@Unique
	private String displayName;
	
	@Persistent
	private Gateway owner;
	
	@Persistent
	@Index
	private String apiToken;
	
	@Persistent
	private String apiSecret;
	
	@Persistent
	private Integer pinWrongCount = 0;

	public Locale getLocale() {
        return locale;
    }

    public Account setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public Gateway getOwner() {
        return owner;
    }

    public String getApiToken() {
        return apiToken;
    }

    public Account setApiToken(String apiToken) {
        this.apiToken = apiToken;
        return this;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public Account setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
        return this;
    }

    public Account setOwner(Gateway owner) {
        this.owner = owner;
        return this;
    }

    public String getPassword() {
		return password;
	}

	public Account setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getDisplayName() {
        return displayName;
    }

    public Account setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public Account setCountryCode(int countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    public Integer getPinWrongCount() {
		return pinWrongCount;
	}

    public String getMobile() {
        return mobile;
    }

    public Account setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public Account setPinWrongCount(Integer pinWrongCount) {
		this.pinWrongCount = pinWrongCount;
		return this;
	}

	public void update(Model newInstance) {
	}

}

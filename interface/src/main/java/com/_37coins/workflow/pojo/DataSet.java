package com._37coins.workflow.pojo;

import java.math.BigDecimal;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateModel;

@JsonInclude(Include.NON_NULL)
public class DataSet {
    public static final String SERVICE = "www.37coins.com";
	
	public enum Action {
		//REQUESTS
		DEPOSIT_REQ("DepositReq"), // request a bitcoin address to receive a payment
		WITHDRAWAL_REQ("WithdrawalReq"), // send a payment
		CHARGE("Charge"), // request a payment
		PRODUCT("Product"), // like charge, but 24h cache
		PAY("Pay"), // do a payment
		RESTORE("Restore"), //from one account to the other
		CLAIM("Claim"), //swipe private key
		VOICE("Voice"), //set up voice second factor
		WITHDRAWAL_CONF("WithdrawalConf"), // confirm a payment
		BALANCE("Balance"), // request the balance
		GW_BALANCE("GwBal"), // request the balance
		GW_DEPOSIT_REQ("GwDepositReq"), //get addresses for bitfinger
		HELP("Help"), 
		TRANSACTION("Transactions"),
		PRICE("Price"),
		BUY("Buy"),
		SELL("Sell"),
		//RESPONSES
		SIGNUP("Signup"), // create a new account
		RESET("Reset"), // gateway reset password
		REGISTER("Register"), //gateway signup
		DEPOSIT_CONF("DepositConf"),
		DEPOSIT_NOT("DepositNot"),
		FORMAT_ERROR("FormatError"),
		UNKNOWN_COMMAND("UnknownCommand"),
		ACCOUNT_BLOCKED("AccountBlocked"),
		ACCOUNT_DELETE("AccountDeleted"),
		INSUFISSIENT_FUNDS("InsufficientFunds"),
		BELOW_FEE("BelowFee"),
		HELP_SEND("HelpSend"),
		TIMEOUT("Timeout"),
		OVERUSE("Overuse"),
		TX_FAILED("TransactionFailed"),
		UNAVAILABLE("Unavailable"),
		GW_ALERT("GatewayAlert"),
		DST_ERROR("DestinationUnreachable"),
		TX_CANCELED("TransactionCanceled");

		private String text;

		Action(String text) {
			this.text = text;
		}

		@JsonValue
		final String value() {
			return this.text;
		}

		public String getText() {
			return this.text;
		}

		@JsonCreator
		public static Action fromString(String text) {
			if (text != null) {
				for (Action b : Action.values()) {
					if (text.equalsIgnoreCase(b.text)) {
						return b;
					}
				}
			}
			return null;
		}
	}
	
	public DataSet(){
		setService(SERVICE);
	}
	
	private Action action;
	
	private Locale locale;
	
	private MessageAddress to;
	
	private Object payload;
	
	private Object fiatPriceProvider;
	
	private String service;
	
	private String gwCn;
	
	private BigDecimal gwFee;
	
	private String cn;
	
	private TemplateModel resBundle;
	
	private String unitName;
	
	private int unitFactor;
	
	private String unitFormat;
	
	//########## UTILS

	@Override
	public boolean equals(Object obj) {
		if (obj==null)
			return false;
		JsonNode a = new ObjectMapper().valueToTree(this);
		JsonNode b = new ObjectMapper().valueToTree(obj);
		return a.equals(b);
	};	
	
	@Override
	public String toString(){
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}


	//########## GETTERS && SETTERS
	
	public Action getAction() {
		return action;
	}

	public DataSet setAction(Action action) {
		this.action = action;
		return this;
	}
	
	@JsonIgnore
	public String getLocaleString() {
		if (null!=locale)
			return locale.toString().replace("_", "-");
		return null;
	}

	public Locale getLocale() {
		return locale;
	}

	public DataSet setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}
	
	@JsonIgnore
	public DataSet setLocaleString(String locale){
		Locale rv = parseLocaleString(locale);
		if (null==rv)
			return this;
		this.locale = rv;
		return this;
	}
	
	public static Locale parseLocaleString(String locale){
		Locale rv = null;
		if (null!=locale){
			String str = locale.split(",")[0];
			String[] arr = str.trim().replace("-", "_").split(";");
			
			String[] l = arr[0].split("_");
			switch (l.length) {
			case 2:
				rv = new Locale(l[0], l[1]);
				break;
			case 3:
				rv = new Locale(l[0], l[1], l[2]);
				break;
			default:
				rv = new Locale(l[0]);
				break;
			}
		}
		return rv;
	}

	public MessageAddress getTo() {
		return to;
	}

	public DataSet setTo(MessageAddress to) {
		this.to = to;
		return this;
	}

	public Object getPayload() {
		return payload;
	}

	public DataSet setPayload(Object payload) {
		this.payload = payload;
		return this;
	}

	public String getService() {
		return service;
	}

	public DataSet setService(String service) {
		this.service = service;
		return this;
	}

	public String getCn() {
		return cn;
	}

	public DataSet setCn(String cn) {
		this.cn = cn;
		return this;
	}
	
	@JsonIgnore
	public String getGwCn() {
		return gwCn;
	}

	public DataSet setGwCn(String gwCn) {
		this.gwCn = gwCn;
		return this;
	}

	@JsonIgnore
	public BigDecimal getGwFee() {
		return gwFee;
	}

	public DataSet setGwFee(BigDecimal gwFee) {
		this.gwFee = gwFee;
		return this;
	}
	
    @JsonIgnore
    public TemplateModel getResBundle() {
        return resBundle;
    }

    public DataSet setResBundle(TemplateModel resBundle) {
        this.resBundle = resBundle;
        return this;
    }

	@JsonIgnore
	public Object getFiatPriceProvider() {
		return fiatPriceProvider;
	}

	public DataSet setFiatPriceProvider(Object fiatPriceProvider) {
		this.fiatPriceProvider = fiatPriceProvider;
		return this;
	}

	@JsonIgnore
    public String getUnitName() {
        return unitName;
    }

    public DataSet setUnitName(String unitName) {
        this.unitName = unitName;
        return this;
    }

    @JsonIgnore
    public int getUnitFactor() {
        return unitFactor;
    }

    public DataSet setUnitFactor(int unitFactor) {
        this.unitFactor = unitFactor;
        return this;
    }

    public String getUnitFormat() {
        return unitFormat;
    }

    public DataSet setUnitFormat(String unitFormat) {
        this.unitFormat = unitFormat;
        return this;
    }

}

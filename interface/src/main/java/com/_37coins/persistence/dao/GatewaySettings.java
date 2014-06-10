package com._37coins.persistence.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.restnucleus.dao.Model;

@PersistenceCapable
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
public class GatewaySettings extends Model {
    private static final long serialVersionUID = 1842211452135602231L;
    
    @Persistent
    private Double fee;
    
    @Persistent
    private String welcomeMsg;
    
    @Persistent
    private String companyName;
    
    @Persistent
    private String signupCallback;

    public GatewaySettings setFee(Double fee) {
        this.fee = fee;
        return this;
    }

    public BigDecimal getFee() {
        if (null!=fee){
            return new BigDecimal(fee).setScale(8,RoundingMode.HALF_UP);
        }else{
            return null;
        }
    }

    public GatewaySettings setFee(BigDecimal fee) {
        if (null!=fee)
            this.fee = fee.doubleValue();
        return this;
    }

    public String getWelcomeMsg() {
        return welcomeMsg;
    }

    public GatewaySettings setWelcomeMsg(String welcomeMsg) {
        this.welcomeMsg = welcomeMsg;
        return this;
    }

    public String getCompanyName() {
        return companyName;
    }

    public GatewaySettings setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public String getSignupCallback() {
        return signupCallback;
    }

    public GatewaySettings setSignupCallback(String signupCallback) {
        this.signupCallback = signupCallback;
        return this;
    }

    @Override
    public void update(Model newInstance) {
        GatewaySettings n = (GatewaySettings) newInstance;
        if (null != n.getFee())this.setFee(n.getFee());
        if (null != n.getSignupCallback())this.setSignupCallback(n.getSignupCallback());
        if (null != n.getWelcomeMsg())this.setWelcomeMsg(n.getWelcomeMsg());
        if (null != n.getCompanyName())this.setCompanyName(n.getCompanyName());
    }
}

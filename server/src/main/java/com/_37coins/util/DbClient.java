package com._37coins.util;

import java.util.List;
import java.util.Properties;

import javax.jdo.Constants;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.restnucleus.PersistenceConfiguration;
import org.restnucleus.dao.GenericRepository;
import org.restnucleus.dao.RNQuery;

import com._37coins.persistence.dao.Gateway;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class DbClient {

    /**
     * @param args
     * @throws NumberParseException 
     * 
     */
    public static void main(String[] args) throws NumberParseException {
        // TODO Auto-generated method stub
        PersistenceConfiguration pc = new PersistenceConfiguration();
        Properties p = new Properties();
        p.setProperty(Constants.PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS,
                "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
        p.setProperty("datanucleus.autoCreateSchema", "true");
        p.setProperty("datanucleus.validateTables", "true");
        p.setProperty("datanucleus.validateConstraints", "true");

            p.setProperty(Constants.PROPERTY_CONNECTION_DRIVER_NAME,
                    "com.mysql.jdbc.Driver");
            p.setProperty(Constants.PROPERTY_CONNECTION_URL,
                    "jdbc:mysql://" + "server"
                            + ":" + "3306" + "/"
                            + "ebdb"
                            +"?useUnicode=true&characterEncoding=UTF-8");
            p.setProperty(Constants.PROPERTY_CONNECTION_USER_NAME,"username");
            p.setProperty(Constants.PROPERTY_CONNECTION_PASSWORD,"password");
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(p);
        GenericRepository dao = new GenericRepository(pmf);
        
        List<Gateway> gateways = dao.queryList(new RNQuery().setRange(0L, 300L), Gateway.class);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        for (Gateway g: gateways){
            System.out.print(g.getMobile() +" : ");
            if (g.getMobile()==null){
                System.out.println("null: "+g.getCn());
            }else{
                PhoneNumber pn = phoneUtil.parse(g.getMobile(), "ZZ");
                System.out.println(pn.getCountryCode() + " : "+g.getCountryCode());
                //g.setCountryCode(pn.getCountryCode());
            }
        }
        
    }

}

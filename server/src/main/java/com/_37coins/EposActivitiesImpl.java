package com._37coins;

import java.math.BigDecimal;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.activities.EposActivities;
import com._37coins.web.MerchantSession;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProviderImpl;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.inject.Inject;

public class EposActivitiesImpl implements EposActivities {
	public static Logger log = LoggerFactory.getLogger(EposActivitiesImpl.class);
	ActivityExecutionContextProvider contextProvider = new ActivityExecutionContextProviderImpl();
	
	@Inject
	Cache cache;
	
	@Inject
	SocketIOServer server;
	
    @Override
    public Boolean displayCharge(String cn, String btcAddress){
    	String room = cn+"/"+cn;
    	if (server.getRoomOperations(room).getClients().size()>0){
	    	server.getRoomOperations(room).sendJsonObject(new MerchantSession().setAction("address").setAddress(btcAddress));
			Element el = cache.get("merchantState"+cn);
			MerchantSession state = (MerchantSession)el.getObjectValue();
			state.setAddress(btcAddress);
			cache.put(new Element("merchantState"+cn,state));
			return true;
    	}else{
    		return false;
    	}
    }
    
    @Override
    public Boolean transactionReceived(String cn, BigDecimal amount, String btcAddress, String cid, int status){
    	String room = cn+"/"+cn;
    	if (server.getRoomOperations(room).getClients().size()>0){
    		server.getRoomOperations(room).sendJsonObject(
    				new MerchantSession()
    					.setAction("payed")
    					.setAmount(amount)
    					.setAddress(btcAddress)
    					.setCid(cid));
    		//check if it's the one we are waiting for
    		Element el = cache.get("merchantState"+cn);
			MerchantSession state = (MerchantSession)el.getObjectValue();
			//amount and either cid or address are equal  
			if (amount.compareTo(state.getAmount())==0 && (
					(null!=btcAddress && null != state.getAddress() && btcAddress.equals(state.getAddress()))
					||(null!=cid && null != state.getCid() && cid.equals(state.getCid())))){
	    		cache.remove("merchantState"+cn);
	    		return true;				
			}else{
				return false;
			}
    	}else{
    		return false;
    	}
    }

}

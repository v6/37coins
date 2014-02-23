package com._37coins;

import java.math.BigDecimal;

import net.sf.ehcache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.activities.EposActivities;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProviderImpl;
import com.google.inject.Inject;

public class EposActivitiesImpl implements EposActivities {
	public static Logger log = LoggerFactory.getLogger(EposActivitiesImpl.class);
	ActivityExecutionContextProvider contextProvider = new ActivityExecutionContextProviderImpl();
		
	@Inject
	AmazonSimpleWorkflow swfService;
	
	@Inject
	Cache cache;
	
    @Override
    public void displayCharge(BigDecimal amount, String btcAddress, String cid){
    	
    }
    
    @Override
    public void transactionReceived(BigDecimal amount, String btcAddress, String cid, int status){
    	
    }

}

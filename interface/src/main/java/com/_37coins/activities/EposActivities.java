package com._37coins.activities;

import java.math.BigDecimal;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

@Activities
public interface EposActivities {
	
    @Activity(name = "DisplayCharge", version = "0.2")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    Boolean displayCharge(String cn, String btcAddress);
    
    @Activity(name = "TransactionReceived", version = "0.2")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    Boolean transactionReceived(String cn, BigDecimal amount, String btcAddress, String cid, int status);
        
}

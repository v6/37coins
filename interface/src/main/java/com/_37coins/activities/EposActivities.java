package com._37coins.activities;

import java.math.BigDecimal;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

@Activities
public interface EposActivities {
	
    @Activity(name = "DisplayCharge", version = "0.1")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    void displayCharge(BigDecimal amount, String btcAddress, String cid);
    
    @Activity(name = "TransactionReceived", version = "0.1")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    void transactionReceived(BigDecimal amount, String btcAddress, String cid, int status);
        
}

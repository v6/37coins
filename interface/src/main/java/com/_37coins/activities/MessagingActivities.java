package com._37coins.activities;

import java.math.BigDecimal;

import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

@Activities
public interface MessagingActivities {
	
    @Activity(name = "SendMessage", version = "0.4")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 180)
    void sendMessage(DataSet rsp);
    
    @Activity(name = "PutCache", version = "0.2")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    void putCache(DataSet rsp);
    
    @Activity(name = "PutAddressCache", version = "0.2")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    void putAddressCache(DataSet rsp);
    
    @Activity(name = "SendConfirmation", version = "0.8")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 3600)	
	Action sendConfirmation(DataSet rsp, String workflowId);

    @Activity(name = "ReadMessageAddress", version = "0.3")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    DataSet readMessageAddress(DataSet data);
    
    @Activity(name = "ReadAccountFee", version = "0.3")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    BigDecimal readAccountFee(String mobile);

    @Activity(name = "ReadRate", version = "0.1")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    BigDecimal readRate(String curCode, BigDecimal amountBtc);
    
    @Activity(name = "PhoneConfirmation", version = "0.3")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 3600)
    Action phoneConfirmation(DataSet rsp, String workflowId);
    
    @Activity(name = "GetAuthLimit", version = "0.1")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    BigDecimal getLimit(String gateway, String mobile);
    
}

package com._37coins.activities;

import java.util.Locale;

import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.EmailFactor;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

@Activities
public interface MessagingActivities {
	
    @Activity(name = "SendMessage", version = "0.3")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 180)
    void sendMessage(DataSet rsp);
    
    @Activity(name = "PutCache", version = "0.1")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    void putCache(DataSet rsp);
    
    @Activity(name = "PutAddressCache", version = "0.1")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    void putAddressCache(DataSet rsp);
    
    @Activity(name = "SendConfirmation", version = "0.7")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 3600)	
	Action sendConfirmation(DataSet rsp, String workflowId);

    @Activity(name = "ReadMessageAddress", version = "0.2")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    DataSet readMessageAddress(DataSet data);
    
    @Activity(name = "PhoneConfirmation", version = "0.2")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 3600)
    Action phoneConfirmation(DataSet rsp, String workflowId);
    
    @Activity(name = "OtpConfirmation", version = "0.1")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    Action otpConfirmation(String cn, String otp, Locale locale);
    
    @Activity(name = "EmailVerification", version = "0.2")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 3600)
    String emailVerification(EmailFactor emailFactor, Locale locale);
    
    @Activity(name = "EmailConfirmation", version = "0.2")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    void emailConfirmation(String emailServiceToken, Locale locale);
    
    @Activity(name = "EmailOtpCreation", version = "0.3")
    @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 30, defaultTaskStartToCloseTimeoutSeconds = 10)
    void emailOtpCreation(String cn, String email, Locale locale);
    
}

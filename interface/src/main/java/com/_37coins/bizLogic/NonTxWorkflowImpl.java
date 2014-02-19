package com._37coins.bizLogic;

import java.math.BigDecimal;
import java.util.List;

import javax.mail.internet.InternetAddress;

import com._37coins.activities.BitcoindActivitiesClient;
import com._37coins.activities.BitcoindActivitiesClientImpl;
import com._37coins.activities.MessagingActivitiesClient;
import com._37coins.activities.MessagingActivitiesClientImpl;
import com._37coins.bcJsonRpc.pojo.Transaction;
import com._37coins.workflow.NonTxWorkflow;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.EmailFactor;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.MessageAddress;
import com._37coins.workflow.pojo.MessageAddress.MsgType;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Withdrawal;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;

public class NonTxWorkflowImpl implements NonTxWorkflow {

    BitcoindActivitiesClient bcdClient = new BitcoindActivitiesClientImpl();
    MessagingActivitiesClient msgClient = new MessagingActivitiesClientImpl();

	@Override
	public void executeCommand(final DataSet data) {
		if (data.getAction()==Action.DEPOSIT_REQ){
			Promise<String> bcAddress = bcdClient.getNewAddress(data.getCn());
			respondDepositReq(bcAddress, data);
		}else if (data.getAction()==Action.SIGNUP){
			Promise<Void> done = msgClient.sendMessage(data);
			createAddress(done, data);
		}else if (data.getAction()==Action.GW_DEPOSIT_REQ){
			Promise<String> bcAddress = bcdClient.getNewAddress(data.getCn());
			respondDataReq(bcAddress, data);
		}else if (data.getAction()==Action.BALANCE){
			Promise<BigDecimal> balance = bcdClient.getAccountBalance(data.getCn());
			respondBalance(balance, data);
		}else if (data.getAction()==Action.GW_BALANCE){
			Promise<BigDecimal> balance = bcdClient.getAccountBalance(data.getCn());
			cacheBalance(balance, data);
		}else if (data.getAction()==Action.TRANSACTION){
			Promise<List<Transaction>> transactions = bcdClient.getAccountTransactions(data.getCn());
			respondTransactions(transactions, data);
		}else if (data.getAction() == Action.DEPOSIT_CONF){
			Promise<BigDecimal> balance = bcdClient.getAccountBalance(data.getCn());
			respondDepositConf(balance, data);
		}else if (data.getAction() == Action.EMAIL){
				msgClient.emailOtpCreation(data.getCn(), (InternetAddress)data.getPayload());
		}else if (data.getAction() == Action.EMAIL_VER){
			//send message
			EmailFactor ef = (EmailFactor)data.getPayload();
			DataSet emailDs = new DataSet()
				.setAction(Action.EMAIL_SMS_VER)
				.setTo(data.getTo())
				.setCn(data.getCn())
				.setPayload(ef.getSmsToken());
			Promise<Void> doneSms = msgClient.sendMessage(emailDs);
			//send email
			
			data.setAction(Action.EMAIL_VER)
				.setTo(new MessageAddress()
					.setAddressType(MsgType.EMAIL)
					.setAddress(ef.getEmail()))
				.setPayload(ef.getEmailToken());
			Promise<String> doneEmail = msgClient.emailVerification(
					new EmailFactor()
						.setEmail(ef.getEmail())
						.setEmailToken(ef.getEmailToken())
						.setCn(data.getCn()));
			//start manual completion
			waitEmailFactorConfirm(doneSms, doneEmail, data.getCn(), ef.getEmail());
		}else if (data.getAction() == Action.EMAIL){
			//send new otp
			msgClient.sendMessage(data);
		}else{
			throw new RuntimeException("unknown action");
		}
    }
	
	@Asynchronous
	public void createAddress(Promise<Void> done,DataSet data){
		Promise<String> bcAddress = bcdClient.getNewAddress(data.getCn());
		respondDataReq(bcAddress, data);
	}
	
	@Asynchronous
	public void waitEmailFactorConfirm(Promise<Void> doneSms, Promise<String> doneEmail, String cn, InternetAddress email){
		String emailServiceToken = doneEmail.get();
		msgClient.emailConfirmation(emailServiceToken);
		msgClient.emailOtpCreation(cn, email);
	}
	
	@Asynchronous
	public void respondDepositReq(Promise<String> bcAddress,DataSet data){
		data.setPayload(new PaymentAddress()
			.setAddress(bcAddress.get())
			.setAddressType(PaymentType.BTC));
		msgClient.sendMessage(data);
	}
	
	@Asynchronous
	public void respondDataReq(Promise<String> bcAddress,DataSet data){
		data.setPayload(new PaymentAddress()
			.setAddress(bcAddress.get())
			.setAddressType(PaymentType.BTC));
		msgClient.putAddressCache(data);
	}
	
	@Asynchronous
	public void respondBalance(Promise<BigDecimal> balance,DataSet data){
		data.setPayload(new Withdrawal()
				.setBalance(balance.get()));
		msgClient.sendMessage(data);
	}
	
	@Asynchronous
	public void cacheBalance(Promise<BigDecimal> balance,DataSet data){
		data.setPayload(new Withdrawal()
				.setBalance(balance.get()));
		msgClient.putCache(data);
	}
	
	@Asynchronous
	public void respondTransactions(Promise<List<Transaction>> transactions,DataSet data){
		data.setPayload(transactions.get());
		msgClient.sendMessage(data);
	}
	
	@Asynchronous
	public void respondDepositConf(Promise<BigDecimal> balance,DataSet data){
		Withdrawal dep = (Withdrawal)data.getPayload();
		dep.setBalance(balance.get());
		Promise<DataSet> addr = msgClient.readMessageAddress(data);
		msgClient.sendMessage(addr);
	}

}

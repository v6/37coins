package com._37coins.bizLogic;

import java.math.BigDecimal;
import java.util.List;

import com._37coins.activities.BitcoindActivitiesClient;
import com._37coins.activities.BitcoindActivitiesClientImpl;
import com._37coins.activities.EposActivitiesClient;
import com._37coins.activities.EposActivitiesClientImpl;
import com._37coins.activities.MessagingActivitiesClient;
import com._37coins.activities.MessagingActivitiesClientImpl;
import com._37coins.bcJsonRpc.pojo.Transaction;
import com._37coins.workflow.NonTxWorkflow;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Withdrawal;
import com.amazonaws.services.simpleworkflow.flow.DecisionContext;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.WorkflowClock;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.annotations.NoWait;
import com.amazonaws.services.simpleworkflow.flow.core.OrPromise;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.Settable;
import com.amazonaws.services.simpleworkflow.flow.core.TryCatch;

public class NonTxWorkflowImpl implements NonTxWorkflow {
	DecisionContextProvider contextProvider = new DecisionContextProviderImpl();
	DecisionContextProvider provider = new DecisionContextProviderImpl();
	DecisionContext context = provider.getDecisionContext();
	private WorkflowClock clock = context.getWorkflowClock();
	private final int confirmationPeriod = 3500;
    BitcoindActivitiesClient bcdClient = new BitcoindActivitiesClientImpl();
    MessagingActivitiesClient msgClient = new MessagingActivitiesClientImpl();
    EposActivitiesClient eposClient = new EposActivitiesClientImpl();

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
		}else if (data.getAction()==Action.VOICE){
			final Settable<DataSet> confirm = new Settable<>();
			final Promise<Action> response = msgClient.phoneConfirmation(data, contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId());
			final OrPromise confirmOrTimer = new OrPromise(startDaemonTimer(confirmationPeriod), response);
		   	new TryCatch() {
				@Override
	            protected void doTry() throws Throwable {
					setConfirm(confirm, confirmOrTimer, response, data);
				}
	            @Override
	            protected void doCatch(Throwable e) throws Throwable {
	            	data.setAction(Action.TIMEOUT);
	    			msgClient.sendMessage(data);
	            	cancel(e);
				}
			};
			handleVoice(confirm);
		}else if (data.getAction() == Action.DEPOSIT_CONF){
			Promise<BigDecimal> balance = bcdClient.getAccountBalance(data.getCn());
			respondDepositConf(balance, data);
		}else{
			throw new RuntimeException("unknown action");
		}
    }
	
	@Asynchronous
    public void handleVoice(Promise<DataSet> rsp){
		msgClient.sendMessage(rsp.get());
	}
	
	@Asynchronous
	public void setConfirm(@NoWait Settable<DataSet> account, OrPromise trigger, Promise<Action> isConfirmed, DataSet data) throws Throwable{
		if (isConfirmed.isReady()){
			if (null!=isConfirmed.get() && isConfirmed.get()!=Action.VOICE){
				data.setAction(isConfirmed.get());
			}
			account.set(data);
		}else{
			throw new Throwable("user did not confirm transaction.");
		}
		
	}
	
	@Asynchronous
	public void createAddress(Promise<Void> done,DataSet data){
		Promise<String> bcAddress = bcdClient.getNewAddress(data.getCn());
		respondDataReq(bcAddress, data);
	}
	
	@Asynchronous(daemon = true)
    private Promise<Void> startDaemonTimer(int seconds) {
        Promise<Void> timer = clock.createTimer(seconds);
        return timer;
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
		eposClient.displayCharge(data.getCn(), bcAddress.get());
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
		String address = null;
		if (null!=dep.getPayDest() && dep.getPayDest().getAddressType()==PaymentType.BTC){
			address = dep.getPayDest().getAddress();
		}
		Promise<Boolean> delivered = eposClient.transactionReceived(data.getCn(),dep.getAmount(), address, dep.getComment(), 1);
		respondDepositConfMessage(data,delivered);
	}
	
	@Asynchronous
	public void respondDepositConfMessage(DataSet data,Promise<Boolean> delivered){
		if (!delivered.get()){
			Promise<DataSet> addr = msgClient.readMessageAddress(data);
			msgClient.sendMessage(addr);			
		}
	}

}

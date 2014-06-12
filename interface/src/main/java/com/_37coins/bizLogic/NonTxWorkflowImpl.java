package com._37coins.bizLogic;

import java.math.BigDecimal;
import java.util.List;

import com._37coins.activities.BitcoindActivitiesClient;
import com._37coins.activities.BitcoindActivitiesClientImpl;
import com._37coins.activities.MessagingActivitiesClient;
import com._37coins.activities.MessagingActivitiesClientImpl;
import com._37coins.bcJsonRpc.pojo.Transaction;
import com._37coins.workflow.NonTxWorkflow;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
import com._37coins.workflow.pojo.PaymentAddress;
import com._37coins.workflow.pojo.PaymentAddress.PaymentType;
import com._37coins.workflow.pojo.Signup;
import com._37coins.workflow.pojo.Withdrawal;
import com.amazonaws.services.simpleworkflow.flow.ActivityTaskTimedOutException;
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

	@Override
	public void executeCommand(final DataSet data) {
		new TryCatch() {
			@Override
            protected void doTry() throws Throwable {
				if (data.getAction()==Action.DEPOSIT_REQ){
					Promise<String> bcAddress = bcdClient.getNewAddress(data.getTo().getAddress().replace("+", ""));
					respondDepositReq(bcAddress, data);
				}else if (data.getAction()==Action.SIGNUP){
					Promise<Void> done = msgClient.sendMessage(data);
					createAddress(done, data);
				}else if (data.getAction()==Action.GW_DEPOSIT_REQ){
					Promise<String> bcAddress = bcdClient.getNewAddress(data.getCn());
					respondDataReq(bcAddress, data);
				}else if (data.getAction()==Action.BALANCE){
					Promise<BigDecimal> balance = bcdClient.getAccountBalance(data.getTo().getAddress().replace("+", ""));
					Promise<BigDecimal> fee = msgClient.readAccountFee(data.getTo().getAddress());
					respondBalance(balance, fee, data);
				}else if (data.getAction()==Action.GW_BALANCE){
					Promise<BigDecimal> balance = bcdClient.getAccountBalance(data.getCn());
					cacheBalance(balance, data);
				}else if (data.getAction()==Action.TRANSACTION){
					Promise<List<Transaction>> transactions = bcdClient.getAccountTransactions(data.getTo().getAddress().replace("+", ""));
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
				}else if (data.getAction() == Action.DEPOSIT_CONF 
						|| data.getAction() == Action.DEPOSIT_NOT){
					Withdrawal w = (Withdrawal)data.getPayload();
					Promise<BigDecimal> balance = null;
					if (w.getBalance()==null){
						balance = bcdClient.getAccountBalance(data.getCn());
					}
					Promise<BigDecimal> fee = null;
					if (w.getFee()==null){
						fee = msgClient.readAccountFee(data.getCn());
					}
					if (data.getTo()==null){
						respondDepositConfMessage(balance, fee, msgClient.readMessageAddress(data));
					}else{
						Settable<DataSet> ds = new Settable<>();
						respondDepositConfMessage(balance, fee, ds);
						ds.set(data);
					}
				}else{
					throw new RuntimeException("unknown action");
				}
			 }
            @Override
            protected void doCatch(Throwable e) throws Throwable {
                if (e instanceof ActivityTaskTimedOutException){
                    ActivityTaskTimedOutException ae = (ActivityTaskTimedOutException)e;
                    String s = ae.getActivityType().getName().toLowerCase();
                    if (s.contains("account")||s.contains("address")){
                        data.setAction(Action.UNAVAILABLE);
                        msgClient.sendMessage(data);
                        e.printStackTrace();
                        cancel(e);
                    }else{
                        throw e;
                    }
                }else{
                    throw e;
                }
            }
		};
    }
	
	@Asynchronous
    public void handleVoice(Promise<DataSet> rsp){
		msgClient.sendMessage(rsp.get());
	}
	
	@Asynchronous
	public void setConfirm(@NoWait Settable<DataSet> account, OrPromise trigger, Promise<Action> isConfirmed, DataSet data) throws Throwable{
		if (isConfirmed.isReady()){
			if (null!=isConfirmed.get() && isConfirmed.get()!=Action.WITHDRAWAL_REQ){
				data.setAction(isConfirmed.get());
			}
			account.set(data);
		}else{
			throw new Throwable("user did not confirm transaction.");
		}
		
	}
	
	@Asynchronous
	public void createAddress(Promise<Void> done,DataSet data){
		Promise<String> bcAddress = bcdClient.getNewAddress(data.getTo().getAddress().replace("+", ""));
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
	    if (data.getPayload() instanceof Signup){
	        Signup s = (Signup)data.getPayload();
	        s.setDestination(new PaymentAddress().setAddress(bcAddress.get()).setAddressType(PaymentType.BTC));
	    }
	    if (data.getPayload()==null){
	        data.setPayload(new PaymentAddress()
	            .setAddress(bcAddress.get())
	            .setAddressType(PaymentType.BTC));
	    }
		msgClient.putAddressCache(data);
	}
	
	@Asynchronous
	public void respondBalance(Promise<BigDecimal> balance, Promise<BigDecimal> fee, DataSet data){
		data.setPayload(new Withdrawal()
				.setBalance(balance.get().subtract(fee.get())));
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
	public void respondDepositConfMessage(Promise<BigDecimal> balance,Promise<BigDecimal> fee,Promise<DataSet> addr){
		DataSet ds =  addr.get();
		Withdrawal dep = (Withdrawal)ds.getPayload();
		BigDecimal bal = (dep.getBalance()!=null)?dep.getBalance():balance.get();
		BigDecimal f = (dep.getFee()!=null)?dep.getFee():fee.get();
		dep.setBalance(bal.subtract(f));
		msgClient.sendMessage(ds);
	}

}

package com._37coins.bizLogic;

import java.math.BigDecimal;
import java.util.concurrent.CancellationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com._37coins.activities.BitcoindActivitiesClient;
import com._37coins.activities.BitcoindActivitiesClientImpl;
import com._37coins.activities.MessagingActivitiesClient;
import com._37coins.activities.MessagingActivitiesClientImpl;
import com._37coins.workflow.NonTxWorkflowClientFactory;
import com._37coins.workflow.NonTxWorkflowClientFactoryImpl;
import com._37coins.workflow.WithdrawalWorkflow;
import com._37coins.workflow.pojo.DataSet;
import com._37coins.workflow.pojo.DataSet.Action;
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

public class WithdrawalWorkflowImpl implements WithdrawalWorkflow {
	public static Logger log = LoggerFactory.getLogger(WithdrawalWorkflowImpl.class);
	public static BigDecimal bcFee = new BigDecimal("0.0001");
	DecisionContextProvider contextProvider = new DecisionContextProviderImpl();
    BitcoindActivitiesClient bcdClient = new BitcoindActivitiesClientImpl();
    MessagingActivitiesClient msgClient = new MessagingActivitiesClientImpl();
    NonTxWorkflowClientFactory factory = new NonTxWorkflowClientFactoryImpl();
    private final int confirmationPeriod = 3500;
    DecisionContextProvider provider = new DecisionContextProviderImpl();
    DecisionContext context = provider.getDecisionContext();
    private WorkflowClock clock = context.getWorkflowClock();
    
    @Override
    public void executeCommand(final DataSet data) {
    	Promise<BigDecimal> balance = bcdClient.getAccountBalance(data.getTo().getAddress().replace("+", ""));
    	Promise<BigDecimal> volume24h = bcdClient.getTransactionVolume(data.getTo().getAddress().replace("+", ""),24);
    	Promise<BigDecimal> limit = msgClient.getLimit(data.getTo().getGateway(),data.getTo().getAddress());
    	handleAccount(balance, volume24h, limit, data);
    }
    
    @Asynchronous
    public void handleAccount(Promise<BigDecimal> balance, Promise<BigDecimal> volume24h, Promise<BigDecimal> limit, final DataSet data){
		final Settable<DataSet> confirm = new Settable<>();
    	Withdrawal w = (Withdrawal)data.getPayload();
    	BigDecimal amount = w.getAmount();
    	BigDecimal fee = w.getFee().setScale(8);
        if (null!=w.getPayDest() && w.getPayDest().getAddressType()==PaymentType.ACCOUNT){
            bcFee=BigDecimal.ZERO;
        }
    	//for withdrawing everything
    	if (amount.equals(BigDecimal.ZERO)){
    		amount = balance.get().subtract(fee).subtract(bcFee);
    		w.setAmount(amount);
    	}
    	if (balance.get().compareTo(amount.add(fee).add(bcFee).setScale(8))<0){
    		//balance not sufficient
    		data.setPayload(new Withdrawal()
				.setAmount(amount.setScale(8))
				.setBalance(balance.get().subtract(fee)))
    			.setAction(Action.INSUFISSIENT_FUNDS);
    		Promise<Void> fail = msgClient.sendMessage(data);
    		fail(fail, "insufficient funds");
    		return;
    	}else if (fee.compareTo(amount)>=0){
    		//avoid dust
    		data.setPayload(new Withdrawal()
					.setAmount(amount)
					.setBalance(fee))
    		    .setAction(Action.BELOW_FEE);
    		Promise<Void> fail = msgClient.sendMessage(data);
    		fail(fail, "send amount below fee");
    		return;
    	}else{
    		//balance sufficient, now secure transaction authenticity 
			final Promise<Action> response;
			if (volume24h.get().add(amount).compareTo(limit.get()) > 0){
				response = msgClient.phoneConfirmation(data,contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId());
				w.setConfKey(WithdrawalWorkflow.VOICE_VER_TOKEN);
			}else {
				response = msgClient.sendConfirmation(data, contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId());
			}
			
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
    	}
		handleTransaction(confirm);
    }
    
    @Asynchronous
    public void handleTransaction(final Promise<DataSet> rsp){
    	if (rsp.get().getAction()!=Action.WITHDRAWAL_REQ
    			&& rsp.get().getAction()!=Action.WITHDRAWAL_CONF){
    		Promise<Void> fail = msgClient.sendMessage(rsp.get());
    		fail(fail, "transaction failed");
    		return;
    	}
		new TryCatch() {
			@Override
            protected void doTry() throws Throwable {
	    		//define transaction
				Withdrawal w = (Withdrawal)rsp.get().getPayload();
				String toId = null;
				String toAddress = null;
				if (w.getPayDest().getAddressType()==PaymentType.ACCOUNT){
					toId = w.getPayDest().getAddress();
					toAddress = (w.getMsgDest()!=null)?rsp.get().getTo().getAddress()+"::"+w.getMsgDest().getAddress():null;
				}
				if (w.getPayDest().getAddressType()==PaymentType.BTC){
					toAddress = w.getPayDest().getAddress();
				}
				String workflowId = contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId();
	    		Promise<String> tx = bcdClient.sendTransaction(
	    				w.getAmount(), 
	    				w.getFee(), 
	    				rsp.get().getTo().getAddress().replace("+", ""), 
	    				toId, 
	    				toAddress,
	    				workflowId,
	    				w.getComment());
				MDC.put("hostName", rsp.get().getTo().getGateway());
				MDC.put("event", Action.WITHDRAWAL_REQ.toString());
				MDC.put("amount", w.getAmount().toString());
				MDC.put("fee", w.getFee().toString());
				MDC.put("comment", w.getComment());
				MDC.put("sender", rsp.get().getCn());
				MDC.put("recepient", toId);
				MDC.put("workflowId", workflowId);
				log.info("withdrawal request processed");
				MDC.clear();
	    		sendFee(tx, rsp.get());
            }
            @Override
            protected void doCatch(Throwable e) throws Throwable {
            	rsp.get().setAction(Action.TX_FAILED);
    			msgClient.sendMessage(rsp);
    			e.printStackTrace();
            	cancel(e);
            }
		};
    }
    
    @Asynchronous
    public void sendFee(Promise<String> txId, DataSet data) throws Throwable{
    	Withdrawal w = (Withdrawal)data.getPayload();
    	if (w.getFee().compareTo(BigDecimal.ZERO)>0){
	    	bcdClient.sendTransaction(
	    			w.getFee(), 
	    			BigDecimal.ZERO, 
	    			data.getTo().getAddress().replace("+", ""), 
	    			w.getFeeAccount(), 
	    			null,
	    			contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId(),
	    			"");
    	}
    	Promise<BigDecimal> balance = bcdClient.getAccountBalance(data.getTo().getAddress().replace("+", ""));
    	w.setTxId(txId.get());
    	afterSend(balance, data);
    }
    
    @Asynchronous
    public void afterSend(Promise<BigDecimal> newBal, DataSet data) throws Throwable{
    	Withdrawal w = (Withdrawal)data.getPayload();
    	w.setBalance(newBal.get().subtract(w.getFee()));
		data.setAction(Action.WITHDRAWAL_CONF);
    	msgClient.sendMessage(data);
    	if (w.getPayDest().getAddressType()==PaymentType.ACCOUNT){
    		//start child workflow to tell receiver about his luck
    		DataSet rsp2 = new DataSet()
    			.setAction(Action.DEPOSIT_CONF)
    			.setCn(w.getPayDest().getAddress())
    			.setPayload(new Withdrawal()
    				.setMsgDest(data.getTo())
    				.setComment(w.getComment())
    				.setAmount(w.getAmount())
    				.setTxId(context.getWorkflowContext().getWorkflowExecution().getRunId()));
    		Promise<Void> rv = factory.getClient().executeCommand(rsp2);
    		awaitWorkflow(rv, rsp2);
    	}
    }
    
    @Asynchronous
	public void awaitWorkflow(Promise<Void> isConfirmed, DataSet data){
		//do nothing
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
	public void fail(Promise<Void> data, String message){
		throw new CancellationException(message);
	}

	
	@Asynchronous(daemon = true)
    private Promise<Void> startDaemonTimer(int seconds) {
        Promise<Void> timer = clock.createTimer(seconds);
        return timer;
    }

}

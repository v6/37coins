package com._37coins.parse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._37coins.util.ResourceBundle;
import com._37coins.util.ResourceBundleFactory;
import com._37coins.workflow.pojo.DataSet.Action;

public class CommandParser {
	public static Logger log = LoggerFactory.getLogger(CommandParser.class);
	public static final List<Action> reqCmdList = Arrays.asList(
			Action.BALANCE,
			Action.DEPOSIT_REQ,
			Action.HELP,
			Action.PRICE,
			Action.BUY,
			Action.SELL,
			Action.SIGNUP,
			Action.TRANSACTION,
			Action.PAY,
			Action.VOICE,
			Action.WITHDRAWAL_REQ, 
			Action.CHARGE,
			Action.PRODUCT);
	
	private final Map<String, Pair<Action,Locale>> wordMap = new HashMap<>();

	public CommandParser(ResourceBundleFactory rbf) {
		// create a map of command words to actions and locales
		for (Locale locale : rbf.getActiveLocales()) {
		    
			try {
				ResourceBundle rb = rbf.getBundle(locale, "labels");
				for (Action a : reqCmdList) {
				    List<String> variations = rb.getStringList(a.getText()+"Cmd"); 
					for (String cmd : variations) {
						wordMap.put(cmd, Pair.of(a, locale));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private Action replaceCommand(String cmd) {
		for (String pos : wordMap.keySet()) {
			if (cmd.equalsIgnoreCase(pos)) {
				return wordMap.get(pos).getLeft();
			}
		}
		if (cmd.equals("*"))
			return Action.WITHDRAWAL_REQ;
		if (cmd.toLowerCase().matches("[adgjmptw]\\d\\d\\d\\d"))
			return Action.WITHDRAWAL_CONF;
		return null;
	}
	
	private Locale readLanguage(String cmd) {
		for (String pos : wordMap.keySet()) {
			if (cmd.equalsIgnoreCase(pos)) {
				return wordMap.get(pos).getRight();
			}
		}
		return null;
	}
	
	public Action processCommand(String msg) {
		msg = msg.trim().replaceAll(" +", " ");
		String[] ca = msg.split(" ");
		return replaceCommand(ca[0]);
	}
	
	public Locale guessLocale(String msg) {
		msg = msg.trim().replaceAll(" +", " ");
		String[] ca = msg.split(" ");
		return readLanguage(ca[0]);
	}

	
}

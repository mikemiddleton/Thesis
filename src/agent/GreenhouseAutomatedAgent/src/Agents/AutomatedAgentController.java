package Agents;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.nau.rtisnl.SegaWebException;
import helpers.Person;
import helpers.SegaDB;
import helpers.SmartList;
import helpers.SubscriptionInfo;
import interfaces.ErrorHandler;
import interfaces.MessageProcessor;
import utilities.NetManagementCommand;
import utilities.SegaLogger;
import utilities.Validator;

/*
 * class definition for automated user controllers which create, authenticate,
 * and return automated user objects 
 */
public class AutomatedAgentController{
	public static AutomatedAgent createAutomatedAgent(String username, String password, ArrayList<SubscriptionInfo> subscriptions, String agentName, MessageProcessor msgProc, ErrorHandler errHandler){
		// logs in user, validates permissions to subscriptions, creates person and automated user
		Person p = doLogin(username, password);
		
		// TO DO - Validate subscription
		
		AutomatedAgent agent = new AutomatedAgent(p, subscriptions, agentName, msgProc, errHandler);
		
		return agent;
	}

	public static void runCommand(NetManagementCommand nmc, AutomatedAgent agent){
		// validates user's permission to run command and performs safety validation
		// Validator.validate(agent.person, nmc, nmc.getWisard());
		
		nmc.runCommand();
	}
	
	private static Person doLogin(String username, String password){
		SegaDB sdb = new SegaDB();
		Person p = null;
		
		try{
	        sdb.init();
	        ResultSet rs = sdb.getLoginPerson(username, password);
	        if(rs.next()){
	          p = new Person(rs);    
	        }
	        sdb.disconnect();
	   }
	   catch(Exception e){       
	       e.printStackTrace(); 
	   }
	   return p;
	}
}
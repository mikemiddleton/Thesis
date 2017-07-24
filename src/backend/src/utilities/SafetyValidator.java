package utilities;

import helpers.SmartList;
import helpers.ValidationIssue;
import helpers.Wisard;

/*
 * this class will validate whether or not a command can safely execute on a node
 */
public class SafetyValidator {
	
	public static SmartList<ValidationIssue> validate(NetManagementCommand cmd,  Wisard wisard){
		SmartList<ValidationIssue> issues = cmd.validate(wisard);
		return issues;
	}
}

/* TO DO:
	- perhaps expand this in the future to do:
		command history
		throttling
		machine learning
		etc.abstract
	anything that is higher level than command level validation
	would go here
*/
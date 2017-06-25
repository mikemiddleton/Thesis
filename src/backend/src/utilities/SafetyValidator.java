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

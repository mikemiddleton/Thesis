package utilities;

import utilities.UserAccessValidator;
import helpers.Person;
import helpers.SmartList;
import helpers.ValidationIssue;
import helpers.Wisard;
import utilities.SafetyValidator;

public class Validator {
	
	public SmartList<ValidationIssue> validateAll(Person p, NetManagementCommand cmd, SmartList<Wisard> slw){
		SmartList<ValidationIssue> issues = new SmartList<ValidationIssue>();
		for(Wisard w: slw){
			issues.addAll(validate(p, cmd, w));
		}
		return issues;
	}
	
	public SmartList<ValidationIssue> validate(Person p, NetManagementCommand cmd, Wisard w){
		SmartList<ValidationIssue> issues = new SmartList<ValidationIssue>();
		
		// validate user access
		boolean permission = UserAccessValidator.validate(p, w);
		if(!permission){
			issues.add(new ValidationIssue("The user does not have permission to access this WiSARD", ValidationIssue.Type.ERROR));
		}
		else{
			issues.addAll(SafetyValidator.validate(cmd, w));
		}
		
		return issues;
	}
}

package utilities;

import utilities.UserAccessValidator;
import helpers.SmartList;
import helpers.ValidationIssue;
import utilities.SafetyValidator;

public class Validator {
	
	public SmartList<ValidationIssue> validate(Person p, ){
		SmartList<ValidationIssue> issues = UserAccessValidator.validate(p, resource)
	}
}

package helpers;

public class ValidationIssue{

	public enum Type{
		WARNING,
		ERROR
	}
	
	private String msg;
	private Type type;
	
	public ValidationIssue(String msg, Type type){
		this.msg = msg;
		this.type = type;
	}
	
	public String getMessage() {
		return msg;
	}
	
	public Type getType(){
		return type;
	}
}

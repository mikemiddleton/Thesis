package interfaces;

/*
 * Classes who implement this interface must define a method to handle
 * exceptions
 */
public interface ErrorHandler{
	public void handleError(Exception e);
}
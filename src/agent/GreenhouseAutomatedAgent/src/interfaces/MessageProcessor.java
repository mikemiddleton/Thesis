package interfaces;

import utilities.Message;

/*
 * Classes who implement this interface must define a method to handle 
 * messages
 */
public interface MessageProcessor{
	public void processMessage(Message msg);
}

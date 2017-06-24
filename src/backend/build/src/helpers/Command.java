package helpers;

/**
 * A class to hold information about a message.
 * 
 * @author jes244 - modified for commands by jdk85
 *
 */
public class Command implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1409503657656669825L;
	// Variables
	public byte[] command;
	public long expiration_date;
	public int priority;
	public Command next;
	
	/**
	 * Default constructor.
	 * 
	 * @param b The data of the Message.
	 * @param t The timestamp.
	 */
	public Command(byte[] command, long expiration_date, int priority)
	{
		// Initialization
		this.command = command;
		this.expiration_date = expiration_date;
		this.priority = priority;
	}
}

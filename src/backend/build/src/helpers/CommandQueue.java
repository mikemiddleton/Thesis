package helpers;

/**
 * 
 * 
 * @author jes244 - modified for commands by jdk85
 *
 */
public class CommandQueue 
{
	// Variables
	private int count = 0;
	private Command first;
	private Command last;

	/**
	 * Default constructor.
	 */
	public CommandQueue()
	{
		// Initialization
		count = 0;
		first = null;
		last = null;
	}
	
	/**
	 * Insert a command into the queue.
	 * 
	 * @param data The data of the command.
	 * @param timestamp The timestamp.
	 */
	public void insert(byte[] data, long expiration_date, int priority)
	{
		// Create new Message
		Command msg = new Command(data, expiration_date, priority);
		// Update the pointer
		msg.next = null;
		// Is the queue empty
		if (is_empty()) {
			// Update first and last Messages
			first = msg;
			last = msg;
		} else {
			// Update the last Message's pointer
			last.next = msg;
		}
		// Update last Message
		last = msg;
		// Increase count
		count++;
	}
	
	/**
	 * Removes the first Command from the queue.
	 */
	public void remove()
	{
		// Is the queue empty
		if (!is_empty()) {
			// Update the first Message
			first = first.next;
			// Decrease the count
			count--;
		}
	}
	
	public Command get_command(){
		if(!is_empty()){
			return first;
		}
		else return null;
	}
	/**
	 * Gets the data from the first
	 * 
	 * @return The data of the command
	 */
	public byte[] get_command_data()
	{
		// Is the queue empty
		if (!is_empty()) {
			// Return the first Message's data
			return first.command;
		} else {
			// No data
			return null;
		}
	}

	/**
	 * Gets the expiration date from the first
	 * 
	 * @return The expiration.
	 */
	public long get_expiration_date()
	{
		// Is the queue empty
		if (!is_empty()) {
			// Return the first commands expiration date
			return first.expiration_date;
		} else {
			// No expiration date
			return -1;
		}
	}
	
	/**
	 * Gets the priority level of the command
	 */
	public int get_priority(){
		if(!is_empty()){
			return first.priority;
		}
		else{
			return -1;
		}
	}
	/**
	 * Return the number of messages in the queue.
	 * 
	 * @return The number of messages.
	 */
	public int get_count()
	{
		return count;
	}
	
	/**
	 * Checks to see if the queue is emtpy.
	 * 
	 * @return True, if empty. False, if not empty.
	 */
	public boolean is_empty()
	{
		// Is the queue empty
		if (count == 0) {
			// Return true if it is
			return true;
		} else {
			// Return false if it is not
			return false;
		}
	}
}

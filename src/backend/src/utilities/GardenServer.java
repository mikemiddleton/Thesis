package utilities;

import helpers.Command;
import edu.nau.rtisnl.SegaWebException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

import com.rbnb.api.AddressException;
import com.rbnb.api.Controller;
import com.rbnb.api.Rmap;
import com.rbnb.api.SerializeException;
import com.rbnb.api.Server;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

public class GardenServer implements java.io.Serializable {

	/** Serial version ID */
	private static final long				serialVersionUID	= -5895738462855953658L;
	/**
	 * The domain name of the garden server (or the IP address although it is
	 * ill advised)
	 */
	private String							gardenServerIP		= "";
	private String							gardenServerName	= "";
	/** Root directory - doesn't include tailing forward-slash */
	private String							rootDirectory		= "";
	/** Log file */
	private SegaLogger						log;
	private Source							gardenSrc;

	/**
	 * Final reconnect logic value - try to reconnect every 30 seconds for 1
	 * hour
	 */
	private final int						max_reconnect_attempts	= 120, reconnect_sleep_time = 30;
	/**
	 * Total number of reconnect attempts by EITHER WiSARD or CR1000 client
	 * threads
	 */
	private int								reconnect_attempts		= 0;

	/**
	 * If the garden server is operational and able to be connected to, set this
	 * value to true
	 */
	private boolean							is_running				= false;
	
	private boolean							start_on_init			= false;

	/** Number of frames to be stored in cache */
	private int								cache_size				= 16;
	/** Number of frames to be stored in the archive */
	private int								archive_size			= 2048;

	private ChannelMap						cMap					= new ChannelMap();
	private int								cMapIndex;
	/** CommandQueue for incoming commands from the data center */
	private volatile Queue<Command>			cmd_queue;

	/** Handles connection to RBNB. Flushes commands */
	private Thread							rbnb_thread;
	/** Used to deal with uncaught exceptions in the rbnb client thread */
	private Thread.UncaughtExceptionHandler	thread_exception_handler;

	/**
	 * @return the gardenServerIP
	 */
	public String getGardenServerIP() {
		return gardenServerIP;
	}

	/**
	 * @param gardenServerIP
	 *            the gardenServerIP to set
	 */
	public void setGardenServerIP(String gardenServerIP) {
		this.gardenServerIP = gardenServerIP;
	}

	/**
	 * @return the gardenServerName
	 */
	public String getGardenServerName() {
		return gardenServerName;
	}

	/**
	 * @param gardenServerName
	 *            the gardenServerName to set
	 */
	public void setGardenServerName(String gardenServerName) {
		this.gardenServerName = gardenServerName;
	}

	/**
	 * @return the rootDirectory
	 */
	public String getRootDirectory() {
		return rootDirectory;
	}

	/**
	 * @param rootDirectory
	 *            the rootDirectory to set
	 */
	public void setRootDirectory(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	
	/**
	 * @return the is_running
	 */
	public boolean getIs_running() {
		return is_running;
	}

	/**
	 * @param is_running the is_running to set
	 */
	public void setIs_running(boolean is_running) {
		ConnectionHandler connector = new ConnectionHandler(log);	
		try{
			if (connector.connect("/opt/postgres_config/db_connect")) {
				// Get the distinct values from datavalues table
				String statement = " UPDATE garden_servers SET is_running='" + is_running + "' WHERE sitename='" + 
						gardenServerName +"' AND ip_addr='" + gardenServerIP + "';";
				try {
					connector.executeStatement(statement);
				} catch (SegaWebException e) {
					writeToLog("Postgres ERROR:",e);	
				}
				connector.disconnect();
	
			}
		}catch(SQLException e){
			writeToLog("ERROR updating is_running",e);
		} catch (ClassNotFoundException e) {
			writeToLog("ERROR updating is_running",e);
		}
		
		this.is_running = is_running;
	}

	/**
	 * @return the start_on_init
	 */
	public boolean getStart_on_init() {		
		return start_on_init;
	}

	/**
	 * @param start_on_init the start_on_init to set
	 */
	public void setStart_on_init(boolean start_on_init) {
		ConnectionHandler connector = new ConnectionHandler(log);	
		try{
			if (connector.connect("/opt/postgres_config/db_connect")) {
				// Get the distinct values from datavalues table
				String statement = " UPDATE garden_servers SET start_on_init='" + start_on_init + "' WHERE sitename='" + 
						gardenServerName +"' AND ip_addr='" + gardenServerIP + "';";
				try {
					connector.executeStatement(statement);
				} catch (SegaWebException e) {
					writeToLog("Postgres ERROR:",e);
				}				
				connector.disconnect();
	
			}
		}catch(SQLException e){
			writeToLog("ERROR updating is_running",e);
		} catch (ClassNotFoundException e) {
			writeToLog("ERROR updating is_running",e);
		}
		
		this.start_on_init = start_on_init;
	}

	/**
	 * 
	 * @param gardenServerIP
	 * @param gardenServerName
	 * @param rootDirectory
	 */
	public GardenServer(String gardenServerIP, String gardenServerName, String rootDirectory, boolean start_on_init) {
		setGardenServerIP(gardenServerIP);
		setGardenServerName(gardenServerName);
		setRootDirectory(rootDirectory);
		setStart_on_init(start_on_init);
		
		try {
			log = new SegaLogger(rootDirectory + "/GardenServerSources/" + getGardenServerName() + "_Log.txt");
		} catch (IOException e) {
			System.out.println("ERROR: Cannot create logger " + rootDirectory + "/GardenServerSources/" + getGardenServerName()
					+ "_Log.txt");//this goes nowhere
			e.printStackTrace();
		}
		writeToLog("Creating uncaught exception handler...");

		// Create the exception handler object
		thread_exception_handler = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				String msg = e.getLocalizedMessage();
				writeToLog("Thread " + t.getName() + " caught:\t" + e.getLocalizedMessage());
				String error_message;
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				// reconnecting_rbnb is true when one of the threads has caught
				// an RBNB exception
				// and is in the process of trying to reconnect
				if (msg.contains("com.rbnb.sapi.SAPIException")) {
					error_message = "\tERROR SAPIException occured for " + getGardenServerName() + " " + t.getName() + "\r\n\r\n";
					writeToLog(error_message,e);
					if (validateReconnectAttempt(e)) {
						execute();
					}
				} else if (msg.contains("java.lang.InterruptedException: sleep interrupted")) {
					writeToLog("Sleep interrupted for thread " + t.getName() + "\tOK");
				} else {
					// Unknown exception occurred - log it, send an alert, and
					// try to reconnect
					error_message = "\r\nUnknown exception occured for " + getGardenServerName() + " " + t.getName() + "\r\n\r\n";
					writeToLog(error_message,e);

					// Restart the garden server
					execute();
				}
			}

		};
		writeToLog("\tOK");
		
		
		
		
		
		if(start_on_init){
			writeToLog("Start on init set to true -  calling execute()");
			execute();
		}
	}

	/**
	 * Initializes the RBNB thread Checks to see if the thread is already
	 * running first. Adds the exception handler for reconnecting to RBNB Sets
	 * name to "rbnb_thread" and calls the start function on the rbnb_thread
	 * variable
	 * 
	 * @param none
	 * @return none
	 */
	private void init_rbnb_thread() {
		interrupt_thread(rbnb_thread);
		rbnb_thread = new Thread(new Runnable() {
			public void run() {
				RBNB_Client();
			}
		});
		rbnb_thread.setUncaughtExceptionHandler(thread_exception_handler);
		rbnb_thread.setName("rbnb_thread");
		rbnb_thread.start();
	}

	private void RBNB_Client() {
		try {
			cmd_queue = new LinkedList<Command>();
			cMapIndex = cMap.Add("Commands");

			while (!rbnb_thread.isInterrupted()) {
				//TODO: implement watchdog - this can spin forever if a command source goes awry
				while (!cmd_queue.isEmpty()) {
					// TODO: implement priority check
					if (cmd_queue.peek().expiration_date < System.currentTimeMillis()) {
						// Command has expired, remove it
						cmd_queue.remove();
						writeToLog("Command removed - expired!");
					} else {
						cMap.PutDataAsByteArray(cMapIndex, cmd_queue.peek().command);
						if (gardenSrc.Flush(cMap) == 1) {
							cmd_queue.remove();
							writeToLog("Command removed after flush");
						} else {
							writeToLog("Command failed to flush");
						}
					}
				}
				Thread.sleep(1000);
			}
			
		} catch (SAPIException e) {
			writeToLog("RBNB client caught SAPI Exception");
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			writeToLog("RBNB client interrupted during sleep");
			throw new RuntimeException(e);
		} catch (Exception e) {
			writeToLog("RBNB client caught unhandled exception");
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			writeToLog(errors.toString());
			throw new RuntimeException(e);
		}

	}

	/**
	 * 
	 * @param thread
	 */
	private void interrupt_thread(Thread thread) {
		// If the wisard thread is already running, interrupt it
		if (thread != null && (thread.isAlive() || !thread.isInterrupted())) {
			writeToLog("Interrupting " + thread.getName() + "...");
			thread.interrupt();
			try {
				// Give up to 30 seconds for the thread to end after being
				// interrupted
				thread.join(120 * 1000);
			} catch (InterruptedException e) {
				writeToLog("InterruptedException while joining " + thread.getName(),e);
			}
			writeToLog("\tSuccessfully interrupted " + thread.getName());
			thread = null;

		}
	}

	public void execute() {
		reconnect_rbnb();
		try {
			cMapIndex = cMap.Add("Commands");
		} catch (SAPIException e) {
			writeToLog("Error adding Commands channel to cMap",e);
		}
		init_rbnb_thread();

	}

	/**
	 * 
	 */
	private boolean reconnect_rbnb() {
		// Block until sources are connected to RBNB
		try {
			while (!connect_rbnb()) {				
				if (reconnect_attempts++ < max_reconnect_attempts) {
					writeToLog("Reconnecting in " + reconnect_sleep_time + " seconds\tAttempt " + reconnect_attempts + "/"
							+ max_reconnect_attempts);
					Thread.sleep(reconnect_sleep_time * 1000);
				} else {
					writeToLog("Maximum number of reconnect attempts reached (" + reconnect_attempts + ")");
					return false;
				}
			}
		} catch (InterruptedException e) {
			writeToLog("Interrupted while reconnecting...",e);
			return false;
		}

		return true;
	}

	/**
	 * This method makes sure that all necessary RBNB connections are made.
	 * 
	 * @return
	 */
	private boolean connect_rbnb() {
		// Connect sources/sinks
		try {
			disconnect_RBNB();
			writeToLog("Connecting GardenServer source...");
			gardenSrc = new Source(cache_size, "append", archive_size);

			gardenSrc.OpenRBNBConnection(getGardenServerIP(), getGardenServerName() + "_ControlSource");
			writeToLog("\tOK");

			reconnect_attempts = 0;
			
			setIs_running(true);
			
			return true;
		} catch (SAPIException e) {
			writeToLog("Error connecting sources or sinks - make sure RBNB is running on both the garden server and the localhost");
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			writeToLog("\r\n\r\n" + errors.toString());
			if (!validateReconnectAttempt(e)) {
				writeToLog("Reconnect validation for garden server '" + gardenServerName
						+ "' failed, operation will not be resumed.\r\n Exiting...");
				reconnect_attempts = max_reconnect_attempts;
				// NOTE: we can't use System.exit() here since that actually
				// would shut down tomcat
			}
		}
		return false;
	}

	public synchronized void insert_command(byte[] command, long expiration_date, int priority) {
		cmd_queue.add(new Command(command, expiration_date, priority));
	}

	public boolean validateConnection() {
		if (gardenSrc != null) {
			if (gardenSrc.VerifyConnection()) {
				return true;
			}
		}
		return false;
	}
	
	public void forceSourceTermination(){
		//NOTE: this code was adapted from Matt Miller's TerminateSource.java which is
		//available here: https://lists.sdsc.edu/pipermail/rbnb-dev/attachments/20110504/08c30431/TerminateSource.zip
		//from the original post found here: https://lists.sdsc.edu/pipermail/rbnb-dev/2011/000956.html
		
		String sourceName = getGardenServerName() + "_ControlSource";
		writeToLog("WARNING\tIllegalStateException, forcing source termination...");
		try{
			writeToLog("\tConnecting to RBNB server");
			// Make a connection to the RBNB
			Server server = Server.newServerHandle("", getGardenServerIP());
			
			writeToLog("\tConnecting controller");
			//Connect the controller
			Controller controller = server.createController("SourceTermControl");
			//Call start to activate the controller
			controller.start();
			
			writeToLog("\tCreating temp Rmap");
			//Create a temporary Rmap using a wildcard to fetch all of the channels
			//for the specified garden server
			Rmap tempRmap = Rmap.createFromName(sourceName + Rmap.PATHDELIMITER + "...");
			//Identify the leaf nodes
			tempRmap.markLeaf();
			
			writeToLog("\tFetching registration");
			//Fetch and store the registration for the channels found matching the 
			//garden server source name
			Rmap rmap = controller.getRegistered(tempRmap);
			
			if (rmap == null) {
				controller.stop();
				writeToLog("No registered channels found for " + sourceName + "... halting operation");
				reconnect_attempts = max_reconnect_attempts; //This will get caught on the next reconnect attempt and suspend operation
			}
	
			writeToLog("\tFetching name hierarchy");
			// Get rid of all the unnamed stuff in the Rmap hierarchy
			rmap = rmap.toNameHierarchy();
			if (rmap == null) {
				controller.stop();
				writeToLog("No named channels found for " + sourceName + "... halting operation");
				reconnect_attempts = max_reconnect_attempts; //This will get caught on the next reconnect attempt and suspend operation
			}
			
			writeToLog("\tFinding registration descendents");
			//Find the descendants of the garden server source
			Rmap startingRmap = rmap.findDescendant(sourceName,false);
			
			if (startingRmap == null) {
				controller.stop();
				writeToLog("No descendant channels found for " + sourceName + "... halting operation");
				reconnect_attempts = max_reconnect_attempts; //This will get caught on the next reconnect attempt and suspend operation
			}		
			
			try {
				// If the client is a Source, clear the keep cache flag.  This will
				// ensure that the RBO will actually go away.
				if (startingRmap instanceof com.rbnb.api.Source) {
					writeToLog("\tTerminating source - setting keep cache flag to false");
					((com.rbnb.api.Source) startingRmap).setCkeep(false);
				}
				writeToLog("\tSending termination signal to the source " + sourceName);
				//Send the signal to stop the client for the garden server source 
				//by casting the startingRmap to a client object
				controller.stop((com.rbnb.api.Client)startingRmap);
				
				
			} catch (Exception e) {
				controller.stop();
				writeToLog("Exception while checking instanceof " + sourceName + " or while issuing client stop command to the " +
						"server... halting operation");
				reconnect_attempts = max_reconnect_attempts; //This will get caught on the next reconnect attempt and suspend operation
				
			}
			writeToLog("\tStopping controller");
			controller.stop();			
		}catch(AddressException | SerializeException | IOException | InterruptedException e){
			writeToLog("ERROR\tSource termination failed... halting operation",e);
			reconnect_attempts = max_reconnect_attempts; //This will get caught on the next reconnect attempt and suspend operation
			
		}
		writeToLog("\tSource termination completed");
	}

	/**
	 * Synchronized function to flush a channel map to the same gardenSrc
	 * Source. This is used by any experiment that needs to send a command to
	 * this garden server
	 * 
	 * @param cMap
	 * @throws SAPIException
	 */
	// public synchronized void flushChannelized(byte[] command){
	// try{
	// cMap.PutDataAsByteArray(cMapIndex, command);
	// gardenSrc.Flush(cMap);
	//
	// }catch(SAPIException e){
	// writeToLog("Error flushing to " + gardenServerName);
	// writeToLog("\tERROR: \t" + e.getLocalizedMessage());
	// if(!validateReconnectAttempt(e.getLocalizedMessage())){
	// writeToLog("Reconnect validation for " + gardenServerName +
	// " failed, operation will not be resumed.\r\n");
	// }
	// }
	//
	// }

	/**
	 * This method determines whether or not a nested SAPI exception requires a
	 * reconnect to recover
	 * 
	 * @param error
	 * @param attempts
	 * @return true if a reconnect attempt is needed
	 */
	public boolean validateReconnectAttempt(Throwable e) {
		// Get the full error
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		String fullError = errors.toString();

		// Get the main nested exception
		String error = e.getLocalizedMessage();

		boolean reconnect = false;
		if (error.contains("Nesting java.io.InterruptedIOException")) {
			// DO NOT try to reconnect
		} else if (error.contains("This operation requires a connection.")) {
			// We've already disconnected
			// DO NOT try to reconnect
		} else if (error.contains("Nesting java.lang.InterruptedException")) {
			// This can get thrown on a shutdown or restart during a fetch
			// timeout
			// DO NOT try to reconnect
		} else if (error.contains("Nesting java.lang.IllegalStateException")) {
			// This can happen when source channels already exist and the
			// application tries to reconnect
			// Only attempt a reconnect after the old source has been terminated
			if (fullError.contains("Cannot reconnect to existing client handler")) {
				//Force the source to be terminated
				forceSourceTermination();
				reconnect = true;
			}
		} else if (error.contains("Nesting java.net.SocketException")) {
			// This can happen when RBNB goes down while operating
			reconnect = true;
		} else if (error.contains("Nesting java.net.ConnectException")) {
			// This can happen when RBNB is down during initial connection
			reconnect = true;
		} else if (error.contains("Nesting java.net.NoRouteToHostException")) {
			// This can happen when server connection is unavailable - most
			// likely powered off or in the process of rebooting
			reconnect = true;
		} else if (error.contains("Nesting java.io.EOFException")) {
			// This can happen when RBNB is down during initial connection
			reconnect = true;
		}

		return reconnect;
	}

	
	/**
	 * 
	 * @param obj
	 */
	public synchronized void writeToLog(Object obj){
		log.write(obj);
	}
	
	/**
	 * 
	 * @param obj
	 * @param e
	 */
	public synchronized void writeToLog(Object obj,Throwable e){		
		StringWriter errors = new StringWriter();
    	e.printStackTrace(new PrintWriter(errors));		
		log.write(obj);
		log.write("\r\n" + errors.toString());
	}
	
	
	/**
	 * Safely shut down garden_src
	 */
	private void disconnect_RBNB() {
		writeToLog("Disconnecting RBNB...");
		if (gardenSrc != null && gardenSrc.VerifyConnection()) {
			gardenSrc.Detach();
			int counter = 0;
			while(gardenSrc.VerifyConnection() && counter < 300){
				try {
					Thread.sleep(1000);
					counter++;
				} catch (InterruptedException e) {
					writeToLog("Sleep interrupted while disconnecting");
				}//sleep for a second
			}
			if(counter == 300){
				writeToLog("ERROR disconnecting garden server source");
			}
			else{
				writeToLog("\tOK - Source detached");

			}
		}
		else{
			writeToLog("\tOK - Source not attached");
		}

	}

	public void shutdown() {
		setIs_running(false);
		writeToLog(gardenServerName + " shutting down...");
		interrupt_thread(rbnb_thread);
		// TODO: implement save cmd queue
		// writeToLog("Saving command queue...");
		// save_command_queue();

		disconnect_RBNB();
	}
}

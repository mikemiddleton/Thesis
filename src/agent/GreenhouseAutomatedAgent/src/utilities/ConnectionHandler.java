package utilities;

import helpers.KeyValueObject;
import edu.nau.rtisnl.SegaWebException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/*
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
*/

import java.sql.*;

import java.util.ArrayList;
import java.util.Properties;

public class ConnectionHandler {
	/** Connection variable used to handle Postgres JDBC connection */
	private Connection connection = null;
	/** The SegaLogger object from the parent class */
	private SegaLogger log;
	
	public ConnectionHandler(){
		//empty constructor
		try {
			this.log = new SegaLogger("~/log.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ConnectionHandler(SegaLogger log){
		this.log = log;
	}

	/**
	 * Connection method handles database connection
	 * @return True if connection is successful, otherwise return false
	 */
	public boolean connect(String url,String user,String password) throws SQLException,ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		//this.getClass().getClassLoader().loadClass("org.postgresql.Driver");
		
		//Make connection and store in the connection variable
		connection = DriverManager.getConnection(url,user,password);
		return connection != null ? true : false;
	}
	
	/**
	 * Connection method handles database connection
	 * 
	 * @param db_config_location - the absolute path of the config file
	 * @return True if connection is successful, otherwise return false
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public boolean connect(String db_config_location) throws SQLException,ClassNotFoundException {
		Properties properties = new Properties();
		try {
			String db_address = "", db_port = "", db_database = "", db_username = "", db_password = "";
			InputStream input = new FileInputStream(db_config_location);
			properties.load(input);
			
			//These are the properties we need to get from the db_connect file
			String[] property_names = new String[]{
					"address",
					"port",
					"database",
					"username",
					"password"
			};
			
			//Iterate through each known param
			String value = "";
			for(String s : property_names){
				value = properties.getProperty(s);
				if(s.equalsIgnoreCase("address")){
					db_address = value;
				}
				else if(s.equalsIgnoreCase("port")){
					db_port = value;
				}
				else if(s.equalsIgnoreCase("database")){
					db_database = value;
				}
				else if(s.equalsIgnoreCase("username")){
					db_username = value;
				}
				else if(s.equalsIgnoreCase("password")){
					db_password = value;
				}
			}
			
			String db_full_address = "jdbc:postgresql://" + db_address + ":" + db_port + "/" + db_database;
			connect(db_full_address, db_username, db_password);
			if(!isConnected()){
				writeToLog("ERROR: Could not connect to postgres database\r\n\tCheck 'db_connect' parameters and try again");
				return false;
			}
			else return true;
		
		} catch (FileNotFoundException e) {
			writeToLog("ERROR: cannot find 'db_connect' file",e);
			return false;
		} catch (IOException e) {
			writeToLog("ERROR: IOException while reading 'db_connect' file",e);
			return false;
		} catch (ClassNotFoundException e) {
			writeToLog("ERROR: ClassNotFound while reading 'db_connect' file",e);
			return false;
		} catch (SQLException e) {
			writeToLog("ERROR: SQLException while connecting to the Postgres database",e);
			return false;
		}
	}
	/**
	 * Disconnect from open JDBC connection
	 * 
	 * @return connection.close() result
	 * @throws SQLException
	 */
	public boolean disconnect() throws SQLException{
		connection.close();
		return connection.isClosed();
		
	}
	
	
	/**
	 * This function accepts the column names for a key value pair and appends the results as a key value pair 
	 * taken from the column results to the ArrayList<KeyValueObject> that was passed to this function and 
	 * then returns the appended array list
	 * 
	 * @param results The array list to append the results to
	 * @param id_key The column name of the key
	 * @param id_value The column name for the value
	 * @param table_name The table name to fetch from
	 * @return An array list of key value objects with the specified  fetched values appended
	 * @throws SQLException
	 */
	public ArrayList<KeyValueObject> buildKeyValuePair(ArrayList<KeyValueObject> results, String id_key, String id_value, String table_name) throws SQLException{
		Statement stm = connection.createStatement();
		ResultSet resultSet = stm.executeQuery("select " + id_key + "," + id_value + " from " + table_name + ";");
		while(resultSet.next()){
			results.add(new KeyValueObject(resultSet.getObject(id_key).toString(),resultSet.getObject(id_value).toString()));
		}
		return results;
	}

	/**
	 * Executes the statement to the active connection
	 * @param statement
	 * @throws SegaWebException 
	 * @throws SQLException
	 */
	public ResultSet executeStatement(String statement) throws SegaWebException{
		
		//Create statement
		Statement stm;
		try {
			stm = connection.createStatement();
			//Execute the statement
			if(stm.execute(statement)){
				return stm.getResultSet();
			}
			else{
				return null;
			}
		} catch (SQLException e) {
			writeToLog("ERROR: SQLException on executeStatement()",e);
			if(e.getMessage().equals("ERROR: canceling statement due to statement timeout")){
				throw new SegaWebException(SegaWebException.error_type.FETCH_TIMEOUT);
			}
			else{
				throw new SegaWebException(e);
			}
		}
		
	}
	
	public boolean isConnected(){
		//Return true if the connection is validated within 10 seconds
		try {
			return connection.isValid(10);
		} catch (SQLException e) {			
			writeToLog("ERROR: Cannot validate connection",e);
			return false;
		}
	}
	
	/**
	 * Write the toString of the object to the log
	 * If debug is set to on, then this call also prints
	 * to the console
	 * @param obj
	 */
	public synchronized void writeToLog(Object obj){
		log.write(obj.toString());
	}
	
	/**
	 * Write the toString of the object to the log and
	 * write the output of the throwable to the log as well
	 * If debug is turnen on, this method will also print 
	 * the output to the console
	 * @param obj - The toString of this object is written to the log
	 * @param e - The stack trace from this throwable is also written to the log
	 */
	public synchronized void writeToLog(Object obj,Throwable e){		
		StringWriter errors = new StringWriter();
    	e.printStackTrace(new PrintWriter(errors));
		log.write(obj);
		log.write("\r\n" + errors.toString());
	}
	
	/**
	 * Prints to the console
	 * @param s - The string to be displayed
	 */
	public synchronized void print_to_console(String s){
		System.out.println(s);
	}
}

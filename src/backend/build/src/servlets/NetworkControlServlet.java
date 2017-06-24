package servlets;

import helpers.KeyValueObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWritger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipal;

import utilities.ConnectionHandler;
import utilities.ArrayUtilities;
import utilities.CRC;
import utilities.PacketGenerator;
import utilities.SegaLogger;

import edu.nau.rtisnl.SegaWebException;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

/**
* Servlet implementation class NetworkControlServlet
*/
@WebServlet("/NetworkControlServlet")
public class NetworkControlServlet extends HttpServlet{
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 5199097670843989396L;
	/** Log object */
	private static SegaLogger log;
	/**ConnectionHandler object used to connect to Postgres and execute requests */
	private ConnectionHandler connector = null;
	
	/**
	 * @see HttpServlet#HttpServlet() 
	 */
	public NetworkControlServlet(){
		super();
		try{
			log = new SegaLogger("/usr/share/tomcat7/segalogs/NetworkControlServlet.txt");
		} catch(IOException e){
			System.out.println("ERROR: Cannot create logger /usr/share/tomcat7/segalogs/NetworkControlServlet.txt");
			e.printStackTrace();
		}
	}
	
	/** 
	 * This method accepts the column names for a key value pair and appends the results as a key value pair
	 * taken from the column results to the ArrayList<KeyValueObject> that was passed to this function and
	 * then returns the append array list
	 * 
	 * @param results The array list to append the results to
	 * @param id_key The column name of the key
	 * @param id_value The column name for the value
	 * @param table_name The table name to fetch from
	 * @return An array list of key value objects with the specified fetched values appended
	 * @throws SQLException
	 * @throws SegaWebException
	 */
	public ArrayList<KeyValueObject> buildKeyValuePair(ArrayList<KeyValueObject> results, String id_key, String id_value, String table_name) throws SQLException, SegaWebException{
		String statement = "select " + id_key + "," + id_value + " from " + table_name + ";";
		ResultSet resultSet = connector.executeStatement(statement);
		while(resultSet.next()){
			results.add(new KeyValueObject(resultSet.getObject(id_key).toString(), resultSet.getObject(id_value).toString()));
		}
		return results;
	}
	
	/**
	 * Page passes garden server to servlet
	 */
	protected void doPost(){
		// method contents
	}
	
	/**
	 * Servlet responds by querying database and returning a list of wisards
	 */
	protected void doGet(){
		// method contents
	}
	
	
	
}
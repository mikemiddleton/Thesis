package servlets;

import helpers.KeyValueObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import utilities.ConnectionHandler;
import utilities.SegaLogger;
import edu.nau.rtisnl.SegaWebException;


/**
 * Servlet implementation class WisardBuildServlet
 */
@WebServlet("/WisardBuildServlet")
public class WisardBuildServlet extends HttpServlet {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 5199097670843989396L;
	/** Log object */
	private static SegaLogger log;
    /**ConnectionHandler object used to connect to Postgres and execute requests */
	private ConnectionHandler connector = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WisardBuildServlet() {
        super();
        try {
			log = new SegaLogger("/usr/share/tomcat7/segalogs/WisardBuildServlet.txt");
		} catch (IOException e) {
			System.out.println("ERROR: Cannot create logger /usr/share/tomcat7/segalogs/WisardBuildServlet.txt");
			e.printStackTrace();
		}
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
	 * @throws SegaWebException 
	 */
	public ArrayList<KeyValueObject> buildKeyValuePair(ArrayList<KeyValueObject> results, String id_key, String id_value, String table_name) throws SQLException, SegaWebException{
		String statement = "select " + id_key + "," + id_value + " from " + table_name + ";";
		ResultSet resultSet = connector.executeStatement(statement);
		while(resultSet.next()){
			results.add(new KeyValueObject(resultSet.getObject(id_key).toString(),resultSet.getObject(id_value).toString()));
		}
		return results;
	}
	
	/**
	 * Fetches the results for the columns specified from the table parameter
	 * @param columns
	 * @param table_name
	 * @return String array containing results
	 * @throws SegaWebException 
	 */
	public ArrayList<KeyValueObject> getBuildFormInitInfo(ArrayList<KeyValueObject> results,String col,String table_name) throws SQLException, SegaWebException{
		//Create statement
		String statement = "select " + col +" from " + table_name + ";";
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = connector.executeStatement(statement);
		//Iterate over result set and build an array of XYDataPointObjects
		while (resultSet.next()) {
			results.add(new KeyValueObject(col,resultSet.getObject(col).toString()));
		}
		resultSet.close();
		return results;
	}
	
	public ArrayList<KeyValueObject> getWisardsBySite(ArrayList<KeyValueObject> results, String garden_site) throws SQLException, SegaWebException{
		//Create statement
		String statement = "SELECT wisards.wisard_serial_id, wisards.wisardnet_id, hardware_types.option_value AS cp_role FROM wisards INNER JOIN hardware_types ON wisards.wisard_role = hardware_types.id WHERE wisards.sites_siteid=(SELECT siteid FROM sites WHERE sitename='" + garden_site + "');";
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = connector.executeStatement(statement);
		//Iterate over result set and build an array of XYDataPointObjects
		while (resultSet.next()) {
			results.add(new KeyValueObject(resultSet.getObject("wisard_serial_id").toString(), "Wisard " + resultSet.getObject("wisardnet_id").toString() + 
					" (" + resultSet.getObject("cp_role").toString() + " - Serial ID: " + 
					resultSet.getObject("wisard_serial_id").toString() + ")"));
		}
		resultSet.close();
		return results;
	}

	public ArrayList<KeyValueObject> getWisardParams(ArrayList<KeyValueObject> results, String wisard_serial_id) throws SQLException, SegaWebException{
		
		//Create statement
		String statement = "select * from diagnostic where wisard_serial_id='" + wisard_serial_id + "';";
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = connector.executeStatement(statement);
		//Iterate over result set and build an array of XYDataPointObjects
		String name = "";
		Object obj = null;
		while (resultSet.next()) {
			for(int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++){
				name = resultSet.getMetaData().getColumnName(i);
				obj = resultSet.getObject(name);
				if(obj != null){				
					results.add(new KeyValueObject(name,obj.toString()));
				}
			}
		}
		resultSet.close();
		return results;
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getSession().removeAttribute("result_msg");
//		request.getSession().removeAttribute("sp_1_type");
//		request.getSession().removeAttribute("sp_2_type");
//		request.getSession().removeAttribute("sp_3_type");
//		request.getSession().removeAttribute("sp_4_type");
		
		request.getSession().removeAttribute("wisard_params");
		request.getSession().removeAttribute("selected_wisard");
		
		String redirect,init,garden_site,cp_role,sp_1_type,sp_2_type,sp_3_type,sp_4_type,build_form_submission,view_wisard_submit,wisard_serial_id,find_by_garden;
		redirect=init=garden_site=cp_role=sp_1_type=sp_2_type=sp_3_type=sp_4_type=build_form_submission=view_wisard_submit=wisard_serial_id=find_by_garden=null;
		
		String result_msg = "";
		
		if((init = request.getParameter("init")) != null){
			if(init.equals("true")){
				ArrayList<KeyValueObject> initBuildFormResults = new ArrayList<KeyValueObject>();
				
				try {
					connector = new ConnectionHandler(log);	
					connector.connect("/opt/postgres_config/db_connect");
					initBuildFormResults = getBuildFormInitInfo(initBuildFormResults,"sitename","sites");
					initBuildFormResults = getBuildFormInitInfo(initBuildFormResults,"wisard_serial_id","wisards");
					initBuildFormResults = buildKeyValuePair(initBuildFormResults,"option_id","option_value","hardware_types");
					connector.disconnect();
					
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
		    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				} catch (ClassNotFoundException e) {
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
		    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				} catch(Exception e){
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
		    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				}
				request.getSession().setAttribute("init", "false");
				request.getSession().setAttribute("initBuildFormResults", initBuildFormResults);
			}
		}
		if((view_wisard_submit = request.getParameter("view_wisard_submit")) != null){
			if(view_wisard_submit.equalsIgnoreCase("true")){
				if((wisard_serial_id = request.getParameter("deployed_wisards")) != null){
					try {
						connector = new ConnectionHandler(log);	
						connector.connect("/opt/postgres_config/db_connect");
						ArrayList<KeyValueObject> wisard_params = new ArrayList<KeyValueObject>();
						wisard_params = getWisardParams(wisard_params,wisard_serial_id);						
						connector.disconnect();											
						request.getSession().setAttribute("wisard_params", wisard_params);
						request.getSession().setAttribute("selected_wisard", wisard_serial_id);
					} catch (SQLException e) {
						StringWriter errors = new StringWriter();
			        	e.printStackTrace(new PrintWriter(errors));
			    		log.write(errors);
			    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
					} catch (ClassNotFoundException e) {
						StringWriter errors = new StringWriter();
			        	e.printStackTrace(new PrintWriter(errors));
			    		log.write(errors);
			    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
					} catch(Exception e){
						StringWriter errors = new StringWriter();
			        	e.printStackTrace(new PrintWriter(errors));
			    		log.write(errors);
			    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
					}
				}
			}
			
		}
		if((find_by_garden = request.getParameter("find_by_garden")) != null && find_by_garden.equalsIgnoreCase("true")){
			String find_by_garden_site;
			if((find_by_garden_site = request.getParameter("find_by_garden_site")) != null){
				ArrayList<KeyValueObject> wisards_by_site  = new ArrayList<KeyValueObject>();
				try{
					connector = new ConnectionHandler(log);	
					connector.connect("/opt/postgres_config/db_connect");
					wisards_by_site = getWisardsBySite(wisards_by_site,find_by_garden_site);
					connector.disconnect();
					request.getSession().setAttribute("selected_garden_site",find_by_garden_site);
					request.getSession().setAttribute("wisards_by_site", wisards_by_site);
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
		    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				} catch (ClassNotFoundException e) {
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
		    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				} catch(Exception e){
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
		    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				}
				
				
			}
		}
		
		
		if((garden_site = request.getParameter("select_garden_site")) != null){
			try{
				request.getSession().setAttribute("garden_site", garden_site);
				if((cp_role = request.getParameter("select_cp_role")) != null){
					request.getSession().setAttribute("cp_role", cp_role);													  
					if((sp_1_type = request.getParameter("select_sp_1_type")) != null){
						request.getSession().setAttribute("sp_1_type", sp_1_type);
						
					}
					if((sp_2_type = request.getParameter("select_sp_2_type")) != null){
						request.getSession().setAttribute("sp_2_type", sp_2_type);
						
					}
					if((sp_3_type = request.getParameter("select_sp_3_type")) != null){
						request.getSession().setAttribute("sp_3_type", sp_3_type);
						
					}
					if((sp_4_type = request.getParameter("select_sp_4_type")) != null){
						request.getSession().setAttribute("sp_4_type", sp_4_type);
						
					}
							
						
					
				}
			}catch(Exception e){				
				StringWriter errors = new StringWriter();
	        	e.printStackTrace(new PrintWriter(errors));
	    		log.write(errors);
	    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
			}
			
		}
		
		if((build_form_submission = request.getParameter("build_form_submission")) != null){
			if(build_form_submission.equalsIgnoreCase("true")){
				
				
				//Create a hashmap for all the parameter values from the build form
				HashMap<String,String> diag_values = new HashMap<String,String>();
				
				
				//Fetch the enumeration of all the parameter names
				Enumeration<String> params = request.getParameterNames();
				//Declare temp variable for storing paramter name
				String temp;
				
				//While there are parameters left in the enumeration add them to the hashmap				
				while(params.hasMoreElements()){
					temp = params.nextElement();
					//Ignore non-build form parameters
					if(!temp.equals("build_form_submission") && !temp.equals("redirect")){
						//Add values to the hashmap
						diag_values.put(temp,request.getParameter(temp));
					}					
				}
				
				
				/////////////////////////////////////////////////////////
				//
				//			Create the diagnostic table entry
				//
				/////////////////////////////////////////////////////////
				result_msg = result_msg.concat("===== DIAGNOSTIC INSERT =====<br/>" );
				String diag_result = "\r\nINSERT INTO diagnostic (\r\n";
				Iterator<Entry<String, String>> it = diag_values.entrySet().iterator();
				Map.Entry<String, String> keyval;
				while(it.hasNext()){
					keyval = (Map.Entry<String, String>) it.next();
					result_msg = result_msg.concat(keyval.getKey() + " - " + keyval.getValue() + "<br/>");
					if(it.hasNext()){
						diag_result = diag_result.concat("\t" + keyval.getKey() + ",\r\n");
					}
					else{
						diag_result = diag_result.concat("\t" + keyval.getKey() + "\r\n");
					}
				}
				
				
				diag_result = diag_result.concat(")\r\nVALUES (\r\n");
				
				it = diag_values.entrySet().iterator();
				
				while(it.hasNext()){
					keyval = (Map.Entry<String, String>) it.next();
					
					if(!keyval.getKey().equalsIgnoreCase("notes_input")){
						diag_result = diag_result.concat("\t'" + keyval.getValue() + "'");
					}
					else{
						diag_result = diag_result.concat("\t$$" + keyval.getValue() + "$$");
					}
					
					if(it.hasNext()){
						diag_result = diag_result.concat(",\r\n");
					}
					else{
						diag_result = diag_result.concat("\r\n");
					}
					
					
				}
				
				diag_result = diag_result.concat("\r\n);");				
				
				
				/////////////////////////////////////////////////////////
				//
				//			Create the wisard table entry
				//
				/////////////////////////////////////////////////////////
				
				String wis_result = "\r\nINSERT INTO wisards (\r\n";
				wis_result = wis_result.concat("\t wisard_serial_id,\r\n" +
						"\t cpsoftwareversion,\r\n" +
						"\t sites_siteid,\r\n" +
						"\t wisardnet_id,\r\n" +
						"\t wisard_role");
				wis_result = wis_result.concat(")\r\nVALUES (\r\n");
				wis_result = wis_result.concat("\t'" + diag_values.get("wisard_serial_id") + "',\r\n");
				wis_result = wis_result.concat("\t'" + diag_values.get("cp_firmware") + "',\r\n");
				wis_result = wis_result.concat("\t (SELECT siteid from sites WHERE sitename='" + diag_values.get("garden_site") + "'),\r\n");
				wis_result = wis_result.concat("\t'" + diag_values.get("cp_wisardnet_id") + "',\r\n");
				wis_result = wis_result.concat("\t (SELECT id from hardware_types WHERE option_value='" + diag_values.get("cp_role") + "'));\r\n");						
				
				
				try{					
					connector = new ConnectionHandler(log);	
					connector.connect("/opt/postgres_config/db_connect");
					result_msg = result_msg.concat("Executing INSERT statement into diagnostic...");
					log.write("\r\n" + diag_result);
					connector.executeStatement(diag_result);
					result_msg = result_msg.concat("OK<br/>Executing INSERT statement into wisards...");
					log.write("\r\n" + wis_result);
					connector.executeStatement(wis_result);
					result_msg = result_msg.concat("OK<br/><br/>");
					connector.disconnect();
				}catch(SQLException e){
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
		    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				} catch (ClassNotFoundException e) {
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
		    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				}catch(Exception e){
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					log.write(errors.toString());
					result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				}
				
				/////////////////////////////////////////////////////////
				//
				//			Create the sp board table entry
				//
				/////////////////////////////////////////////////////////
				//WHERE DO I LIVE???
				String sp_result;
				
				for(int i = 1; i <= 4; i++){
					if(diag_values.get("sp_" + i + "_type") != null){
						
						sp_result = "\r\nINSERT INTO spboards (\r\n";
						
						sp_result = sp_result.concat(
							"\t sp_serial_id,\r\n" +
							"\t splocation,\r\n" +
							"\t softwareversion,\r\n" +
							"\t wisard_serial_id,\r\n" + 
							"\t sp_type");
						
						sp_result = sp_result.concat(")\r\nVALUES (\r\n");
						sp_result = sp_result.concat("\t'" + diag_values.get("sp_" + i + "_serial_id") + "',\r\n");
						sp_result = sp_result.concat("\t'" + i + "',\r\n");
						sp_result = sp_result.concat("\t'" + diag_values.get("sp_" + i + "_firmware") + "',\r\n");
						sp_result = sp_result.concat("\t (SELECT wisard_serial_id from wisards WHERE wisard_serial_id='" + diag_values.get("wisard_serial_id") + "'),\r\n");
						sp_result = sp_result.concat("\t (SELECT id from hardware_types WHERE option_value='" + diag_values.get("sp_" + i + "_type") + "'));\r\n");		
						
						try{					
							connector = new ConnectionHandler(log);	
							connector.connect("/opt/postgres_config/db_connect");
							result_msg = result_msg.concat("Executing INSERT statement into spboards for SP location " + i + "...");
							log.write("\r\n" + sp_result);
							connector.executeStatement(sp_result);
							result_msg = result_msg.concat("OK<br/>");
							connector.disconnect();
						}catch(SQLException e){
							StringWriter errors = new StringWriter();
				        	e.printStackTrace(new PrintWriter(errors));
				    		log.write(errors);
				    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
						}catch (ClassNotFoundException e) {
							StringWriter errors = new StringWriter();
				        	e.printStackTrace(new PrintWriter(errors));
				    		log.write(errors);
				    		result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
						}catch(Exception e){
							StringWriter errors = new StringWriter();
							e.printStackTrace(new PrintWriter(errors));
							log.write(errors.toString());
							result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
						}
					}
				}

				
				
			}
		}
		
		request.getSession().setAttribute("result_msg", result_msg);
		if((redirect=request.getParameter("redirect")) != null){
			response.sendRedirect(redirect);
		}
	}
	
	

}

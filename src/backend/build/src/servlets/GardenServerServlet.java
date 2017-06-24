package servlets;

import helpers.KeyValueObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nau.rtisnl.SegaWebException;
import utilities.ConnectionHandler;
import utilities.GardenServer;
import utilities.SegaLogger;


/**
 * Servlet implementation class GardenServerServlet
 */
@WebServlet("/GardenServerServlet")
public class GardenServerServlet extends HttpServlet {
	/** Required by java.io.Serializable */
	private static final long	serialVersionUID	= 2459449890697350757L;

	/** Log object */
	private static SegaLogger log;
	/** Connection variable used to handle Postgres JDBC connection */
	private Connection connection = null;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GardenServerServlet() {
        super();
        try {
			log = new SegaLogger("/usr/share/tomcat7/segalogs/GardenServerServlet.txt");
		} catch (IOException e) {
			System.out.println("ERROR: Cannot create logger /usr/share/tomcat7/segalogs/GardenServerServlet.txt");
			e.printStackTrace();
		}
    }


	
	
	/**
	 * Fetches the results for the columns specified from the table parameter
	 * @param columns
	 * @param table_name
	 * @return String array containing results
	 */
	public ArrayList<KeyValueObject> getBuildFormInitInfo(ArrayList<KeyValueObject> results,String col,String table_name) throws SQLException{
		//Create statement
		Statement stm = connection.createStatement();
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = stm.executeQuery("select " + col +" from " + table_name + ";");
		//Iterate over result set and build an array of XYDataPointObjects
		while (resultSet.next()) {
			results.add(new KeyValueObject(col,resultSet.getObject(col).toString()));
		}
		resultSet.close();
		return results;
	}
	
	public ArrayList<KeyValueObject> getWisardsBySite(ArrayList<KeyValueObject> results, String garden_site) throws SQLException{
		//Create statement
		Statement stm = connection.createStatement();
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = stm.executeQuery("SELECT wisards.wisard_serial_id, wisards.wisardnet_id, hardware_types.option_value AS cp_role FROM wisards INNER JOIN hardware_types ON wisards.wisard_role = hardware_types.id WHERE wisards.sites_siteid=(SELECT siteid FROM sites WHERE sitename='" + garden_site + "');");
		//Iterate over result set and build an array of XYDataPointObjects
		while (resultSet.next()) {
			results.add(new KeyValueObject(resultSet.getObject("wisard_serial_id").toString(), "Wisard " + resultSet.getObject("wisardnet_id").toString() + 
					" (" + resultSet.getObject("cp_role").toString() + " - Serial ID: " + 
					resultSet.getObject("wisard_serial_id").toString() + ")"));
		}
		resultSet.close();
		return results;
	}

	public ArrayList<KeyValueObject> getWisardParams(ArrayList<KeyValueObject> results, String wisard_serial_id) throws SQLException{
		
		//Create statement
		Statement stm = connection.createStatement();
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = stm.executeQuery("select * from diagnostic where wisard_serial_id='" + wisard_serial_id + "';");
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
		
		
		String redirect,init,view_gss_submit,gss_select,reconnect,selected_gss,update_param,param_name,param_val,add_new_gs;
		redirect=init=gss_select=view_gss_submit=selected_gss=update_param=param_name=param_val=add_new_gs=null;
		
		String result_msg = "";
		
		if((init = request.getParameter("initGS")) != null){
			if(init.equals("true")){				
				request.getSession().setAttribute("initGS", "false");
				request.getSession().setAttribute("initGardenServers", SegaExperimentServlet.getGardenServers());
			}
		}
		
		
		if((view_gss_submit = request.getParameter("view_gss_submit")) != null){
			if(view_gss_submit.equalsIgnoreCase("true")){
				if((gss_select = request.getParameter("gss_select")) != null){
					request.getSession().setAttribute("selected_gss", SegaExperimentServlet.getGardenServerByName(gss_select));
				}
			}
			
		}
		
		if((reconnect = request.getParameter("reconnect")) != null && reconnect.equalsIgnoreCase("true")){			
			if((selected_gss = request.getParameter("selected_gss")) != null){
				log.write("Reconnecting " + selected_gss + ", calling shutdown() (1/2)");
				SegaExperimentServlet.getGardenServerByName(selected_gss).shutdown();
				log.write("\tOK");
				log.write("Reconnecting " + selected_gss + ", calling execute() (2/2)");
				SegaExperimentServlet.getGardenServerByName(selected_gss).execute();
				log.write("\tOK");
			}
		}
		
		if((update_param = request.getParameter("update_param")) != null && update_param.equalsIgnoreCase("true")){
			if((selected_gss = request.getParameter("selected_gss")) != null){
				if((param_name = request.getParameter("param_name")) != null){
					if((param_val = request.getParameter("param_val")) != null){
						switch(param_name){
							case("is_running"):
								if(param_val.equalsIgnoreCase("true")){
									if(!SegaExperimentServlet.getGardenServerByName(selected_gss).getIs_running()){
										log.write("Changing is_running to true for " + selected_gss + ", calling execute()");
										result_msg = result_msg.concat("Changing is_running to true for " + selected_gss + ", calling execute()");
										SegaExperimentServlet.getGardenServerByName(selected_gss).execute();
									}
									else{
										log.write("Cannot update is_running to true for " + selected_gss + ": Already running");
										result_msg = result_msg.concat("Cannot update is_running to true for " + selected_gss + ": Already running");
									}
								}
								else if(param_val.equalsIgnoreCase("false")){
									if(SegaExperimentServlet.getGardenServerByName(selected_gss).getIs_running()){
										log.write("Changing is_running to false for " + selected_gss + ", calling shutdown()");
										result_msg = result_msg.concat("Changing is_running to false for " + selected_gss + ", calling shutdown()");
										SegaExperimentServlet.getGardenServerByName(selected_gss).shutdown();
									}
									else{
										log.write("Cannot update is_running to false for " + selected_gss + ": Already stopped");
										result_msg = result_msg.concat("Cannot update is_running to false for " + selected_gss + ": Already stoppped");
									}
								}
								else{
									log.write("UNKNOWN param_val passed for " + param_name + ": " + param_val);
									result_msg = result_msg.concat("UNKNOWN param_val passed for " + param_name + ": " + param_val);
								}
								break;
							case("start_on_init"):
								if(param_val.equalsIgnoreCase("true")){
									log.write("Changing start_on_init to true for " + selected_gss);
									result_msg = result_msg.concat("Changing start_on_init to true for " + selected_gss );
									SegaExperimentServlet.getGardenServerByName(selected_gss).setStart_on_init(true);
								}
								else if(param_val.equalsIgnoreCase("false")){
									log.write("Changing start_on_init to false for " + selected_gss);
									result_msg = result_msg.concat("Changing start_on_init to false for " + selected_gss);
									SegaExperimentServlet.getGardenServerByName(selected_gss).setStart_on_init(false);
								}
								else{
									log.write("UNKNOWN param_val passed for " + param_name + ": " + param_val);
									result_msg = result_msg.concat("UNKNOWN param_val passed for " + param_name + ": " + param_val);
								}
								break;
							default:
								log.write("UNKNOWN param_name passed: " + param_name);
								result_msg = result_msg.concat("UNKNOWN param_name passed: " + param_name);
						}
					}
				}
			}
		}
		
		if((add_new_gs = request.getParameter("add_new_gs")) != null){
			
			String gs_name, gs_siteid, gs_ip, gs_init,gs_notes;
			gs_name = gs_siteid = gs_ip = gs_init = gs_notes = null;
			if(		(gs_name = request.getParameter("gs_name")) != null &&
					(gs_siteid = request.getParameter("gs_siteid")) != null &&
					(gs_ip = request.getParameter("gs_ip")) != null &&
					(gs_init = request.getParameter("gs_init")) != null &&
					(gs_notes = request.getParameter("gs_notes")) != null){
				//Insert the garden server into the database
				try{
					ConnectionHandler connector = new ConnectionHandler(log);	
					if (connector.connect("/opt/postgres_config/db_connect")) {
						// Get the distinct values from datavalues table
						String statement = "INSERT INTO garden_servers (\r\n" +
											"	siteid,\r\n" +
											"	sitename,\r\n" +
											"	ip_addr,\r\n" +
											"	is_running,\r\n" +
											"	start_on_init,\r\n" +
											"	description\r\n" +
											")\r\n" +
											"VALUES (\r\n" +
											"	'" + gs_siteid + "',\r\n" +
											"	'" + gs_name + "',\r\n" +
											"	'" + gs_ip + "',\r\n" +
											"	'false',\r\n" +
											"	'" + gs_init + "',\r\n" +
											"	'" + gs_notes + "'\r\n" +
											");\r\n";
						connector.executeStatement(statement);
						connector.disconnect();
						
						//Add the experiment to the garden server list
						SegaExperimentServlet.getGardenServers().add(new GardenServer(gs_ip,gs_name,"/usr/share/tomcat7/segalogs",Boolean.getBoolean(gs_init)));
						//Execute the garden server
						SegaExperimentServlet.getGardenServerByName(gs_name).execute();
					}
				}catch (SegaWebException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				
				
			}
			else{
				//TODO: throw error - alert missing param
			}
			
		}
		request.getSession().setAttribute("result_msg", result_msg);
		if((redirect=request.getParameter("redirect")) != null){
			response.sendRedirect(redirect);
		}
	}
	
	

}

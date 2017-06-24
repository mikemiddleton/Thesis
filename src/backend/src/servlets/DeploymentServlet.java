package servlets;

import helpers.DeploymentObject;
import helpers.DeviceObject;
import helpers.KeyValueObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nau.rtisnl.SegaWebException;

import utilities.ConnectionHandler;
import utilities.SegaLogger;

/**
 * Servlet implementation class DeploymentServlet
 */
@WebServlet("/DeploymentServlet")
public class DeploymentServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5595890476008311031L;
	/**ConnectionHandler object used to connect to Postgres and execute requests */
	private ConnectionHandler connector = null;
	/** Log object */
	private static SegaLogger log;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DeploymentServlet() {
		super();
		try {
			log = new SegaLogger("/usr/share/tomcat7/segalogs/DeploymentServlet.txt");
		} catch (IOException e) {
			System.out.println("ERROR: Cannot create logger /usr/share/tomcat7/segalogs/DeploymentServlet.txt");
			e.printStackTrace();
		}
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
		String result_msg = "";
		request.getSession().removeAttribute("errorAlert");
		request.getSession().removeAttribute("result_msg");
		
		
		if(request.getParameter("initDeviceTypes") != null && request.getParameter("initDeviceTypes").equalsIgnoreCase("true")){
			try {
				connector = new ConnectionHandler(log);	
				connector.connect("/opt/postgres_config/db_connect_v1_5");
				ArrayList<KeyValueObject> results = new ArrayList<KeyValueObject>();
				//Create statement
				String statement = "select devicetype_id,name from devicetype where category='device_type' order by name asc;";
				//Fetch the id and name of each form that a user has saved
				ResultSet resultSet = connector.executeStatement(statement);
				//Iterate over result set
				while (resultSet.next()) {
					results.add(new KeyValueObject(resultSet.getObject("devicetype_id").toString(),resultSet.getObject("name")));
				}

				request.getSession().setAttribute("devicetypes", results);
				resultSet.close();				
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
		}


		if(request.getParameter("getDevicesByType") != null && request.getParameter("getDevicesByType").equalsIgnoreCase("true")){
			String devicetype;
			if((devicetype = request.getParameter("deviceTypes")) != null){
				request.getSession().setAttribute("selectedDeviceType", devicetype);
				try{
					connector = new ConnectionHandler(log);	
					connector.connect("/opt/postgres_config/db_connect_v1_5");

					/**
					 * Fetch device HIDs
					 */
					ArrayList<KeyValueObject> result = new ArrayList<KeyValueObject>();
					//Create statement
					String statement = "select * from device where devicetype_id=" + devicetype + " order by serialnumber asc;";
					//Fetch the id and name of each form that a user has saved
					ResultSet resultSet = connector.executeStatement(statement);
					//Iterate over result set
					while (resultSet.next()) {
						result.add(new KeyValueObject(resultSet.getObject("device_id").toString(),resultSet.getObject("serialnumber").toString()));
					}

					request.getSession().setAttribute("devices", result);
					resultSet.close();	

					/**
					 * Fetch  sites
					 */
					ArrayList<KeyValueObject> sites = new ArrayList<KeyValueObject>();
					//Create statement
					statement = "select site_id,name from site order by name asc;";
					//Fetch the id and name of each form that a user has saved
					resultSet = connector.executeStatement(statement);
					//Iterate over result set
					while (resultSet.next()) {
						sites.add(new KeyValueObject(resultSet.getObject("site_id").toString(),resultSet.getObject("name").toString()));
					}

					request.getSession().setAttribute("sites", sites);
					resultSet.close();

					/**
					 * Fetch deployment types
					 */
					ArrayList<KeyValueObject> deployTypes = new ArrayList<KeyValueObject>();
					//Create statement
					statement = "select deployment_type_id,deployment_type_name from deployment_type order by deployment_type_name asc;";
					//Fetch the id and name of each form that a user has saved
					resultSet = connector.executeStatement(statement);
					//Iterate over result set
					while (resultSet.next()) {
						deployTypes.add(new KeyValueObject(resultSet.getObject("deployment_type_id").toString(),resultSet.getObject("deployment_type_name").toString()));
					}

					request.getSession().setAttribute("deployTypes", deployTypes);
					resultSet.close();

					/**
					 * Fetch plots
					 */
					ArrayList<KeyValueObject> plots = new ArrayList<KeyValueObject>();
					//Create statement
					statement = "select plot_id,name from plot order by name asc;";
					//Fetch the id and name of each form that a user has saved
					resultSet = connector.executeStatement(statement);
					//Iterate over result set
					while (resultSet.next()) {
						plots.add(new KeyValueObject(resultSet.getObject("plot_id").toString(),resultSet.getObject("name").toString()));
					}

					request.getSession().setAttribute("plots", plots);
					resultSet.close();

					/**
					 * Fetch deployments
					 */
					ArrayList<KeyValueObject> deployments = new ArrayList<KeyValueObject>();
					//Create statement
					statement = "select deployment.deploy_id,deployment.device_id,device.serialnumber from deployment inner join device on deployment.device_id=device.device_id where active='true' order by deployment.deploy_id asc;";
					//Fetch the id and name of each form that a user has saved
					resultSet = connector.executeStatement(statement);
					//Iterate over result set
					while (resultSet.next()) {
						deployments.add(new KeyValueObject(resultSet.getObject("deploy_id").toString(),"(" + resultSet.getObject("deploy_id").toString() + ") " +resultSet.getObject("serialnumber").toString()));
					}

					request.getSession().setAttribute("deployments", deployments);
					resultSet.close();

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
			}
		}
		if(request.getParameter("registerDevice") != null && request.getParameter("registerDevice").equalsIgnoreCase("true")){
			String selDevType, selDevHID, selDevHWVer, selDevDesc;
			if(
				(selDevType = request.getParameter("device_type")) != null && !selDevType.isEmpty() &&
				(selDevHID = request.getParameter("device_hid")) != null && !selDevHID.isEmpty() &&
				(selDevHWVer = request.getParameter("device_hwver")) != null &&
				(selDevDesc = request.getParameter("device_desc")) != null){
				
				//Set the selected device parameters as attributes so if a
				//user returns to the form, these will all be populated
				request.getSession().setAttribute("selDevType", selDevType);
				request.getSession().setAttribute("selDevHID", selDevHID);
				request.getSession().setAttribute("selDevHWVer", selDevHWVer);
				request.getSession().setAttribute("selDevDesc", selDevDesc);
				
				
				try {
					connector = new ConnectionHandler(log);	
					connector.connect("/opt/postgres_config/db_connect_v1_5");
					//Create statement
					String statement;
					if(!selDevDesc.isEmpty()){
						statement = "WITH desc_id AS (" +
					
							"	INSERT INTO description" +
							"	(text)" +
							"	VALUES" +
							"	($$" + selDevDesc + "$$)" +
							"	RETURNING description_id" +
							")" +
							"INSERT INTO device (serialnumber, devicetype_id, description_id,hw_version)" +
							"VALUES ('" +
							selDevHID + "'," + 
							selDevType + "," + 
							"(SELECT description_id FROM desc_id),'" +
							selDevHWVer + "');";
					}
					else{
						statement = "INSERT INTO device (serialnumber, devicetype_id,hw_version)" +
								"VALUES ('" +
								selDevHID + "'," + 
								selDevType + ",'" +
								selDevHWVer + "');";
					}
					log.write(statement);
					connector.executeStatement(statement);
					connector.disconnect();
					fetchDevices(request);

				} catch (SegaWebException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					log.write("Caught SegaWebException\r\n\t" + errors);
					result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
					if(errors.toString().contains("org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint")){
						request.getSession().setAttribute("errorAlert", "HID " + selDevHID + " already exists in the database!");
					}
				} catch (ClassNotFoundException e) {
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					log.write("Caught ClassNotFoundException\r\n\t" + errors);
					result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				} catch(Exception e){
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					log.write("Caught Exception\r\n\t" + errors);
					result_msg = result_msg.concat(errors.toString() + "<br/><br/>");
				}
				
						
			}
			else{
				log.write("Missing data in registration");
				result_msg = result_msg.concat("There was missing data in the registration form, please verify inputs and try again.<br/><br/>");
			}


		}
		
		if(request.getParameter("fetchDevices") != null && request.getParameter("fetchDevices").equalsIgnoreCase("true")){
			try{
				fetchDevices(request);
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
		if(request.getParameter("createDeployment") != null && request.getParameter("createDeployment").equalsIgnoreCase("true")){
			String selectedHID,selectedGardenSite,selectedPlot,selectedDepStartDate,selectedDepStartTime,selectedDepStartDateTime,selectedParentDeploy,
			selectedRelativeID,selectedGPSLat,selectedGPSLon,selectedVersion,selectedDeployType;
			try{
				if((selectedHID = request.getParameter("devices")) != null){
					request.getSession().setAttribute("selectedHID", selectedHID);
					//Fetch the rest of the variables necessary for the insert statment

					if((selectedDeployType = request.getParameter("deployment_type")) == null){
						selectedDeployType = "null";
					}else{
						request.getSession().setAttribute("selectedDeployType", selectedDeployType);
					}
					if((selectedGardenSite = request.getParameter("site")) == null){
						selectedGardenSite = "null";
					}
					else{
						request.getSession().setAttribute("selectedGardenSite", selectedGardenSite);
					}
					if((selectedPlot = request.getParameter("plot")) == null){
						selectedPlot = "null";
					}
					else{
						request.getSession().setAttribute("selectedPlot", selectedPlot);
					}

					if ((selectedDepStartDate = request.getParameter("starting_date")) != null && (selectedDepStartTime = request.getParameter("starting_time")) != null) {
						selectedDepStartDateTime = selectedDepStartDate + " " + selectedDepStartTime;
						SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a");
						request.getSession().setAttribute("selectedDepStartDateTime", sdf.parseObject(selectedDepStartDateTime));
						request.getSession().setAttribute("selectedDepStartDate", selectedDepStartDate);
						request.getSession().setAttribute("selectedDepStartTime", selectedDepStartTime);


					}else{
						selectedDepStartDate = "null";
						selectedDepStartDateTime = "null";
						selectedDepStartTime = "null";
					}

					if((selectedParentDeploy = request.getParameter("parent_deploy")) == null){
						selectedParentDeploy = "null";
					}else{
						request.getSession().setAttribute("selectedParentDeploy", selectedParentDeploy);
					}
					if((selectedRelativeID = request.getParameter("relative_id")) == null){
						selectedRelativeID = "null";
					}else{
						request.getSession().setAttribute("selectedRelativeID", selectedRelativeID);
					}
					if((selectedGPSLat = request.getParameter("gps_lat")) == null){
						selectedGPSLat = "null";
					}else{
						request.getSession().setAttribute("selectedGPSLat", selectedGPSLat);
					}
					if((selectedGPSLon = request.getParameter("gps_lon")) == null){
						selectedGPSLon = "null";
					}else{
						request.getSession().setAttribute("selectedGPSLon", selectedGPSLon);
					}
					if((selectedVersion = request.getParameter("version")) == null){
						selectedVersion = "null";
					}else{
						request.getSession().setAttribute("selectedVersion", selectedVersion);
					}



					try{

						connector = new ConnectionHandler(log);	
						connector.connect("/opt/postgres_config/db_connect_v1_5");
						//Create statement
						String statement = "INSERT INTO deployment" +
								"(device_id,site_id,plot_id,active,datetime_start,parent_deploy_id,relative_id,deployment_type_id,lat_long,version)" +
								"VALUES" +
								"(\r\n" + 
								selectedHID + ",\r\n" + 
								selectedGardenSite + ",\r\n" +
								selectedPlot + ",\r\n" +
								"'true',\r\n'" + 
								selectedDepStartDateTime + "',\r\n" + 
								selectedParentDeploy  + ",\r\n'" + 
								selectedRelativeID  + "',\r\n" + 
								selectedDeployType  + ",\r\npoint(" + 
								selectedGPSLat  + "," + 
								selectedGPSLon  + "),\r\n'" + 
								selectedVersion +
								"');";

						log.write(statement);
						connector.executeStatement(statement);

						connector.disconnect();
						
						fetchDeployments(request);


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

				else{
					//TODO: Can't create a deployment without a device id - throw the error and log it
					log.write("NO HID");
				}
			}catch(ParseException e){
				//TODO: Log it and throw it - stop parsing now, we need the start date parameter
				log.write("PARSE EXCEPTION");
			}



		}

		if(request.getParameter("fetch_deployments") != null && request.getParameter("fetch_deployments").equalsIgnoreCase("true")){
			try{
				fetchDeployments(request);

			}catch (SQLException e) {
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

		if(!result_msg.isEmpty()){
			request.getSession().setAttribute("result_msg", result_msg);
		}
		String redirect;
		if((redirect=request.getParameter("redirect")) != null){
			response.sendRedirect(redirect);
		}



	}

	private void fetchDeployments(HttpServletRequest request) throws SegaWebException, ClassNotFoundException, SQLException {
		connector = new ConnectionHandler(log);	

		connector.connect("/opt/postgres_config/db_connect_v1_5");

		/**
		 * Fetch device HIDs
		 */
		ArrayList<DeploymentObject> result = new ArrayList<DeploymentObject>();
		//Create statement
		String statement = " SELECT " + 
				" 	deployment.deploy_id," + 
				" 	device.serialnumber," + 
				" 	devicetype.name as device_type," + 
				" 	deployment.active," + 
				" 	deployment.datetime_start," + 
				" 	deployment.datetime_stop," + 
				" 	deployment.parent_deploy_id," + 
				" 	deployment.relative_id," + 
				" 	deployment.lat_long," + 
				" 	deployment.version," + 
				" 	deployment_type.deployment_type_name," + 
				" 	site.name as site_name," + 
				" 	plot.name as plot_name" + 
				" FROM" + 
				" 	deployment" + 
				" INNER JOIN" + 
				" 	device " + 
				" ON deployment.device_id=device.device_id" + 
				" INNER JOIN" + 
				" 	site" + 
				" ON deployment.site_id=site.site_id" + 
				" INNER JOIN " + 
				" 	devicetype" + 
				" ON device.devicetype_id=devicetype.devicetype_id" + 
				" INNER JOIN" + 
				" 	deployment_type" + 
				" ON deployment.deployment_type_id=deployment_type.deployment_type_id" + 
				" LEFT JOIN" + 
				" 	plot" + 
				" ON deployment.plot_id=plot.plot_id;";

		DeploymentObject depo;
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = connector.executeStatement(statement);
		//Iterate over result set
		while (resultSet.next()) {
			//Build the deployment object for each result in the table
			//Use an in-line if-else to fill in any empty or null fields
			depo = new DeploymentObject(
					resultSet.getObject("deploy_id") != null ? resultSet.getObject("deploy_id").toString() : "NA",
							resultSet.getObject("serialnumber") != null ? resultSet.getObject("serialnumber").toString() : "NA",
									resultSet.getObject("active") != null ? resultSet.getObject("active").toString() : "NA",
											resultSet.getObject("device_type") != null ? resultSet.getObject("device_type").toString() : "NA",
													resultSet.getObject("datetime_start") != null ? resultSet.getObject("datetime_start").toString() : "NA",
															resultSet.getObject("datetime_stop") != null ? resultSet.getObject("datetime_stop").toString() : "NA",
																	resultSet.getObject("parent_deploy_id") != null ? resultSet.getObject("parent_deploy_id").toString() : "NA",
																			resultSet.getObject("relative_id") != null ? resultSet.getObject("relative_id").toString() : "NA",
																					resultSet.getObject("lat_long") != null ? resultSet.getObject("lat_long").toString() : "NA",
																							resultSet.getObject("version") != null ? resultSet.getObject("version").toString() : "NA",
																									resultSet.getObject("deployment_type_name") != null ? resultSet.getObject("deployment_type_name").toString() : "NA",
																											resultSet.getObject("site_name") != null ? resultSet.getObject("site_name").toString() : "NA",
																													resultSet.getObject("plot_name") != null ? resultSet.getObject("plot_name").toString() : "NA"
					);
			result.add(depo);

		}

		request.getSession().setAttribute("deploymentTable", result);
		resultSet.close();
		connector.disconnect();
		
	}

	private void fetchDevices(HttpServletRequest request) throws SQLException, ClassNotFoundException, SegaWebException {
			connector = new ConnectionHandler(log);	
			connector.connect("/opt/postgres_config/db_connect_v1_5");
			ArrayList<DeviceObject> results = new ArrayList<DeviceObject>();
			String statement = " SELECT" + 
					" 	device.device_id," + 
					" 	device.serialnumber," + 
					" 	devicetype.name," + 
					" 	description.text," + 
					" 	device.hw_version" + 
					" FROM" + 
					" 	device" + 
					" INNER JOIN" + 
					" 	devicetype" + 
					" ON " + 
					" 	device.devicetype_id=devicetype.devicetype_id" + 
					" LEFT JOIN" + 
					" 	description" + 
					" ON" + 
					" 	device.description_id=description.description_id" + 
					" ORDER BY device.device_id asc;";
			//Fetch the id and name of each form that a user has saved
			ResultSet resultSet = connector.executeStatement(statement);
			//Iterate over result set
			while (resultSet.next()) {
				results.add(new DeviceObject(
						resultSet.getObject("device_id") != null ? resultSet.getObject("device_id").toString() : "NA",
						resultSet.getObject("serialnumber") != null ? resultSet.getObject("serialnumber").toString() : "NA",
						resultSet.getObject("name") != null ? resultSet.getObject("name").toString() : "NA",
						resultSet.getObject("text") != null ? resultSet.getObject("text").toString() : "NA",
						resultSet.getObject("hw_version") != null ? resultSet.getObject("hw_version").toString() : "NA"
						));
			}

			request.getSession().setAttribute("deviceTable", results);
			resultSet.close();				
			connector.disconnect();


		
		
	}

}

package servlets;

import helpers.ChannelTreeRetriever;
import helpers.DataFetchHelper;
import helpers.KeyValueObject;
import helpers.MySQLHelper;
import helpers.RBNBSourceObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nau.rtisnl.SegaWebException;

import utilities.ConnectionHandler;
import utilities.SegaLogger;

/**
 * This servlet handles the data request from datarequest.jsp and returns
 * dynamic information that populates the form in order to ensure real-time
 * validation of what data is available
 * 
 * @author jdk85
 * 
 */
@WebServlet("/SegaDataRequestFormServlet")
public class SegaDataRequestFormServlet extends HttpServlet implements Servlet {
	/** Required by java.io.Serializable */
	private static final long	     serialVersionUID = 6888655359700667900L;
	/** Log file and location to write to disk */
	private static SegaLogger	     log;
	/** Absolute URL for the database */
	private static String		 DB_URL	   = "jdbc:mysql://wisard-serv1.egr.nau.edu:3306/sega_testing";
	/** Username used when connecting to the database */
	private static String		 DB_USER	  = "segawebapp";
	/** Password used when connecting to the database */
	private static String		 DB_PASSWORD      = "qxYz!75";
	/** Local date time sdf for parsing dates */
	private static final SimpleDateFormat sdf_local	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static SimpleDateFormat sdf_ipcam = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	
	/** The relative (excluding the serlvetContextPath) path of the IP camera upload folder */
	public static String ipcam_folder = "images/ipcam_ftp_uploads/ipcam_ftp/";
	/**
	 * Helper class used to handle SQL connections
	 * 
	 * @see MySQLHelper
	 */
	private static MySQLHelper	    mysql	    = new MySQLHelper();

	/**
	 * This method takes in a table name parameter and runs a simple query that
	 * returns all the rows from the table
	 * 
	 * @param tableName
	 *            the name of the table to fetch results from
	 * @return A string array containing the results of the query
	 */
	protected String[][] getAllResults(String tableName) {
		mysql.open_connection(DB_URL, DB_USER, DB_PASSWORD);
		mysql.execute_query("select * from " + tableName);
		String[][] results = mysql.get_results();
		mysql.close_connection();
		return results;

	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SegaDataRequestFormServlet() {
		super();
		try {
			log = new SegaLogger("/usr/share/tomcat7/segalogs/SegaDataRequestFormServlet.txt");
		} catch (IOException e) {
			System.out.println("ERROR: Cannot create logger /usr/share/tomcat7/segalogs/SegaDataRequestFormServlet.txt");
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// Reset the error message attribute
		request.getSession().removeAttribute("error_msg");
		request.getSession().removeAttribute("errorMsg");
		//request.getSession().removeAttribute("updatingData");
		request.getSession().removeAttribute("requestDataSize");
		request.getSession().removeAttribute("continute_submit");

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Fetch the servlet context path
		String servletContextPath = getServletContext().getRealPath(File.separator);
				
		// These are placeholder strings used when parsing request parameters
		String data_type_selection, server_addr, current_tab, selected_channels, selected_server, redirect, starting_date, ending_date, starting_time, ending_time, output_style, selected_data_interval, time_interval_text, time_interval_select, updating_data, time_interval_style_select;
		// Init all the placeholder strings to null
		data_type_selection = server_addr = current_tab = selected_channels = selected_server = redirect = starting_date = ending_date = output_style = selected_data_interval = time_interval_text = time_interval_select = updating_data = time_interval_style_select = starting_time = ending_time= null;
		String[] parsedChannels, parsedServer;
		parsedChannels = parsedServer = null;
		
		try {
			// Reset the error message attribute
			request.getSession().removeAttribute("error_msg");
			request.getSession().removeAttribute("errorMsg");
			//request.getSession().removeAttribute("updatingData");
			request.getSession().removeAttribute("requestDataSize");
			request.getSession().removeAttribute("continute_submit");
			// Make sure that the form has provided a redirect attribute
			if ((redirect = request.getParameter("redirect")) != null) {
				// If datarequest.jsp is being loaded for the first time,
				// automatically fetch and
				// return the available data styles
				/*if ((init = request.getParameter("init")) != null) {
					if (init.equals("true")) {
						List<KeyValueObject> server_type_list = new ArrayList<KeyValueObject>();
						// NOTE: These values are *only* defined here...
						// They should eventually live in a database
						server_type_list.add(new KeyValueObject("rbnb_data", "Real-Time Data"));
						server_type_list.add(new KeyValueObject("management_data", "SEGA Management Data"));
						server_type_list.add(new KeyValueObject("archived_sega_data", "Archived SEGA Data"));
						server_type_list.add(new KeyValueObject("archived_network_data", "Archived WiSARDNet Data"));
						request.getSession().setAttribute("initForm", "false");
						request.getSession().setAttribute("server_types", server_type_list);
					}

				}
				*/
				// Current tab variable is used to keep track of which accordion
				// tab the user should be redirected to
				if ((current_tab = request.getParameter("current_tab")) != null) {
					request.getSession().setAttribute("currentTab", current_tab);
				}

				// This returns all available servers when a user has selected a
				// server type
				// The server objects are fetched from the respective tables and
				// parsed by the jsp
				// to get the name/ip/type of each server which is uses to build
				// a drop down select menu
				if ((data_type_selection = request.getParameter("data_type_selection")) != null) {
					request.getSession().setAttribute("selectedDataType", data_type_selection);
				}
				// When the user selects a server, this parameter will get
				// passed along to this servlet
				// The servlet returns the appropriate server information
				// (channels, tables, columns, etc) as
				// well as storing the selected server address for the final
				// request
				if ((server_addr = request.getParameter("data_server_addr")) != null) {
					// Remove old attributes
					request.getSession().removeAttribute("rbnb_channels");
					request.getSession().removeAttribute("psql_channels");
					request.getSession().removeAttribute("ipcam_folders");
					
					request.getSession().setAttribute("selectedServerAddrName", server_addr);

					if (data_type_selection.equalsIgnoreCase("rbnb_data")) {
						ChannelTreeRetriever ctr = new ChannelTreeRetriever(server_addr);
						List<RBNBSourceObject> sources;
						if (request.getParameterValues("includeHidden") != null) {							
							request.getSession().setAttribute("hiddenCheckboxVal", "true");
							sources = ctr.getSourceObjects();

						} else {
							request.getSession().setAttribute("hiddenCheckboxVal", "false");
							sources = ctr.getSourceObjectsExcludingHidden();
						}

						request.getSession().setAttribute("rbnb_channels", sources);
						
					} else if(data_type_selection.equalsIgnoreCase("camera_images")){
						LinkedList<KeyValueObject> folderNames = new LinkedList<KeyValueObject>();
						File ipcam_dir = new File(servletContextPath + server_addr);
						File[] files = ipcam_dir.listFiles();
						
						if(files != null){
							for(File f : files){
								if(f.isDirectory()){
									for(File f1 : f.listFiles()){
										folderNames.add(new KeyValueObject(f.getName(),f1.getName()));
									}
								}
							}
						}else{
							throw new SegaWebException(SegaWebException.error_type.NO_DATA);
						}
						
						if (!folderNames.isEmpty()) {
							request.getSession().setAttribute("ipcam_folders", folderNames);
						}
						else{
							throw new SegaWebException(SegaWebException.error_type.NO_DATA);
						}
						
						
					} else if (data_type_selection.equalsIgnoreCase("archived_sega_data")) {
						ConnectionHandler connector = new ConnectionHandler(log);
						if (connector.connect("/opt/RBNB/processors/client_configs/PostgresHandler/db_connect")) {
							// Get the distinct values from datavalues table
							String statement = "SELECT DISTINCT rbnb_channel_name FROM datavalues;";
							ResultSet resultSet = connector.executeStatement(statement);
							if (resultSet != null && resultSet.isBeforeFirst()) {
								LinkedList<KeyValueObject> channelNames = new LinkedList<KeyValueObject>();
								String cName = "";
								// Build key value object from result set of
								// channel names
								while (resultSet.next()) {
									cName = resultSet.getString("rbnb_channel_name");
									channelNames.add(new KeyValueObject(cName.substring(0, cName.indexOf('/')), cName.substring(cName
											.indexOf('/') + 1)));
								}

								if (!channelNames.isEmpty()) {
									request.getSession().setAttribute("psql_channels", channelNames);
								}
							}
							connector.disconnect();
						}

					}

				}
				if ((selected_channels = request.getParameter("selected_channels")) != null) {
					if ((selected_server = request.getParameter("selected_server")) != null) {
						parsedChannels = selected_channels.split(",");
						parsedServer = selected_server.split(",");

						request.getSession().setAttribute("selectedIPAddress", parsedServer[0]);
						request.getSession().setAttribute("selectedServerName", parsedServer[1]);

						// Check data type
						if (data_type_selection.equalsIgnoreCase("rbnb_data")) {
							DataFetchHelper datafetchhelper = new DataFetchHelper(parsedServer[0]);
							try {
								datafetchhelper.connect("IntervalSink");
								Date[] startEndTimes = datafetchhelper.getValidTimeInterval(parsedChannels);
								datafetchhelper.disconnect();
								request.getSession().removeAttribute("selectedStartDate");
								request.getSession().removeAttribute("selectedEndDate");
								request.getSession().setAttribute("startInterval", startEndTimes[0]);
								request.getSession().setAttribute("endInterval", startEndTimes[1]);
							} catch (Exception e) {
								StringWriter errors = new StringWriter();
								e.printStackTrace(new PrintWriter(errors));
								log.write(errors);
							}
						} else if(data_type_selection.equalsIgnoreCase("camera_images")){
							File ipcam_dir;
							String filename;
							Date startDate = null, endDate = null, tempDate = null;
							
							for(String s : parsedChannels){
								ipcam_dir = new File(servletContextPath + ipcam_folder + s);
								File[] files = ipcam_dir.listFiles();
								if(files != null){
									for(File f : files){
										if(f.isFile()){
											//Trim by the file extension
											filename = f.getName().substring(0,f.getName().indexOf("."));
											
											tempDate = sdf_ipcam.parse(filename);
											
											if(startDate == null || tempDate.getTime() < startDate.getTime()){
												startDate = tempDate;
											}
											
											if(endDate == null || tempDate.getTime() > endDate.getTime()){
												endDate = tempDate;
											}
											
										}
									}
								}else{
									throw new SegaWebException(SegaWebException.error_type.NO_DATA);
								}
							}
							
							
							request.getSession().removeAttribute("selectedStartDate");
							request.getSession().removeAttribute("selectedEndDate");
							if(startDate != null && endDate != null){
								request.getSession().setAttribute("startInterval", startDate);
								request.getSession().setAttribute("endInterval", endDate);
							}
							else{
								throw new SegaWebException(SegaWebException.error_type.NO_DATA);
							}
							
							
							
						} else if (data_type_selection.equalsIgnoreCase("archived_sega_data")) {
						

							ConnectionHandler connector = new ConnectionHandler(log);
							//This is the file location of the database log-in info
							if (connector.connect("/opt/RBNB/processors/client_configs/PostgresHandler/db_connect")) {
								String statement = "SELECT localdatetime FROM datavalues where\r\n" + "rbnb_channel_name='"
										+ parsedChannels[0] + "'\r\n";
								for (int i = 1; i < parsedChannels.length; i++) {
									statement = statement.concat("OR rbnb_channel_name='" + parsedChannels[i] + "'\r\n");
								}
								statement = statement.concat("ORDER BY localdatetime ASC LIMIT 1;");
								ResultSet resultSet = connector.executeStatement(statement);
								resultSet.next();
								Date startDate = sdf_local.parse(resultSet.getString("localdatetime"));

								statement = "SELECT localdatetime FROM datavalues where\r\n" + "rbnb_channel_name='" + parsedChannels[0]
										+ "'\r\n";
								for (int i = 1; i < parsedChannels.length; i++) {
									statement = statement.concat("OR rbnb_channel_name='" + parsedChannels[i] + "'\r\n");
								}
								statement = statement.concat("ORDER BY localdatetime DESC LIMIT 1;");
								resultSet = connector.executeStatement(statement);
								resultSet.next();
								Date endDate = sdf_local.parse(resultSet.getString("localdatetime"));

								request.getSession().removeAttribute("selectedStartDate");
								request.getSession().removeAttribute("selectedEndDate");
								request.getSession().setAttribute("startInterval", startDate);
								request.getSession().setAttribute("endInterval", endDate);

								connector.disconnect();
							}

						}
					}
					request.getSession().setAttribute("selectedChannels", selected_channels);
				}
				if ((selected_data_interval = request.getParameter("selected_data_interval")) != null) {
					// Case '0' is 'Date Range'
					if (selected_data_interval.equals("0")) {
						request.getSession().setAttribute("selectedDataIntervalName", "Date Range");
						if ((starting_date = request.getParameter("starting_date")) != null) {
							if ((ending_date = request.getParameter("ending_date")) != null) {
								starting_date = starting_date + " 00:00:00";
								ending_date = ending_date + " 23:59:59";
								SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
								request.getSession().setAttribute("selectedStartDate", sdf.parseObject(starting_date));
								request.getSession().setAttribute("selectedEndDate", sdf.parseObject(ending_date));

							}

						}
					}
					// Case '1' is 'Date and Time'
					if (selected_data_interval.equals("1")) {
						request.getSession().setAttribute("selectedDataIntervalName", "Date and Time");
						if ((starting_date = request.getParameter("starting_date")) != null) {
							if ((ending_date = request.getParameter("ending_date")) != null) {
								if ((starting_time = request.getParameter("starting_time")) != null) {
									if ((ending_time = request.getParameter("ending_time")) != null) {
										starting_date = starting_date + " " + starting_time;
										ending_date = ending_date + " " + ending_time;
										SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a");
										request.getSession().setAttribute("selectedStartDate", sdf.parseObject(starting_date));
										request.getSession().setAttribute("selectedEndDate", sdf.parseObject(ending_date));
										request.getSession().setAttribute("selectedStartTime", starting_time);
										request.getSession().setAttribute("selectedEndTime", ending_time);
									}
								}

							}

						}
					}
					// Case '2' is 'Time Interval'
					else if (selected_data_interval.equals("2")) {
						if ((time_interval_style_select = request.getParameter("interval_style_select")) != null) {
							request.getSession().setAttribute("selectedDataIntervalName", time_interval_style_select);
							if ((time_interval_text = request.getParameter("time_interval_text")) != null) {
								if ((time_interval_select = request.getParameter("time_interval_select")) != null) {
									long duration = Long.parseLong(time_interval_text);
									if (time_interval_select.equals("Minutes"))
										duration = duration * 60;
									else if (time_interval_select.equals("Hours"))
										duration = duration * 60 * 60;
									else if (time_interval_select.equals("Days"))
										duration = duration * 60 * 60 * 24;
									else if (time_interval_select.equals("Weeks"))
										duration = duration * 60 * 60 * 24 * 7;
									else if (time_interval_select.equals("Years"))
										duration = duration * 60 * 60 * 24 * 365;
									request.getSession().setAttribute("selectedTimeIntervalValue", duration);
									request.getSession().setAttribute("selectedTimeIntervalText", time_interval_text);
									request.getSession().setAttribute("selectedTimeIntervalSelect", time_interval_select);

								}
							}
						}
					}

					request.getSession().setAttribute("selectedDataInterval", selected_data_interval);

				}

				if ((output_style = request.getParameter("output_style_select")) != null) {
					request.getSession().setAttribute("selectedOutputStyle", output_style);
				}
				if ((updating_data = request.getParameter("updating_data")) != null) {
					request.getSession().setAttribute("updatingData", updating_data);
				}

				
			}
		}catch (SegaWebException e){
			// Add the error_msg attribute the redirect from the SegaWebException error.
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
			request.getSession().setAttribute("error_msg", e.getMessage());
			redirect = "/segaWeb/data/form/datarequest.jsp";
			
		}catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
			request.getSession().setAttribute("error_msg", e.getMessage());
			redirect = "/segaWeb/data/form/datarequest.jsp";
		}
		
		if(redirect != null){
			response.sendRedirect(redirect);
		}
		else{
			response.sendRedirect("/segaWeb/data/form/datarequest.jsp");
		}
	}
}

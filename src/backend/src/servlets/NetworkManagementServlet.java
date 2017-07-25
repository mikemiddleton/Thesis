// include this class in the servlet package
package servlets;

// include java io, sql, util
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

// include javax servlet classes
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import org.postgresql.util.PSQLException;

// include sega classes
import edu.nau.rtisnl.SegaWebException;
import helpers.KeyValueObject;
import helpers.Person;
import helpers.SP;
import helpers.SegaDB;
import helpers.SmartList;
import helpers.ValidationIssue;
import helpers.Wisard;
import helpers.SmartList;
import helpers.Experiment;
import helpers.Site;
//import utilities.ConnectionHandler;
import utilities.SegaLogger;
import utilities.ArrayUtilities;
import utilities.CRC;
import utilities.NetManagementCommand;
import utilities.NetResetCommand;
import utilities.PacketGenerator;
import utilities.Validator;

/**
 * Servlet implementation class NetworkManagementServlet
 */
@WebServlet("/NetworkManagementServlet")
public class NetworkManagementServlet extends HttpServlet {
	/* serial version uid */
	private static final long serialVersionUID = 1L;
	/* Log object */
	private static SegaLogger log;
    /* database helper */
	private SegaDB db_helper = null;  
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NetworkManagementServlet() throws ClassNotFoundException{
        super();
        try{
        	log = new SegaLogger("/usr/share/tomcat8/segalogs/NetworkManagementServlet.txt");
        }
        catch(IOException e){
        	System.out.println("ERROR: was not able to create log file /usr/share/tomcat8/segalogs/NetworkManagementServlet.txt");
        	e.printStackTrace();
        }
    }
	
	/**
	 * Fetches the results for the columns specified from the table parameter
	 * @param columns
	 * @param table_name
	 * @return String array containing results
	 * @throws SegaWebException 
	 */
    /*
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
	}*/	
    
	/**
	 * Adds crc to packet and returns the final packet
	 * @param message
	 * @return
	 */
	public byte[] addCrc(byte[] message){
		//Calculate the value for the CRC
		int crc = CRC.compute_crc(ArrayUtilities.convert_to_int_array(message));
		//Parse out the high byte
		byte crc_hi_byte = (byte) (crc >> 8);
		//Parse out the low byte
		byte crc_low_byte = (byte) (crc & 0xFF);

		//Create new packet with extra 2 bytes for the CRC
		byte[] cmdFinal = new byte[message.length + 2];

		//Populate the new packet with the original packet contents
		for(int i = 0; i < message.length; i++){
			cmdFinal[i] = message[i];
		}

		//Add the CRC to the new packet
		cmdFinal[cmdFinal.length-2] = crc_hi_byte;
		cmdFinal[cmdFinal.length-1] = crc_low_byte;
		
		return cmdFinal;


	}
    
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		/*
		System.out.println("hello world\n");
		PrintWriter out = response.getWriter();
		out.println("Hello World\n");
		
		// make ArrayList to store results
		ArrayList<KeyValueObject> wisards_by_site = new ArrayList<KeyValueObject>();
		ArrayList<KeyValueObject> wisards_by_id = new ArrayList<KeyValueObject>();
		ArrayList<KeyValueObject> wisard_params_by_serial = new ArrayList<KeyValueObject>();
		
		// connect to postgres
		try {
			
			// attempt to connect to database
			out.println("Attempting to connect to database");
			
			// database connection file location
			String db_config_location = "/opt/postgres_config/db_connect";
			
			// make new connection handler
			out.println("Creating new ConnectionHandler");
			connector = new ConnectionHandler(log);
			
			// connect to database
			//if(connector.connect("/opt/postgres_config/db_connect") == true){
			//	out.println("null pointer exception");
			//	throw new NullPointerException();	
			//}
			
			// connect to Postgresql database
			connector.connect("/opt/postgres_config/db_connect");
			
			// perform queries
			wisards_by_site = getWisardsBySite(wisards_by_site, "Arboretum");
			wisards_by_id = getWisardsByNetID(wisards_by_id, "0");
			ResultSet resultSet = getListOfTables();
			wisard_params_by_serial = getWisardParams(wisard_params_by_serial, "0101160000000044");
			
			// display comfirmation message
			out.println("Made It\n");
			out.println("* "+wisards_by_id);
			
			// dissconnect from postgresql
			connector.disconnect();
			
			// loop through resultSet and print table names
			out.println("Table Names\n");
			while(resultSet.next()){
				out.println(resultSet.getObject("table_name").toString());
			}
			
			
			// print out arrayList
			out.println("\nWiSARDs by site\n");
			for(KeyValueObject o : wisards_by_site){
				out.println(o.getValue());
			}
			
			// print out arrayList
			out.println("\nWiSARDs by ID\n");
			if(wisards_by_id == null)
				out.println("it's null bruh");
			for(KeyValueObject o2 : wisards_by_id){
				out.println(o2.getValue());
			}
			
			// print out arrayList
			out.println("\nWiSARD params by serial\n");
			for(KeyValueObject o3: wisard_params_by_serial){
				out.println(o3.getKey());
				out.println(o3.getValue());
			}
			
			//out.print(wisards_by_site.toString());
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			out.println(e);
		} catch (SegaWebException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			out.println(e);
		} catch (NullPointerException e){
			// TODO Auto-generated catch block
			out.println(e);
			e.printStackTrace();
		} catch (Exception e){
			out.println(e);
		}
		*/
		
		
		try {
			// make new writer 
			PrintWriter out = response.getWriter();
			// populate a new arraylist with all registered wisards
			out.println("Creating wisard array");
			SmartList<Wisard> wisards = Wisard.getAllWisards();
			out.println("fetched all wisards");
			// display the number of registered wisards
			out.println(wisards.size() + " wisards");
			// print out each wisard and its wisards
			
			// run println on each wisard
			out.println("---------------------------------");
			wisards.forEach(out::println);
			
			//for(Wisard w: wisards){
			//	out.println(w.getDeviceID() + ": " + w.getSerialID() + ":" + Arrays.toString(w.getAttachedSPs().toArray()));
			//}
			
			
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors);
	  }
		
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		// make new database helper
		db_helper = new SegaDB();
		String statement = null;
		
		log.write("init variable: " + request.getParameter("init"));
		log.write("current_tab: " + request.getParameter("current_tab"));
		log.write("---");
		
		// get initial values to popular the form results
		if(request.getParameter("init") != null && request.getParameter("init").equalsIgnoreCase("true")){
			
			// get list of garden sites 
			db_helper.init();

		  // query psql db for info to populate forms
		  try {
			  // do the query
			  ResultSet resultSet_sites = db_helper.getListOfSites();
			  ResultSet resultSet_states = db_helper.getListOfStates();
			  ResultSet resultSet_cproles = db_helper.getListOfCPRoles();
			  ResultSet resultSet_sptypes = db_helper.getListOfSPTypes();
			  ResultSet resultSet_gardenservers = db_helper.getListOfGardenServers();
			  ResultSet resultSet_experiments = db_helper.getAllExperiment(); //
			  ResultSet resultSet_transducertypes = db_helper.getListOfTransducerTypes(); //
			  ResultSet resultSet_deploymenttypes = db_helper.getListOfDeploymentTypes(); //
			  
			  // make the result set
			  ArrayList<KeyValueObject> results_sites = new ArrayList<KeyValueObject>();
			  ArrayList<KeyValueObject> results_states = new ArrayList<KeyValueObject>();
			  ArrayList<KeyValueObject> results_cproles = new ArrayList<KeyValueObject>();
			  ArrayList<KeyValueObject> results_sptypes = new ArrayList<KeyValueObject>();
			  ArrayList<KeyValueObject> results_gardenservers = new ArrayList<KeyValueObject>();
			  ArrayList<KeyValueObject> results_experiments = new ArrayList<KeyValueObject>();
			  ArrayList<KeyValueObject> results_transducertypes = new ArrayList<KeyValueObject>();
			  ArrayList<KeyValueObject> results_deploymenttypes = new ArrayList<KeyValueObject>();
			  
			  // add sites to results
			  while(resultSet_sites.next()){
				  results_sites.add(new KeyValueObject(resultSet_sites.getObject("name").toString(),resultSet_sites.getObject("name").toString()));
			  }
			  results_sites.sort((KeyValueObject o1, KeyValueObject o2)-> ((String)(o1.getValue())).compareTo( (String)(o2.getValue()) ));
			  
			  // add states to results
			  while(resultSet_states.next()){
				  results_states.add(new KeyValueObject(resultSet_states.getObject("state").toString(), resultSet_states.getObject("state").toString()));
			  }
			  results_states.sort((KeyValueObject o1, KeyValueObject o2)-> ((String)(o1.getValue())).compareTo( (String)(o2.getValue()) ));
			  
			  // add cproles to results
			  while(resultSet_cproles.next()){
				  results_cproles.add(new KeyValueObject(resultSet_cproles.getObject("name").toString(), resultSet_cproles.getObject("name").toString()));
			  }
			  results_cproles.sort((KeyValueObject o1, KeyValueObject o2)-> ((String)(o1.getValue())).compareTo( (String)(o2.getValue()) ));
			  
			  // add sptypes to results
			  while(resultSet_sptypes.next()){
				  results_sptypes.add(new KeyValueObject(resultSet_sptypes.getObject("name").toString(), resultSet_sptypes.getObject("name").toString()));
			  }
			  results_sptypes.sort((KeyValueObject o1, KeyValueObject o2)-> ((String)(o1.getValue())).compareTo( (String)(o2.getValue()) ));
			  
			  // add gardenservers to results
			  while(resultSet_gardenservers.next()){
				  results_gardenservers.add(new KeyValueObject(resultSet_gardenservers.getObject("sitename").toString(), resultSet_gardenservers.getObject("sitename").toString()));
			  }
			  results_gardenservers.sort((KeyValueObject o1, KeyValueObject o2)-> ((String)(o1.getValue())).compareTo( (String)(o2.getValue()) ));
			  
			  // add experiments
			  while(resultSet_experiments.next()){
				  results_experiments.add(new KeyValueObject(resultSet_experiments.getObject("name").toString(), resultSet_experiments.getObject("name").toString()));
			  }
			  results_experiments.sort((KeyValueObject o1, KeyValueObject o2)-> ((String)(o1.getValue())).compareTo( (String)(o2.getValue()) ));
			  
			  // add transducertypes
			  while(resultSet_transducertypes.next()){
				  results_transducertypes.add(new KeyValueObject(resultSet_transducertypes.getObject("name").toString(), resultSet_transducertypes.getObject("name").toString()));
			  }
			  results_transducertypes.sort((KeyValueObject o1, KeyValueObject o2)-> ((String)(o1.getValue())).compareTo( (String)(o2.getValue()) ));
			  
			  // add deploymenttypes
			  while(resultSet_deploymenttypes.next()){
				  results_deploymenttypes.add(new KeyValueObject(resultSet_deploymenttypes.getObject("deployment_type_name").toString(), resultSet_deploymenttypes.getObject("deployment_type_name").toString()));
			  }
			  results_deploymenttypes.sort((KeyValueObject o1, KeyValueObject o2)-> ((String)(o1.getValue())).compareTo( (String)(o2.getValue()) ));
			  
			  // set attributes
			  request.getSession().setAttribute("sites", results_sites);
			  request.getSession().setAttribute("states", results_states);
			  request.getSession().setAttribute("cproles", results_cproles);
			  request.getSession().setAttribute("sptypes", results_sptypes);
			  request.getSession().setAttribute("gardenservers", results_gardenservers);
			  request.getSession().setAttribute("experiments", results_experiments);
			  request.getSession().setAttribute("transducertypes", results_transducertypes);
			  request.getSession().setAttribute("deploymenttypes", results_deploymenttypes);
			  
			  // close resultSets
			  resultSet_sites.close();
			  resultSet_states.close();
			  resultSet_cproles.close();
			  resultSet_sptypes.close();
			  resultSet_gardenservers.close();
			  resultSet_experiments.close();
			  resultSet_transducertypes.close();
			  resultSet_deploymenttypes.close();
			
			  // disconnect
			  db_helper.disconnect();
			  
			  request.getSession().setAttribute("initialize", "true");
			
		  } catch (SQLException | SegaWebException e) {
				StringWriter errors = new StringWriter();
	        	e.printStackTrace(new PrintWriter(errors));
	    		log.write(errors);
		  }
		}
		
		// if criteria have been selected, get a list of wisards and append them to the session
		
		else if(request.getParameter("current_tab") != null && request.getParameter("current_tab").equalsIgnoreCase("source_tab")){
			try {
				// try to get a list of all wisards
				SmartList<Wisard> wisards = Wisard.getAllWisards();
				
				// filter based on sp-type
				if(!(request.getParameter("data_sptype_selection") == null) && !(request.getParameter("data_sptype_selection").equals("")))
					wisards = wisards.where((Wisard w) -> (w.getAttachedSPs().where((SP sp) -> request.getParameter("data_sptype_selection").equals(sp.getSPType())).size() > 0 ));

				
				// filter based on wisard relative id
				if(!(request.getParameter("network_id_selection") == null) && !(request.getParameter("network_id_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						//out.println("comparing" + request.getParameter("network_id_selection") + "and" + w.getNetworkID() + ": " + request.getParameter("network_id_selection").equals(w.getNetworkID()));
						return Integer.parseInt(request.getParameter("network_id_selection")) == (w.getNetwork_id());
					});
				}
				
				// filter based on wisard serial id
				if(!(request.getParameter("wisard_serial_selection")== null) && !(request.getParameter("wisard_serial_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						return request.getParameter("wisard_serial_selection").equals(w.getSerial_id());
					});
				}
				
				// filter based on wisard site				
				if(!(request.getParameter("data_site_selection") == null) && !(request.getParameter("data_site_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						return request.getParameter("data_site_selection").equals(w.getSite());
					});
				}			
				
				// wisard role
				if(!(request.getParameter("data_cprole_selection") == null) && !(request.getParameter("data_cprole_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						return request.getParameter("data_cprole_selection").equals(w.getRole());
					});
				}
				
				// filter based on experiment
				if(!(request.getParameter("data_exp_selection") == null) && !(request.getParameter("data_exp_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						return w.getExperiments().where((Experiment e) -> e.getName().equals(request.getParameter("data_exp_selection"))).size() > 0 ;
					});
				}
				
				// filter based on state
				if(!(request.getParameter("data_state_selection") == null) && !(request.getParameter("data_state_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						if(request.getParameter("data_state_selection") != null){
							if(w.getState() != null)
								return w.getState().equals(request.getParameter("data_state_selection"));
						}
						return false;
					});
				}
					
				// filter based on deployment type
				if(!(request.getParameter("data_deploymenttype_selection") == null) && !(request.getParameter("data_deploymenttype_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						return w.getDeploymentType().equals(request.getParameter("data_deploymenttype_selection"));
					});
				}
				
				// filter based on transducertype
				if(!(request.getParameter("data_transducer_selection") == null) && !(request.getParameter("data_transducer_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						return w.getTransducerTypes().where((String s) -> s.equals(request.getParameter("data_transducer_selection"))).size() > 0;
					});
				}
			  
			  // set attributes
			  request.getSession().setAttribute("wisardTable", wisards);
			  request.getSession().setAttribute("current_tab", "source_tab");
				
			} catch (Exception e) {
				log.write(e);
				e.printStackTrace();
				//e.printStackTrace(out);
				//response.sendError(500, e.getMessage());
				  request.getSession().setAttribute("wisardTable", new SmartList<Wisard>());
				  request.getSession().setAttribute("current_tab", "source_tab");
			} // end catch			
			
		} // end elsif("initialize")
		
		else if(request.getParameter("current_tab") != null && request.getParameter("current_tab").equalsIgnoreCase("output_tab")){
			SmartList<Wisard> wisards = (SmartList<Wisard>) request.getSession().getAttribute("wisardTable");
			log.write(wisards.size());
			request.getSession().setAttribute("current_tab", "output_tab");
		}
		
		else if(request.getParameter("current_tab") != null && request.getParameter("current_tab").equalsIgnoreCase("request_tab")){
			SmartList<Wisard> wisards = (SmartList<Wisard>) request.getSession().getAttribute("wisardTable");
			String cmd = request.getParameter("data_cmd_selection");
			log.write("Command selected: " + cmd);
			log.write("wisards in list" + wisards.size());
			request.getSession().setAttribute("current_tab", "request_tab");
			request.getSession().setAttribute("cmd", request.getParameter("data_cmd_selection"));
		}
		
		// Note: Might need to move this
		else{
			
			SmartList<Wisard> wisards = (SmartList<Wisard>) request.getSession().getAttribute("wisardTable");
			
			// --- begin command portion ---
			for(Wisard w: wisards){
				//if(w.getSite().equals("RTISNL-EXP249a-12")){
				
					// get the command type from the JSP page
				
					// get any parameters for the commands
					
					// create an object of the appropriate command class
					NetManagementCommand cmd = new NetResetCommand(w, 0, 3600000);
					
					// get person from browser session
					Person p = (Person) request.getSession().getAttribute("remoteUser");
					
					// create an array for all of the issues
					SmartList<ValidationIssue> issues = Validator.validate(p ,cmd, w);
					log.write("validating command");
					
					// perform validation for the command
					try {
						// add all validation warnings and errors to the message queue
						SmartList<ValidationIssue> errors = issues != null ? issues.where((ValidationIssue i) -> i.getType() == ValidationIssue.Type.ERROR):null;
						SmartList<ValidationIssue> warnings = issues != null ? issues.where((ValidationIssue i) -> i.getType() == ValidationIssue.Type.WARNING):null;
						
						// if any warnings print them
						if(warnings != null && warnings.size() > 0){
							warnings.stream().map((ValidationIssue issue) -> issue.getMessage()).forEach(
									(String warning) -> log.write(warning)
							);
						}
						
						// if any errors, print them
						if(errors != null && errors.size() > 0){
							errors.stream().map((ValidationIssue issue) -> issue.getMessage()).forEach(
									(String error) -> log.write(error)
							);
						}
						
						// connect and send command
						else{
							cmd.runCommand();
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
				//}
				//else{
					//log.write("did not send command to wis " + w.getNetwork_id() + " at " + w.getSite() + " because site unavailable");
				//}
				/*
				String pubTopic = "exp249a-12/cmd/siccs106-01";
				int qos = 2;
				String broker = "tcp://exp249a-12.egr.nau.edu:1883";
				String pubId = "siccs106/cmd_publisher";
				
				log.write("site: " + w.getSite());
				if(w.getSite().equals("RTISNL-EXP249a-12")){
				
					//Connect to MQTT
					out.println(System.getProperty("user.dir"));
					MqttDefaultFilePersistence pubPersistence = new MqttDefaultFilePersistence("./mqtt_persistence");
					
					log.write("created pubPersistence");
					
					try{
						MqttClient pubClient = new MqttClient(broker, pubId, pubPersistence);
						MqttConnectOptions connOpts = new MqttConnectOptions();
						connOpts.setCleanSession(false);
						log.write("Connecting cmd pub to broker " + broker + " as client " + pubId + "...");
						pubClient.connect(connOpts);
						log.write("\tOK - " + pubId + " connected to " + broker);
						
						// get  string hex id from 
						String hex_id = Integer.toHexString(w.getNetwork_id());
						byte dest = Byte.parseByte(hex_id,16);
						
						// reset command for wisard 60 (0x3C)
						byte[] payload = PacketGenerator.Reset_Command_Packet_ExpDur((byte)0x00, (byte)0x00, (byte)0x00, dest, System.currentTimeMillis() + 60*60*1000);
						payload = addCrc(payload);
						
						// reset command for wisard 61 (0x3D)
						//byte[] payload2 = PacketGenerator.Reset_Command_Packet_ExpDur((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3D, System.currentTimeMillis() + 60*60*1000);
						//payload2 = addCrc(payload2);
						
						//publish packet 1
						MqttMessage message = new MqttMessage(payload);
						message.setQos(qos);
						pubClient.publish(pubTopic, message);
						
						//publish packet 2
						//MqttMessage message2 = new MqttMessage(payload2);
						//message2.setQos(qos);
						//pubClient.publish(pubTopic, message2);
						
						pubClient.disconnect();
						log.write("Disconnected... exiting");	
					} catch(MqttException e){
						log.write("Mqtt error occurred" + e.getReasonCode());
						e.printStackTrace(out);
					}
				} // end if
				
				else{
					log.write("did not send command to wis " + w.getNetwork_id() + " at " + w.getSite() + " because site unavailable");
				}*/
			}
			// --- end command portion ---
			
			
			
			// form has been submitted
			//out.println(request.getParameter("data_site_selection"));
			//out.println(request.getParameter("data_state_selection"));
			//out.println(request.getParameter("data_server_selection"));
			//out.println(request.getParameter("data_cprole_selection"));
			//out.println(request.getParameter("data_sptype_selection"));
			//out.println(request.getParameter("wisard_serial_selection"));
			//out.println(request.getParameter("network_id_selection"));
			
			
			/*
			try {
				// try to get a list of all wisards
				SmartList<Wisard> wisards = Wisard.getAllWisards();
				
				// filter based on sp-type
				if(!(request.getParameter("data_sptype_selection") == null) && !(request.getParameter("data_sptype_selection").equals("")))
					wisards = wisards.where((Wisard w) -> (w.getAttachedSPs().where((SP sp) -> request.getParameter("data_sptype_selection").equals(sp.getSPType())).size() > 0 ));
				
				// filter based on wisard relative id
				if(!(request.getParameter("network_id_selection") == null) && !(request.getParameter("network_id_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						//out.println("comparing" + request.getParameter("network_id_selection") + "and" + w.getNetworkID() + ": " + request.getParameter("network_id_selection").equals(w.getNetworkID()));
						return Integer.parseInt(request.getParameter("network_id_selection")) == (w.getNetwork_id());
					});
				}
				
				// filter based on wisard serial id
				if(!(request.getParameter("wisard_serial_selection")== null) && !(request.getParameter("wisard_serial_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						return request.getParameter("wisard_serial_selection").equals(w.getSerial_id());
					});
				}
				
				// filter based on wisard site				
				if(!(request.getParameter("data_site_selection") == null) && !(request.getParameter("data_site_selection").equals(""))){
					wisards = wisards.where((Wisard w) -> {
						return request.getParameter("data_site_selection").equals(w.getSite());
					});
				}
				*/			
				
				// remove duplicates from the Wisard SmartList
				//wisards.removeDuplicates((Wisard a, Wisard b) -> b.getDeviceID() - a.getDeviceID());
				
				// print out each of the wisards
				//out.println("Printing all WiSARDs matching SP-Type");
				//wisards.forEach(out::println);
				
				/*
				// commands section
				String pubTopic = "exp249a-12/cmd/siccs106-01";
				int qos = 2;
				String broker = "tcp://exp249a-12.egr.nau.edu:1883";
				String pubId = "siccs106/cmd_publisher";
				
				//Connect to MQTT
				out.println(System.getProperty("user.dir"));
				MqttDefaultFilePersistence pubPersistence = new MqttDefaultFilePersistence("./mqtt_persistence");
				
				out.println("created pubPersistence");
				
				try{
					MqttClient pubClient = new MqttClient(broker, pubId, pubPersistence);
					MqttConnectOptions connOpts = new MqttConnectOptions();
					connOpts.setCleanSession(false);
					out.println("Connecting cmd pub to broker " + broker + " as client " + pubId + "...");
					pubClient.connect(connOpts);
					out.println("\tOK - " + pubId + " connected to " + broker);
					
					// reset command for wisard 60 (0x3C)
					byte[] payload1 = PacketGenerator.Reset_Command_Packet_ExpDur((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3C, System.currentTimeMillis() + 60*60*1000);
					payload1 = addCrc(payload1);
					
					// reset command for wisard 61 (0x3D)
					byte[] payload2 = PacketGenerator.Reset_Command_Packet_ExpDur((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3D, System.currentTimeMillis() + 60*60*1000);
					payload2 = addCrc(payload2);
					
					//publish packet 1
					MqttMessage message1 = new MqttMessage(payload1);
					message1.setQos(qos);
					pubClient.publish(pubTopic, message1);
					
					//publish packet 2
					MqttMessage message2 = new MqttMessage(payload2);
					message2.setQos(qos);
					pubClient.publish(pubTopic, message2);
					
					pubClient.disconnect();
					out.println("Disconnected... exiting");
				
				} catch(MqttException e){
					out.println("Mqtt error occurred" + e.getReasonCode());
					e.printStackTrace(out);
				}
				*/
				
				
				// return result set of wisards back to requester
				//ArrayList<KeyValueObject> results_wisards = new ArrayList<KeyValueObject>();
				
			  // add sites to results
			  //wisards.forEach(results_wisards.add(new KeyValueObject(getSerialID(),getSite()));
			  //for(Wisard w : wisards){
			  //  results_wisards.add(new KeyValueObject(w.getSerial_id(), w.getSite()));
			  //}
			  
			  /*
			  // set attributes
			  request.getSession().setAttribute("wisardTable", wisards);
			  request.getSession().setAttribute("current_tab", "source_tab");
				
			} catch (Exception e) {
				out.println("I would be redirecting here...");
				e.printStackTrace(out);
				response.sendError(500, e.getMessage());
			} // end catch
			*/
			
			//if((redirect=request.getParameter("redirect")) != null){
				//out.println("I would be redirecting here...");
				//response.sendRedirect(redirect);
			//}
		} // end else
		
		//request.getSession().setAttribute("current_tab", "view_tab");
		//String redirect = request.getParameter("redirect");
		//out.println("redirect");
		
		String redirect = "/segaWeb/index.jsp";
		response.sendRedirect(redirect);
		
	} // end doPost
		
} // end servlet		
		
		// display form entries
		/*
		out.println("[DEBUG] Form Submission Results:");
		out.println(request.getParameter("wisard_serial_id"));
		out.println(request.getParameter("wisard_net_id"));
		out.println(request.getParameter("data_site_selection"));
		out.println(request.getParameter("data_role_selection"));
		out.println(request.getParameter("data_sp_selection"));
		out.println(request.getParameter("data_sensor_selection"));
		*/
		// obtain wisards that match form entries
		//out.println("\nWiSARD Search:");
		
		// if all fields are empty, perform no search
		/*
		if(
			request.getParameter("wisard_serial_id").equals("") &&
			request.getParameter("wisard_net_id").equals("") &&
			request.getParameter("data_site_selection").equals("") &&
			request.getParameter("data_role_selection").equals("") &&
			request.getParameter("data_sp_selection").equals("") &&
			request.getParameter("data_sensor_selection").equals("")){
			out.println("No WiSARDs Found");
		}
		// if WiSARD serial number was entered, then fetch that WiSARD
		else if(
			!request.getParameter("wisard_serial_id").equals("")
			//request.getParameter("wisard_net_id").equals("none") &&
			//request.getParameter("data_site_selection").equals("none") &&
			//request.getParameter("data_role_selection").equals("none") &&
			//request.getParameter("data_sp_selection").equals("none") &&
			//request.getParameter("data_sensor_selection").equals("none")
			){
			
			// query serial number to check for existence
			statement = "SELECT wisards.wisard_serial_id From wisards WHERE wisards.wisard_serial_id='" + request.getParameter("wisard_serial_id") + "';";
			db_helper.init();
			
			try {
				results = db_helper.executeStatement(statement);
				
			} catch (SegaWebException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				if(results.getObject("wisard_serial_id").toString().equals(request.getParameter("wisard_serial_id")))
					//out.println("WiSARD: " + request.getParameter("wisard_serial_id"));
					out.println("WiSARD: " + results.getObject("wisard_serial_id"));
				else
					out.println("No WiSARDs Match your search");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			
			/* TO DO: Replace this with an actual query to verify existence before printing */
		/*
			out.println("WiSARD: " + request.getParameter("wisard_serial_id"));
		}
		// otherwise, obtain WiSARDs from form entries
		else{
			//statement = "SELECT wisards.wisard_serial_id, wisards.wisardnet_id, hardware_types.option_value AS cp_role FROM wisards INNER JOIN hardware_types ON wisards.wisard_role = hardware_types.id WHERE wisards.sites_siteid=(SELECT siteid FROM sites WHERE sitename='" + garden_site + "');";
			//if(!request.getParameter("wisard_serial_id").equals(""))
			statement = "SELECT wisards.wisard_serial_id FROM wisards WHERE ";
			int numModifiers = 0;
			
			if(!request.getParameter("wisard_net_id").equals("")){
				if(numModifiers != 0)
					statement += "AND ";
				statement += "wisards.wisardnet_id='" + request.getParameter("wisard_net_id") + "'";
				numModifiers += 1;
			}
			if(!request.getParameter("data_site_selection").equals("")){
				if(numModifiers != 0)
					statement += "AND ";
				statement += "wisards.sites_siteid=(SELECT siteid FROM sites WHERE sitename='" + request.getParameter("data_site_selection") + "')";
				numModifiers += 1;
			}
			if(!request.getParameter("data_role_selection").equals("")){
				if(numModifiers != 0)
					statement += "AND ";
				statement += "wisard_role=(SELECT id FROM hardware_types WHERE option_value='" + request.getParameter("data_role_selection") + "')";
				numModifiers += 1;
			}
			//request.getParameter("data_sp_selection").equals("")
			//request.getParameter("data_sensor_selection").equals(""))
			statement += ";";
			
			// initialize the database helper
			db_helper.init();
			
			try {
				// attempt to execute the psql statement
				results = db_helper.executeStatement(statement);
				
			} catch (SegaWebException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				if(results.getObject("wisard_serial_id").toString().equals(request.getParameter("wisard_serial_id")))
					//out.println("WiSARD: " + request.getParameter("wisard_serial_id"));
					out.println("WiSARD: " + results.getObject("wisard_serial_id"));
				else
					out.println("No WiSARDs Match your search");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
			try {
					// ToDo: if no results match, print out appropriate message
					//out.println("No WiSARDs match your search");
				
					// loop through result set and display each wisards' info 
					while(results.next()){
						out.println("WiSARD: " + results.getObject("wisard_serial_id"));
					}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/		
package helpers;

/* import helpers */
import helpers.KeyValueObject;
import utilities.ConnectionHandler;
import edu.nau.rtisnl.SegaWebException;
import helpers.SP;
import helpers.Wisard;

/* import java io */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/* import sql driver */
import java.sql.*;

/* import utilities */
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author mike
 * Class definition for database connection helper class which uses the ConnectionHandler Utility
 */
public class SegaDB{	
	// database connection file location
	String db_config_location = "/opt/postgres_config/db_connect";	
	// Connection handler reference
	ConnectionHandler conn = null;
	
	/**
	 * connector for SegaDB class objects
	 * @param void
	 * @return void
	 */
	public SegaDB(){
		/* empty constructor */
	}
	
	/**
	 * initializes connection to database
	 * @throws IOException
	 * @throws NullPointerException
	 * @param none
	 * @return void
	 */
	public void init() throws IOException, NullPointerException{
		// perform initialization		
		try{				
			// create new ConnectionHandler object
			conn = new ConnectionHandler();
			
			// connect to Postgresql database
			conn.connect(db_config_location);
		}
		catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NullPointerException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * disconnects from the database
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws SQLException
	 */
	public void disconnect() throws IOException, NullPointerException, SQLException{
		conn.disconnect();
	}
	
	/**
	 * returns an arraylist of the tables in the database
	 * @param void
	 * @return ResultSet
	 */
	public ResultSet getListOfTables() throws SQLException, SegaWebException{
		// create statement
		String statement = "SELECT table_schema,table_name FROM information_schema.tables ORDER BY table_schema,table_name;";		
		// query database and get list of table names
		ResultSet resultSet = conn.executeStatement(statement);		
		// return arraylist of results
		return resultSet;
	}
	
	/**
	 * obtains a set of properties for a single wisard
	 * @param results
	 * @param wisard_serial_id
	 * @return results
	 * @throws SQLException
	 * @throws SegaWebException
	 */
	public ArrayList<KeyValueObject> getWisardParams(ArrayList<KeyValueObject> results, String wisard_serial_id) throws SQLException, SegaWebException{		
		//Create statement
		String statement = "select * from diagnostic where wisard_serial_id='" + wisard_serial_id + "';";		
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = conn.executeStatement(statement);		
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
	 * writes the contents of a keyvalue pair to the passed in PrintWriter 
	 * @param results
	 * @param outputWriter
	 */
	public void displayResults(ArrayList<KeyValueObject> results, PrintWriter outputWriter){
		for(KeyValueObject o : results){
			outputWriter.println(o.getValue());
		}
	}
	
	/**
	 * obtains a list of wisards whose site parameter match that of the passed string
	 * @param results
	 * @param garden_site
	 * @return results
	 * @throws SQLException
	 * @throws SegaWebException
	 */
	public ArrayList<KeyValueObject> getWisardsBySite(ArrayList<KeyValueObject> results, String garden_site) throws SQLException, SegaWebException{	
		//Create statement
		String statement = "SELECT wisards.wisard_serial_id, wisards.wisardnet_id, hardware_types.option_value AS cp_role FROM wisards INNER JOIN hardware_types ON wisards.wisard_role = hardware_types.id WHERE wisards.sites_siteid=(SELECT siteid FROM sites WHERE sitename='" + garden_site + "');";
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = conn.executeStatement(statement);
		//Iterate over result set and build an array of XYDataPointObjects
		while (resultSet.next()) {
			results.add(new KeyValueObject(resultSet.getObject("wisard_serial_id").toString(), "Wisard " + resultSet.getObject("wisardnet_id").toString() + 
					" (" + resultSet.getObject("cp_role").toString() + " - Serial ID: " + 
					resultSet.getObject("wisard_serial_id").toString() + ")"));
		}
		resultSet.close();
		return results;
	}

	/**
	 * obtains a list of wisards whose netID parameter matches the passed parameter
	 * @param results
	 * @param wisard_ID
	 * @return results
	 * @throws SQLException
	 * @throws SegaWebException
	 */
	public ArrayList<KeyValueObject> getWisardsByNetID(ArrayList<KeyValueObject> results, String wisard_ID) throws SQLException, SegaWebException{	
		//Create statement
		String statement = "SELECT wisards.wisard_serial_id, wisards.wisardnet_id FROM wisards WHERE wisards.wisardnet_id='0';";
		ResultSet resultSet = conn.executeStatement(statement);		
		if(resultSet == null)
			return null;		
		while (resultSet.next()) {
			results.add(new KeyValueObject(resultSet.getObject("wisardnet_id").toString(), "Wisard " + resultSet.getObject("wisardnet_id").toString() + 
					" (" + " - Serial ID: " + 
					resultSet.getObject("wisard_serial_id").toString() + ")"));
		}		
		return results;
	}
	
	/**
	 * obtains a list of garden sites 
	 * @return resultSet
	 * @throws SQLException
	 * @throws SegaWebException
	 */
	public ResultSet getListOfSites() throws SQLException, SegaWebException{
		String statement = "SELECT site.site_id, site.name FROM site"; // this is the query for the segav1_5 database
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/**
	 * obtains a site based on its id 
	 * @return resultSet
	 * @throws SQLException
	 * @throws SegaWebException
	 */
	public ResultSet getSiteByName(String name) throws SQLException, SegaWebException{
		String statement = "SELECT * FROM site WHERE site.site_id='" + name + "'"; // this is the query for the segav1_5 database
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/* 
	 * returns a ResultSet of all states with registered garden locations
	 */
	public ResultSet getListOfStates() throws SQLException, SegaWebException{
		String statement = "select distinct state from site where state is not null and (state = '') is false;";
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/* 
	 * returns a ResultSet of all garden servers (not gs objects)
	 */
	public ResultSet getListOfGardenServers() throws SQLException, SegaWebException{
		String statement = "SELECT garden_servers.sitename FROM garden_servers";
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/* 
	 * returns a resultSet of all device types (cp roles + sp + sensor)
	 */
	public ResultSet getListOfDeviceTypes() throws SQLException, SegaWebException{
		String statement = "SELECT devicetype.name FROM devicetype";
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/*
	 * returns a ResultSet of device categories
	 */
	public ResultSet getListOfDeviceCategories() throws SQLException, SegaWebException{
		String statement = "SELECT devicetype.category FROM devicetype";
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/* 
	 * returns a ResultSet of all CP roles
	 */
	public ResultSet getListOfCPRoles() throws SQLException, SegaWebException{
		String statement = "SELECT devicetype.name FROM devicetype WHERE devicetype.category='cp_role'";
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/*
	 * returns a ResultSet of all SP types
	 */
	public ResultSet getListOfSPTypes() throws SQLException, SegaWebException{
		String statement = "SELECT devicetype.name FROM devicetype WHERE devicetype.category='sp_type'";
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/* 
	 * returns result set of all registered wisards
	 */
	public ResultSet getAllWisards() throws SQLException, SegaWebException{
		String statement = 
				"SELECT device.device_id, device.serialnumber, description.text, device.hw_version, deployment.relative_id, site.name, devicetype.name " +
				 "FROM device " +
				  "LEFT JOIN description ON device.description_id=description.description_id " +
				  "LEFT JOIN devicetype ON device.devicetype_id=devicetype.devicetype_id " + 
				  "LEFT JOIN deployment ON device.device_id=deployment.device_id " +
				  "LEFT JOIN site ON deployment.site_id=site.site_id " +
				 "WHERE devicetype.name='CP' AND deployment.active='TRUE'" +
				 "ORDER BY device.device_id asc;";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		return resultSet;
	}
	
	/* 
	 * returns result set of a single wisard object specified by device id
	 */
	public ResultSet getWisardByID(int id) throws SQLException, SegaWebException{
		String statement = 
				"SELECT device.device_id, device.serialnumber, description.text, device.hw_version, deployment.relative_id " +
				 "FROM device " +
				  "LEFT JOIN description ON device.description_id=description.description_id "+
				  "LEFT JOIN devicetype ON device.devicetype_id=devicetype.devicetype_id " + 
				 "WHERE devicetype.name='CP' AND device.device_id='" + id + "' " +
				 "ORDER BY device.device_id asc;";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		return resultSet;
	}
	
	/*
	 * returns result set of all attached SPs for a given wisard device id
	 */
	public ResultSet getWisardSPs(int wisard_id) throws SQLException, SegaWebException{
		String statement = 
				"SELECT device.device_id, device.serialnumber, device.hw_version, devicetype.name, dev2.device_id AS parent_id, deployment.relative_id " +
				 "FROM device " +
				  "LEFT JOIN devicetype ON device.devicetype_id=devicetype.devicetype_id "+
				  "LEFT JOIN deployment ON device.device_id=deployment.device_id "+
				  "LEFT JOIN deployment AS dep2 ON deployment.parent_deploy_id=dep2.deploy_id " +
				  "LEFT JOIN device AS dev2 ON dep2.device_id=dev2.device_id " +
				"WHERE devicetype.name like 'SP%' and devicetype.category='device_type' and dev2.device_id='" + wisard_id + "' " +
				"ORDER BY device.device_id asc;";		
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/*
	 * returns a result set of all deployments in the sega db
	 */
	public ResultSet getAllDeployments() throws SegaWebException{
		String statement = 
				"SELECT * FROM deployment;";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/*
	 * returns a result set of all active deployments in the sega db
	 */
	public ResultSet getAllActiveDeployments() throws SegaWebException{
		String statement = 
				"SELECT * FROM deployment WHERE active IS NOT FALSE;";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/*
	 * returns a result set of all active deployments that a particular user has either write or full access to
	 */
	public ResultSet getAllActiveDeploymentsForPerson(Person p) throws SegaWebException{
		String statement = 
				"SELECT deployment.*" +
				"FROM person" +
					"LEFT JOIN permission_entity ON person.person_id=permission_entity.person_id" +
					"LEFT JOIN permission ON permission_entity.permission_entity_id=permission.permission_entity_id" +
					"LEFT JOIN permission_resource ON permission.permission_resource_id=permission_resource.permission_resource_id" +
					"LEFT JOIN deployment ON permission_resource.deploy_id=deployment.deploy_id" +
				"WHERE deployment.active='TRUE' AND permission.access_level!='' AND permission.access_level!='read' AND person.person_id='" + p.getPerson_id() +"'" +
				"ORDER BY deployment.deploy_id asc;";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/*
	 * returns a result set of all persons who have write or full access to a specified deployment
	 */
	public ResultSet getAllPersonForDeployment(Deployment d) throws SegaWebException{
		String statement = 
				"SELECT person.*" +
				"FROM person" +
					"LEFT JOIN permission_entity ON person.person_id=permission_entity.person_id" +
					"LEFT JOIN permission ON permission_entity.permission_entity_id=permission.permission_entity_id" +
					"LEFT JOIN permission_resource ON permission.permission_resource_id=permission_resource.permission_resource_id" +
					"LEFT JOIN deployment on permission_resource.deploy_id=deployment.deploy_id" +
				"WHERE permission.access_level!='' AND permission.access_level!='read' AND deployment.deploy_id = '" + d.getDeploymentID() + "'" +
				"ORDER BY person.person_id asc;";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	/*
	 * returns a result set of all persons who have write or full access to a specified deployment
	 */
	public ResultSet getAllPersonsForWisardDeployment(Wisard w) throws SegaWebException{
		String statement = 
				"SELECT person.*" +
				"FROM person" +
					"LEFT JOIN permission_entity ON person.person_id=permission_entity.person_id" +
					"LEFT JOIN permission ON permission_entity.permission_entity_id=permission.permission_entity_id" +
					"LEFT JOIN permission_resource ON permission.permission_resource_id=permission_resource.permission_resource_id" +
					"LEFT JOIN deployment on permission_resource.deploy_id=deployment.deploy_id" +
					"LEFT JOIN device on deployment.device_id=device.device_id" +
					"LFET JOIN devicetype on device.devicetype_id=devicetypte.devicetype_id" +
				"WHERE devicetype.name='CP' AND permission.access_level!='' AND permission.access_level!='read' AND deployment.relative_id= '" + w.getNetwork_id() + "'" +
				"ORDER BY person.person_id asc;";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	/*
	 * returns a result set of all persons
	 */
	public ResultSet getAllPersons() throws SegaWebException{
		String statement =
				"SELECT person.* FROM person";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/*
	 * returns a result set of all sites
	 */
	public ResultSet getAllSites() throws SegaWebException{
		String statement = 
				"SELECT * FROM site;";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/*
	 * Performs a psql statement execution by passing the statement to the database helper's
	 * connection handler object.
	 */
	public ResultSet executeStatement(String statement) throws SegaWebException{
		return conn.executeStatement(statement);
	}
	
	/*
	 * returns a result set of experiments that a particular wisard is a part of
	 */
	public ResultSet getWisardExperiments(Wisard w) throws SegaWebException{
		String statement = 
				"SELECT experiment.*" + 
				"FROM experiment" +
					"LEFT JOIN experiment_resource ON experiment_resource.experiment_id=experiment.experiment_id" +
					"LEFT JOIN deployment ON experiment_resource.deploy_id=deployment.deploy_id" +
					"LEFT JOIN device ON deployment.device_id=device.device_id" +
					"LEFT JOIN devicetype ON device.devicetype_id=devicetype.devicetype_id" +
				"WHERE devicetype.name='CP' AND deployment.active='TRUE' AND deployment.relative_id='" + w.getNetwork_id() + "'" +
				"ORDER BY experiment.experiment_id asc;";
		System.out.println(statement);
		ResultSet resultSet = conn.executeStatement(statement);
		if(resultSet == null)
			return null;
		return resultSet;
	}
	
	/*
	 * returns a result set of person and login relationships
	 */
	public ResultSet getLoginPerson(String username, String password) throws SegaWebException{
		String statement = "SELECT * FROM person WHERE person_id IN (SELECT login.person_id FROM login JOIN person ON login.person_id=person.person_id WHERE login.username='" + username + "' AND login.password='" + password + "');" ;
		ResultSet rs = conn.executeStatement(statement);
		if(rs == null)
			return null;
		return rs;
	}
}

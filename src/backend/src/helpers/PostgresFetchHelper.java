package helpers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.nau.rtisnl.SegaWebException;
import utilities.ConnectionHandler;
import utilities.SegaLogger;




public class PostgresFetchHelper {

	/** Postgres ConnectionHandler */
	private ConnectionHandler connector;
	/** Local date time sdf for parsing dates */
	private static final SimpleDateFormat sdf_local = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private int timeout = 1500;
	
	private SegaLogger log;
	public PostgresFetchHelper(ConnectionHandler connector, SegaLogger log){
		this.connector = connector;
		this.log = log;
	}
	
	public void setTimeout(int timeout){
		this.timeout = timeout;
	}
	
	public ArrayList<PostgresDataObject> getData(String[] channelNames, Date start, Date end) throws SegaWebException{
		
		try {
			if(connector.isConnected()){
				connector.executeStatement("SET statement_timeout to "+timeout);
				String statement = "SELECT localdatetime,rbnb_channel_name,datavalue,transducer_serial_id FROM datavalues where\r\n"+
						"(rbnb_channel_name='" + channelNames[0] +"'\r\n"; 
						for(int i = 1; i < channelNames.length; i++){
							statement = statement.concat("OR rbnb_channel_name='" + channelNames[i] + "'\r\n");
						}
					statement = statement.concat(")\r\nAND localdatetime > '" + sdf_local.format(start) + "'\r\n" + 
						"AND localdatetime < '" + sdf_local.format(end) + "';");
					ResultSet resultSet = connector.executeStatement(statement);
					
					if(resultSet != null && resultSet.isBeforeFirst()){
						ArrayList<PostgresDataObject> psqlData = new ArrayList<PostgresDataObject>();
						String rbnb_channel_name = "",
								transducer_serial_id = "";
						Date localdatetime;
						double sample_data;
						PostgresDataObject pdo;
						
						//Build key value object from result set of channel names
						while(resultSet.next()){
							rbnb_channel_name = resultSet.getString("rbnb_channel_name");
							transducer_serial_id = resultSet.getString("transducer_serial_id");
							pdo = findByTransducerID(psqlData,transducer_serial_id);
							if(pdo != null){
								localdatetime = sdf_local.parse(resultSet.getString("localdatetime"));
								sample_data = resultSet.getDouble("datavalue");
								pdo.getSample_data().add(new KeyValueObject(localdatetime.getTime(),sample_data));								
							}
							else{
								log.write("ERROR: problem creating PostgresDataObject for " + rbnb_channel_name);
							}
							
							
						}
						
						if(!psqlData.isEmpty()){
							log.write("PSQLData Size: " + psqlData.size());
							connector.executeStatement("RESET statement_timeout");
							return psqlData;
						}
						else{
							log.write("psqlData is empty for statement:\r\n\r\n" + statement);
						}
					}else{
						log.write("Empty request for \r\n\r\n" + statement);
					}
					
					connector.executeStatement("RESET statement_timeout");
					
					
			}
			
			return null;
			
		}
		catch (SQLException e) {
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors);
    		throw new SegaWebException(e);
		}catch (ParseException e) {
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors);
    		throw new SegaWebException(e);
		}
		
	}
	
	public ArrayList<PostgresDataObject> getDashboardData(String[] channelNames){
		try {
			if(connector.isConnected()){
				String statement;
				
				if(channelNames.length == 1){
					statement = "SELECT localdatetime,rbnb_channel_name,datavalue,transducer_serial_id FROM datavalues WHERE\r\n" +
							"rbnb_channel_name='" + channelNames[0] + "' ORDER BY localdatetime DESC LIMIT 1;";
				}
				else{
					statement = "(SELECT localdatetime,rbnb_channel_name,datavalue,transducer_serial_id FROM datavalues WHERE\r\n" +
							"rbnb_channel_name='exp249a-04_channelized/wisard_6/mod_2/stream_2' ORDER BY localdatetime DESC LIMIT 1)\r\n";
					for(int i = 1; i < channelNames.length; i++){
							statement = statement.concat("UNION\r\n" +
							"(SELECT localdatetime,rbnb_channel_name,datavalue,transducer_serial_id FROM datavalues WHERE\r\n" +
							"rbnb_channel_name='" + channelNames[i] + "' ORDER BY localdatetime DESC LIMIT 1)\r\n");
					}
					statement = statement.concat(";");
				}

				ResultSet resultSet = connector.executeStatement(statement);
				
				if(resultSet != null && resultSet.isBeforeFirst()){						
					ArrayList<PostgresDataObject> psqlData = new ArrayList<PostgresDataObject>();
					String rbnb_channel_name = "",transducer_serial_id = "";
					
					Date localdatetime;
					double sample_data;
					PostgresDataObject pdo;
					
					//Build key value object from result set of channel names
					while(resultSet.next()){
						rbnb_channel_name = resultSet.getString("rbnb_channel_name");
						transducer_serial_id = resultSet.getString("transducer_serial_id");
						//pdo = findByChannelName(psqlData,rbnb_channel_name);
						pdo = findByTransducerID(psqlData,transducer_serial_id);
						if(pdo != null){
							localdatetime = sdf_local.parse(resultSet.getString("localdatetime"));
							sample_data = resultSet.getDouble("datavalue");
							pdo.getSample_data().add(new KeyValueObject(localdatetime.getTime(),sample_data));
							psqlData.add(pdo);
						}
						else{
							log.write("ERROR: problem creating PostgresDataObject for " + rbnb_channel_name);
						}
						
						
					}
					
					if(!psqlData.isEmpty()){
						log.write("PSQLData Size: " + psqlData.size());
						return psqlData;
					}
					else{
						log.write("psqlData is empty for statement:\r\n\r\n" + statement);
					}
				}
				else{
					log.write("Empty request for \r\n\r\n" + statement);
				}
				
					
					
					
			}
			
			return null;
			
		}catch (Exception e) {
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors);
    		return null;
		}
		
	}
	
	/**
	 * 
	 * @param psqlData
	 * @param rbnb_channel_name
	 * @return
	 * @throws SQLException
	 * @throws SegaWebException 
	 */
	@SuppressWarnings("unused")
	private PostgresDataObject findByChannelName(ArrayList<PostgresDataObject> psqlData,String rbnb_channel_name) throws SQLException, SegaWebException {
		for(PostgresDataObject pdo : psqlData){
			if(pdo.getRbnb_channel_name().equalsIgnoreCase(rbnb_channel_name)){
				return pdo;
			}
		}
		//pdo doesn't exist for specified channel name - create a new one
		String wisard_serial_id,sp_serial_id,transducer_serial_id,stream_id,splocation;
		
		String statement = "SELECT transducer_serial_id,wisard_serial_id,sp_serial_id,stream_id,splocation " +
				"FROM transducers where rbnb_channel_name='" + rbnb_channel_name + "';";
		ResultSet resultSet = connector.executeStatement(statement);
		
		if(resultSet != null && resultSet.isBeforeFirst()){
			resultSet.next();
			transducer_serial_id = resultSet.getString("transducer_serial_id");
			wisard_serial_id = resultSet.getString("wisard_serial_id");
			sp_serial_id = resultSet.getString("sp_serial_id");
			stream_id = resultSet.getString("stream_id");
			splocation = resultSet.getString("splocation");
			PostgresDataObject pdo = new PostgresDataObject(rbnb_channel_name,wisard_serial_id,sp_serial_id,transducer_serial_id,stream_id,splocation);
			//Add the new pdo to the psqlData object
			psqlData.add(pdo);
			return pdo;
			
		}
		else{
			return null;
		}
	}
	/**
	 * 
	 * @param psqlData
	 * @param transducer_serial_id
	 * @return
	 * @throws SQLException
	 * @throws SegaWebException 
	 */
	private PostgresDataObject findByTransducerID(ArrayList<PostgresDataObject> psqlData,String transducer_serial_id) throws SQLException, SegaWebException {
		for(PostgresDataObject pdo : psqlData){
			if(pdo.getTransducer_serial_id().equalsIgnoreCase(transducer_serial_id)){
				return pdo;
			}
		}
		//pdo doesn't exist for specified channel name - create a new one
		String rbnb_channel_name,wisard_serial_id,sp_serial_id,stream_id,splocation;
		
		String statement = "SELECT \r\n" +
								"\ttransducerstream.rbnb_channel_name,\r\n" +
								"\ttransducers.wisard_serial_id,\r\n" +
								"\ttransducers.sp_serial_id,\r\n" +
								"\ttransducers.stream_id,\r\n" +
								"\ttransducers.splocation\r\n" +
							"FROM \r\n" +
								"\ttransducers,\r\n" +
								"\ttransducerstream\r\n" +
							"WHERE transducers.transducer_serial_id='" + transducer_serial_id + "'\r\n" +
								"\tAND transducerstream.transducer_serial_id='" + transducer_serial_id + "';";
		
		ResultSet resultSet = connector.executeStatement(statement);
		
		if(resultSet != null && resultSet.isBeforeFirst()){
			resultSet.next();
			rbnb_channel_name = resultSet.getString("rbnb_channel_name");
			wisard_serial_id = resultSet.getString("wisard_serial_id");
			sp_serial_id = resultSet.getString("sp_serial_id");
			stream_id = resultSet.getString("stream_id");
			splocation = resultSet.getString("splocation");
			PostgresDataObject pdo = new PostgresDataObject(rbnb_channel_name,wisard_serial_id,sp_serial_id,transducer_serial_id,stream_id,splocation);
			//Add the new pdo to the psqlData object
			psqlData.add(pdo);
			return pdo;
			
		}
		else{
			return null;
		}
	}


	
	

	
}

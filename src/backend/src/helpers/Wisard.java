package helpers;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import helpers.SmartList;

import edu.nau.rtisnl.SegaWebException;
import helpers.SP;

/* 
 * class definition for wisard objects
 */
public class Wisard implements Serializable{
	private int device_id;
	private String serial_id;
	private String description;
	private String hw_version;
	private int network_id;
	private String site;
	//private String broker;
	//private String role;
	
	/*
	 * constructor method for wisard objects which takes in attributes
	 */
	public Wisard(int dev_id, String serial, String desc, String ver, int net_id, String s, String r) throws SQLException{
		// assign values from Result set to member variables
		device_id = dev_id;
		serial_id = serial;
		description = desc;
		hw_version = ver;
		network_id = net_id;
		site = s;
		//role = r;
	}
	
	/* 
	 * alternate constructor method which takes in a ResultSet object
	 */
	public Wisard(ResultSet rs) throws SQLException{
		this.setDevice_id(rs.getInt("device_id"));
		this.setSerial_id(rs.getString("serialnumber"));
		this.setDescription(rs.getString("text"));
		this.setHw_version(rs.getString("hw_version"));
		this.setNetwork_id(rs.getInt("relative_id"));
		this.setSite(rs.getString("name"));
		//this.setRole(rs.getString("name"));
	}
	
	/* wisard id setter */
	public void setDevice_id(int wis_id){
		device_id = wis_id;
	}
	
	/* wisard id getter */
	public int getDevice_id(){
		return device_id;
	}
	
	/* serial id setter */
	public void setSerial_id(String serial){
		serial_id = serial;
	}
	
	/* serial id getter */
	public String getSerial_id(){
		return serial_id;
	}
	
	/* device description setter */
	public void setDescription(String des){
		description = des;
	}
	
	/* device description getter */
	public String getDescription(){
		return description;
	}
	
	/* hardware version setter */
	public void setHw_version(String ver){
		hw_version = ver;
	}
	
	/* hardware version getter */
	public String getHw_version(){
		return hw_version;
	}
	
	/*
	 * network id setter
	 */
	public void setNetwork_id(int net_id){
		network_id = net_id;
	}
	
	/*
	 * network id getter
	 */
	public int getNetwork_id(){
		return network_id;
	}
	
	public void setSite(String s){
		site = s;
	}
	
	public String getSite(){
		return site;
	}
	
	public Site getSiteObject() throws SQLException, SegaWebException, NullPointerException, IOException{
		SegaDB sdb = new SegaDB();
		sdb.init();
		ResultSet rs = sdb.getSiteByName(site);
		sdb.disconnect();
		rs.next();
		Site s = new Site(rs);
		return s;
	}
	
	/*
	 * get all experiments
	 */
	public SmartList<Experiment> getExperiments() throws NullPointerException, IOException, SegaWebException, SQLException{
		SegaDB sdb = new SegaDB();
		sdb.init();
		ResultSet rs = sdb.getWisardExperiments(this);
		SmartList<Experiment> experiments = new SmartList<Experiment>();
		while(rs.next()){
			Experiment e = new Experiment(rs);
			experiments.add(e);
		}
		return experiments;
	}
	
	/*
	public void setBroker(String b){
		broker = b;
	}
	
	public String getBroker(){
		return broker;
	}
	*/
	
	/*
	public void setRole(String r){
		role = r;
	}
	
	public String getRole(){
		return role;
	}
	*/
	
	/*
	 *  gets and returns an SmartList of child SP objects for this wisard
	 */
	public SmartList<SP> getAttachedSPs() throws SQLException, SegaWebException, NullPointerException, IOException{
		SegaDB sdb = new SegaDB();
		sdb.init();
		ResultSet rs = sdb.getWisardSPs(device_id);
		sdb.disconnect();
		SmartList<SP> attachedSPs = new SmartList<SP>();
		while(rs.next()){
			SP s = new SP(rs);
			s.setParent(this);
			attachedSPs.add(s);
		}
		return attachedSPs;
	}
	
	/*
	 * print out a wisard
	 */
	public String toString(){
		String str = null;
		try {
			str = device_id + " : " + serial_id + " : " + network_id + " : " + description + " : " + hw_version + " : " + network_id + " : " + site + " : " + Arrays.toString(this.getAttachedSPs().toArray());
		} catch (NullPointerException | SQLException | SegaWebException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
	
	/*
	 *  returns and array list of all wisards in the Sega DB
	 */
	public static SmartList<Wisard> getAllWisards() throws NullPointerException, IOException, SQLException, SegaWebException{
		SegaDB sdb = new SegaDB();
		sdb.init();
		ResultSet rs = sdb.getAllWisards();
		sdb.disconnect();
		
		// NOTE: when java 8, do lambda 1-liner
		SmartList<Wisard> wisards = new SmartList<Wisard>();
		while(rs.next()){
			wisards.add(new Wisard(rs));
		}
		return wisards;
	}
	
	/*
	 * returns a SmartList of WiSARDs with the SP type "ST"
	 */
	public static SmartList<Wisard> getAllWisardsWithST() throws Exception{
		SmartList<Wisard> sl = new SmartList<>(getAllWisards());
		//sl.forEach((Wisard w) -> System.out.println(w));
		return sl.where((Wisard w) -> (w.getAttachedSPs().where((SP sp) -> "ST".equals(sp.getSPType())).size() > 0 ));
	}
}
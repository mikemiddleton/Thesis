package helpers;

import java.io.IOException;
import java.sql.SQLException;

import org.postgresql.geometric.PGpoint;

import java.sql.ResultSet;

import edu.nau.rtisnl.SegaWebException;
import utilities.GardenServer;

/*
 * class definition for GardenSite objects
 */
public class GardenSite {
	private int site_id;
	private String name;
	private PGpoint lat_long;
	private PGpoint local_origin;
	private double elevation_m;
	private String site_code;
	//private GardenServer server;
	private String state;
	private String county;
	//private String deployment_type;
	private int description_id;
	private String ip_address;
	
	/*
	 * constructor method which takes in a ResultSet object
	 */
	public GardenSite(ResultSet rs) throws SQLException{
		this.setSiteID(rs.getInt("site_id"));
		this.setName(rs.getString("name"));
		this.setLatLong((PGpoint)rs.getObject("lat_long"));
		this.setLocalOrigin((PGpoint)rs.getObject("local_origin"));
		this.setSiteCode(rs.getString("site_code"));
		this.setDescriptionID(rs.getInt("description_id"));
		this.setIPAddress(rs.getString("ip_address"));
		//this.setServer(server);
		//this.setDeploymentType(rs.getString("deployment_type_name"));
		this.setState(rs.getString("state"));
		this.setCounty(rs.getString("county"));
		//this.setLatLong(rs.get());
		this.setElevation(rs.getDouble("elevation_m"));
	}
	
	/*
	 * site id setter
	 */
	public void setSiteID(int site_id){
		this.site_id = site_id;
	}
	
	/*
	 * site id getter
	 */
	public int getSiteID(){
		return site_id;
	}
	
	/*
	 * garden name setter
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/*
	 * garden name getter
	 */
	public String getName(){
		return name;
	}
	
	/*
	 * garden server setter
	 */
	/*
	public void setServer(GardenServer server){
		this.server = server;
	}
	*/
	
	/*
	 * garden server getter
	 */
	/*
	public GardenServer getServer(){
		return server;
	}
	*/
	
	/*
	 * garden state setter
	 */
	public void setState(String state){
		this.state = state;
	}
	
	/*
	 * garden state getter
	 */
	public String getState(){
		return state;
	}
	
	/*
	 * county setter
	 */
	public void setCounty(String county){
		this.county = county;
	}
	
	/*
	 * county getter
	 */
	public String getCounty(){
		return county;
	}
	
	/*
	 * lat-long setter
	 */
	public void setLatLong(PGpoint lat_long){
		this.lat_long = lat_long;
	}
	
	/*
	 * lat-long getter
	 */
	public PGpoint getLatLong(){
		return lat_long;
	}
	
	/*
	 * local origin setter
	 */
	public void setLocalOrigin(PGpoint local_origin){
		this.local_origin = local_origin;
	}
	
	/*
	 * local origin getter
	 */
	public PGpoint getLocalOrigin(){
		return local_origin;
	}
	
	/*
	 * elevation setter
	 */
	public void setElevation(double elevation_m){
		this.elevation_m = elevation_m;
	}
	
	/*
	 * elevation getter
	 */
	public double getElevation(){
		return elevation_m;
	}
	
	/*
	 * site code setter
	 */
	public void setSiteCode(String site_code){
		this.site_code = site_code;
	}
	
	/*
	 * site code getter
	 */
	public String getSiteCode(){
		return site_code;
	}
	
	/*
	 * description id setter
	 */
	public void setDescriptionID(int description_id){
		this.description_id = description_id;
	}
	
	/*
	 * description id getter
	 */
	public int getDescriptionID(){
		return description_id;
	}
	
	/*
	 * ip address setter
	 */
	public void setIPAddress(String ip_address){
		this.ip_address = ip_address;
	}
	
	/*
	 * ip address getter
	 */
	public String getIPAddress(){
		return ip_address;
	}
	
	/*
	 * returns an array list of all sites in the sega db
	 */
	public static SmartList<GardenSite> getAllSites() throws NullPointerException, IOException, SQLException, SegaWebException{
		SegaDB sdb = new SegaDB();
		sdb.init();
		ResultSet rs = sdb.getAllSites();
		sdb.disconnect();
		
		SmartList<GardenSite> sites = new SmartList<GardenSite>();
		while(rs.next()){
			sites.add(new GardenSite(rs));
		}
		return sites;
	}
}



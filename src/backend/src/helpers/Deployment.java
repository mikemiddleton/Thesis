package helpers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.geometric.PGpoint;

import helpers.SmartList;

import edu.nau.rtisnl.SegaWebException;
import helpers.SP;
import helpers.Wisard;

/*
 * class description for Deployment objects
 */
public class Deployment {
	private int deploy_id;
	private int device_id;
	private int plot_id;
	private int site_id;
	//private String site_name;
	private boolean active;
	private String date_start;
	private String date_end;
	private int parent_deploy_id;
	private String relative_id;
	private PGpoint lat_long;
	private String version;
	
	/* constructor method for Deployment objects */
	public Deployment(ResultSet rs) throws SQLException{
		this.setDeploymentID(rs.getInt("deploy_id"));
		this.setDeviceID(rs.getInt("device_id"));
		this.setPlotID(rs.getInt("plot_id"));
		this.setSiteID(rs.getInt("site_id"));
		this.setDeploymentActive(rs.getBoolean("active"));
		this.setStartDate(rs.getString("date_start"));
		this.setEndDate(rs.getString("date_end"));
		this.setParentDeployID(rs.getInt("parent_deploy_id"));
		this.setRelativeID(rs.getString("relative_id"));
		this.setLatLong((PGpoint)rs.getObject("lat_long"));
		this.setVersion(rs.getString("version"));
	}
	
	/* deployment id setter */
	public void setDeploymentID(int deploy_id){
		this.deploy_id = deploy_id;
	}
	
	/* deployment id getter */
	public int getDeploymentID(){
		return deploy_id;
	}
	
	/* device id setter */
	public void setDeviceID(int device_id){
		this.device_id = device_id;
	}
	
	/* device id getter */
	public int getDeviceID(){
		return device_id;
	}
	
	/* plot id setter */
	public void setPlotID(int plot_id){
		this.plot_id = plot_id;
	}
	
	/* plot id getter */
	public int getPlotID(){
		return plot_id;
	}
	
	/* site id setter */
	public void setSiteID(int site_id){
		this.site_id = site_id;
	}
	
	/* site id getter */
	public int getSiteID(){
		return site_id;
	}
	
	/* site name setter */
	/*
	public void setSiteName(String site_name){
		this.site_name = site_name;
	}
	*/
	
	/* site name getter */
	/*
	public String getSiteName(){
		return site_name;
	}
	*/
	
	/* deployment active setter */
	public void setDeploymentActive(boolean active){
		this.active = active;
	}
	
	/* deployment active getter */
	public boolean getDeploymentActive(){
		return active;
	}
	
	/* start date setter */
	public void setStartDate(String date_start){
		this.date_start = date_start;
	}
	
	/* start date getter */
	public String getStartDate(){
		return date_start;
	}
	
	/* end date setter */
	public void setEndDate(String date_end){
		this.date_end = date_end;
	}
	
	/* end date getter */
	public String getEndDate(){
		return date_end;
	}
	
	/* parent deploy id setter */
	public void setParentDeployID(int parent_deploy_id){
		this.parent_deploy_id = parent_deploy_id;
	}
	
	/* parent deploy id getter */
	public int getParentDeployID(){
		return parent_deploy_id;
	}
	
	/* relative id setter */
	public void setRelativeID(String relative_id){
		this.relative_id = relative_id;
	}
	
	/* relative id getter */
	public String getRelativeID(){
		return relative_id;
	}
	
	/* lat long setter */
	public void setLatLong(PGpoint lat_long){
		this.lat_long = lat_long;
	}
	
	/* lat long getter */
	public PGpoint getLatLong(){
		return lat_long;
	}
	
	/* version setter */
	public void setVersion(String version){
		this.version = version;
	}
	
	/* version getter */
	public String getVersion(){
		return version;
	}
	
	/*
	 * returns an arraylist of all deployments in the sega DB
	 */
	public static SmartList<Deployment> getAllDeployments() throws NullPointerException, IOException, SQLException, SegaWebException{
		SegaDB sdb = new SegaDB();
		sdb.init();
		ResultSet rs = sdb.getAllDeployments();
		sdb.disconnect();
		
		// NOTE: when java 8, do lambda 1-liner
		SmartList<Deployment> deployments = new SmartList<Deployment>();
		while(rs.next()){
			deployments.add(new Deployment(rs));
		}
		return deployments;
	} 
}
package helpers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.nau.rtisnl.SegaWebException;

//class definition for objects of type SP 
public class SP{
	private int device_id;
	private String serial_id;
	private Wisard parent;
	private String slot;
	private String type;
	private String hw_version;
	private boolean active;
	
	/*
	 *  SP constructor which takes in all attributes separately
	 */
	public void SP(int dev, String serial, Wisard wis, String slot, String type, String hw_ver, boolean active){
		device_id = dev;
		serial_id = serial;
		parent = wis;
		this.slot = slot;
		this.type = type;
		hw_version = hw_ver;
		this.active = active;
	}	
	
	/*
	 * alternate SP constructor which takes attributes as a ResultSet
	 */
	public SP(ResultSet rs) throws SQLException, SegaWebException, NullPointerException, IOException{
		this.setDeviceID(rs.getInt("device_id"));
		this.setSerialID(rs.getString("serialnumber"));
		this.setHWVersion(rs.getString("hw_version"));
		this.setSlot(rs.getString("relative_id"));
		this.setType(rs.getString("name"));
		this.setActive(rs.getBoolean("active"));
	}	
	
	public boolean getActive(){
		return active;
	}
	
	public void setActive(boolean active){
		this.active = active;
	}
	
	/* Device ID getter */
	public int getDeviceID(){
		return device_id;
	}
	
	/* Device ID setter */
	public void setDeviceID(int id){
		device_id = id;
	}
	
	/* Serial ID setter */
	public void setSerialID(String serial){
		serial_id = serial;
	}
	
	/* Serial ID getter */
	public String getSerialID(){
		return serial_id;
	}
	
	/* SP type setter */
	public void setSPType(String type){
		this.type = type;
	}
	
	/* SP type getter */
	public String getSPType(){
		return type;
	}
	
	/* parent serial id setter */
	public void setParent(Wisard parent){
		this.parent = parent;
	}
	
	/* parent serial id getter */
	public Wisard getParent(){
		return parent;
	}
	
	/* relative id (slot) getter */
	public String getSlot(){
		return slot;
	}
	
	/* relative id (slot) setter */
	public void setSlot(String slot){
		this.slot = slot;
	}
	
	/* hardware version getter */
	public String getHWVer(){
		return hw_version;
	}
	
	/* hardware version setter */
	public void setHWVersion(String ver){
		hw_version = ver;
	}
	
	/* device type getter */
	public String getType(){
		return type;
	}
	
	/* device type setter */
	public void setType(String type){
		this.type = type;
	}
	
	/*
	 * print out an SP
	 */
	public String toString(){
		String str = null;
		str = serial_id + "";
		return str;
	}
	
}
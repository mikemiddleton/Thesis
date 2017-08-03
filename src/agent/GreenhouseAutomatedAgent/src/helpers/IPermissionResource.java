package helpers;

import java.io.IOException;
import java.sql.SQLException;

import edu.nau.rtisnl.SegaWebException;
import helpers.SmartList;

public interface IPermissionResource {
	
	public SmartList<Person> getAllPersons() throws SQLException, SegaWebException, NullPointerException, IOException;
	
	/*
	private int permission_resource_id;
	private int experiment_id;
	private int site_id;
	private int deploy_id;
	
	// constructor for permission resource
	public PermissionResource(int permission_resource_id, int experiment_id, int site_id, int deploy_id){
		this.permission_resource_id = permission_resource_id;
		this.experiment_id = experiment_id;
		this.site_id = site_id;
		
	}
	
	// get permission resource id
	public int getPermissionResourceID(){
		return this.permission_resource_id;
	}
	
	// set permission resource id
	public void setPermissionResourceID(int p){
		this.permission_resource_id = p;
	}
	
	// get experiment id
	public int getExperimentID(){
		return this.experiment_id;
	}
	
	// set experiment id
	public void setExperimentID(int p){
		this.experiment_id = p;
	}
	
	// get site id
	public int getSiteID(){
		return this.site_id;
	}
	
	// set site id
	public void setSiteID(int s){
		this.site_id = s;
	}
	
	// get deploy id
	public int getDeployID(){
		return this.deploy_id;
	}
	
	// set deploy id
	public void setDeployID(int d){
		this.deploy_id = d;
	}
	
	// returns all persons with write access to this resource
	public SmartList<Person> getAllPersons(){
		
	}
	*/
}

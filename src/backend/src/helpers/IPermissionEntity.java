package helpers;

import java.io.IOException;
import java.sql.SQLException;

import edu.nau.rtisnl.SegaWebException;
import helpers.Deployment;

public interface IPermissionEntity {
	
	//public SmartList<Site> getAllSites();
	//public SmartList<Experiment> getAllExperiments();
	public SmartList<Deployment> getAllDeployments() throws SQLException, SegaWebException, NullPointerException, IOException;
	
	
	
	/*
	private int permission_entity_id;
	private int person_id;
	private int organization_id;
	
	// constructor for permission entitty class
	public PermissionEntity(int permission_entity_id, int person_id, int organization_id){
		this.permission_entity_id = permission_entity_id;
		this.person_id = person_id;
		this.organization_id = organization_id;
	}
	
	// get permission entity id
	public int getPermissionEntityID(){
		return this.permission_entity_id;
	}
	
	// set permission entity id
	public void setPermissionEntityID(int p){
		this.permission_entity_id = p;
	}
	
	// get person id
	public int getPersonID(){
		return this.person_id;
	}
	
	// set person id
	public void setPersonID(int p){
		this.person_id = p;
	}
	
	// get organization id
	public int getOrganizationID(){
		return this.organization_id;
	}
	
	// set organization id
	public void setOrganizationID(int o){
		this.organization_id = o;
	}
	*/
	
	
}

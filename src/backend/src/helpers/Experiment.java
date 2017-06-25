package helpers;

import java.sql.ResultSet;
import java.sql.SQLException;


public class Experiment {
	private int experiment_id;
	private String name;
	private int description_id;
	private int organization_id;
	private int manager_id;
	
	// constructor method
	public Experiment(ResultSet rs) throws SQLException{
		this.setExperiment_id(rs.getInt("experiment_id"));
		this.setName(rs.getString("name"));
		this.setDescription_id(rs.getInt("description_id"));
		this.setOrganization_id(rs.getInt("organization_id"));
		this.setManager_id(rs.getInt("manager_id"));
	}

	public int getExperiment_id() {
		return experiment_id;
	}

	public void setExperiment_id(int experiment_id) {
		this.experiment_id = experiment_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDescription_id() {
		return description_id;
	}

	public void setDescription_id(int description_id) {
		this.description_id = description_id;
	}

	public int getOrganization_id() {
		return organization_id;
	}

	public void setOrganization_id(int organization_id) {
		this.organization_id = organization_id;
	}

	public int getManager_id() {
		return manager_id;
	}

	public void setManager_id(int manager_id) {
		this.manager_id = manager_id;
	}
}
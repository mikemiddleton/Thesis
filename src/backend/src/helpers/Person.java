package helpers;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.nau.rtisnl.SegaWebException;

public class Person implements IPermissionEntity{

	private int person_id;
	private String first;
	private String last;
	private int organization_id;
	private int description_id;
	private String email;
	private String phone;
	
	public Person(ResultSet rs) throws SQLException{
		this.setPerson_id(rs.getInt("person_id"));
		this.setFirst(rs.getString("first"));
		this.setLast(rs.getString("last"));
		this.setOrganization_id(rs.getInt("organization_id"));
		this.setDescription_id(rs.getInt("description_id"));
		this.setEmail(rs.getString("email"));
		this.setPhone(rs.getString("phone"));
	}
	
	@Override
	public SmartList<Deployment> getAllDeployments() throws SQLException, SegaWebException, NullPointerException, IOException{
		// new db helper
		SegaDB sdb = new SegaDB();
		sdb.init();
		ResultSet rs = sdb.getAllActiveDeploymentsForPerson(this);
		sdb.disconnect();
		SmartList<Deployment> deployments = new SmartList<Deployment>();
		while(rs.next()){
			deployments.add(new Deployment(rs));
		}
		return deployments;	
	}

	public int getPerson_id() {
		return person_id;
	}

	public void setPerson_id(int person_id) {
		this.person_id = person_id;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = last;
	}

	public int getOrganization_id() {
		return organization_id;
	}

	public void setOrganization_id(int organization_id) {
		this.organization_id = organization_id;
	}

	public int getDescription_id() {
		return description_id;
	}

	public void setDescription_id(int description_id) {
		this.description_id = description_id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

}

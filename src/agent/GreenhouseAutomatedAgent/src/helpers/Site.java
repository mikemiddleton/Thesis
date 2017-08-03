package helpers;

import java.sql.SQLException;

import org.postgresql.geometric.PGpoint;

import java.sql.ResultSet;

public class Site {
	private int site_id;
	private String name;
	private PGpoint lat_long;
	private PGpoint local_origin;
	private double elevation_m;
	private String site_code;
	private String state;
	private String county;
	private int description_id;
	private String ip_address;
	
	public Site(ResultSet rs) throws SQLException{
		this.setState(rs.getString("state"));
		this.setSite_id(rs.getInt("site_id"));
		this.setName(rs.getString("name"));
		this.setLat_long((PGpoint)rs.getObject("lat_long"));
		this.setLocal_origin((PGpoint)rs.getObject("local_origin"));
		this.setElevation_m(rs.getDouble("elevation_m"));
		this.setSite_code(rs.getString("site_code"));
		this.setCounty(rs.getString("county"));
		this.setDescription_id(rs.getInt("description_id"));
		this.setIp_address(rs.getString("ip_address"));
	}
	
	public int getSite_id() {
		return site_id;
	}
	public void setSite_id(int site_id) {
		this.site_id = site_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PGpoint getLat_long() {
		return lat_long;
	}
	public void setLat_long(PGpoint lat_long) {
		this.lat_long = lat_long;
	}
	public PGpoint getLocal_origin() {
		return local_origin;
	}
	public void setLocal_origin(PGpoint local_origin) {
		this.local_origin = local_origin;
	}
	public double getElevation_m() {
		return elevation_m;
	}
	public void setElevation_m(double elevation_m) {
		this.elevation_m = elevation_m;
	}
	public String getSite_code() {
		return site_code;
	}
	public void setSite_code(String site_code) {
		this.site_code = site_code;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public int getDescription_id() {
		return description_id;
	}
	public void setDescription_id(int description_id) {
		this.description_id = description_id;
	}
	public String getIp_address() {
		return ip_address;
	}
	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}
	
	
}

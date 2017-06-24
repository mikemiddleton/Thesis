package helpers;

import java.sql.*;

public class Validate {
	
	public static boolean checkUser(String email, String pass){
		boolean st = false;
		try{
			// loading drivers for msql
			Class.forName("org.postgresql.Driver");
			
			// creating connection with the database
			Connection conn = DriverManager.getConnection("jdbc:postgresql://romer2.cefns.nau.edu:5432/segav1_5", "postgres", "Se6@2017");
			
			PreparedStatement ps = conn.prepareStatement("select * from register where email=? and pass=?");
			
			ps.setString(1, email);
			ps.setString(2,  pass);
			ResultSet rs = ps.executeQuery();			
			st = rs.next();
			
			
		} catch(Exception e){
			e.printStackTrace();
		}
		return st;
	}
}

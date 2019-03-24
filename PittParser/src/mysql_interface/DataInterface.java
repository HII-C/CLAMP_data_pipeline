package mysql_interface;

import java.sql.*;

public class DataInterface {
	private static String connection = "jdbc:mysql://db01.healthcreek.org";	
	
	public static void QueryRemote(String query) throws Exception {
		try {
			Connection conn = DriverManager.getConnection(connection, System.getenv(""), System.getenv(""));
			Statement statement = conn.createStatement();
			
			statement.executeQuery(query);
			
			statement.close();
			conn.close();
		} catch (Exception e) {
			throw e;
		} finally {
			
		}
	}
	
	public static void main(String[] args) {
		
	}
}

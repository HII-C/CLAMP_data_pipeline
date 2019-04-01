package mysql_interface;

import java.sql.*;

public class DatabaseManager implements DatabaseManagerIntf{

	private static String CONNECTION_URL = "jdbc:mysql://db01.healthcreek.org";


	private static DatabaseManager instance;
	public Connection mConnection;

	private DatabaseManager() {
	}

	public static DatabaseManagerIntf getInstance() {
		if( instance == null ) {
			instance = new DatabaseManager();
		}

		return instance;
	}

	@Override
	public void openConnection() {

		try {
			if( mConnection != null && !mConnection.isClosed() ) {
				return;
			}

			mConnection = DriverManager.getConnection(CONNECTION_URL, System.getenv(""), System.getenv(""));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	@Override
	public void closeConnection() {
		try {
			mConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	@Override
	public void pushSqlQuery( String aQuery ) {
		try {
			Statement statement = mConnection.createStatement();
			statement.executeQuery( aQuery );
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public static void main(String[] args) throws Exception {
		DatabaseManagerIntf database = DatabaseManager.getInstance();

		database.openConnection();
		database.pushSqlQuery("my query");
		database.closeConnection();
	}
}

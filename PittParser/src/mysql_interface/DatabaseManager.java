package mysql_interface;

import java.sql.*;

public class DatabaseManager implements DatabaseManagerIntf{

	private static String CONNECTION_URL = "jdbc:mysql://db01.healthcreek.org:3306/capstone";

	private static DatabaseManager instance;
	public Connection mConnection;

	public String mUserName;
	public String mPassword;

	private DatabaseManager() {
	}

	public static DatabaseManagerIntf getInstance() {
		if( instance == null ) {
			instance = new DatabaseManager();
		}

		return instance;
	}

	public void setCredentials( String aUserName, String aPassword ) {
		mUserName = aUserName;
		mPassword = aPassword;
	}

	@Override
	public void openConnection() {

		try {
			if( mConnection != null && !mConnection.isClosed() ) {
				return;
			}

			mConnection = DriverManager.getConnection(CONNECTION_URL, mUserName, mPassword);
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
			statement.execute( aQuery );
			statement.close();
		} catch (Exception e) {
			if( !(e instanceof com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException) ) {
				System.out.println("\n\n" + aQuery + "\n");
				e.printStackTrace();

				System.exit(0);
			}
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

package kettlebell.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {
	private static final String DB_URL = "jdbc:sqlite::resource:speculator.db";

	public Connection getConnection() throws SQLException {
		Connection connection = DriverManager.getConnection(DB_URL);		
		return connection;
	}

}

package edu.ncsu.alda.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
	private static ConnectionFactory connectionFactory = null;

	private ConnectionFactory() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("MySQL JDBC driver not loaded" + e);
		}
	}

	public Connection getConnection() throws SQLException {
		Connection conn = null;
		String dburl = "jdbc:mysql://junkURL:3306/twitter_loksabha2014";
		conn = (Connection) DriverManager.getConnection(dburl,
				"junkUser", "junkPassword");
		return conn;
	}

	public static ConnectionFactory getInstance() {
		if (connectionFactory == null) {
			connectionFactory = new ConnectionFactory();
		}
		return connectionFactory;
	}
}

package nl.rubend.ipass;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class
SqlInterface {
	private static Connection conn;
	static {
		try {
			Properties prop=new Properties();
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties"));
			Class.forName(prop.getProperty("driver"));
			conn = DriverManager.getConnection(prop.getProperty("url"), prop.getProperty("user"), prop.getProperty("pass"));
		} catch (SQLException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static ResultSet executeQuery(String query) throws SQLException {
		return conn.createStatement().executeQuery(query);
	}
	public static int executeUpdate(String query) throws SQLException {
		return conn.createStatement().executeUpdate(query);
	}
	public static PreparedStatement prepareStatement(String statement) throws SQLException {
		return conn.prepareStatement(statement);
	}
}

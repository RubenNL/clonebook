package nl.rubend.ipass.utils;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class SqlInterface {
	private static Connection conn;
	private static void connect() {
		try {
			Properties prop=new Properties();
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties"));
			Class.forName(prop.getProperty("driver"));
			conn = DriverManager.getConnection(prop.getProperty("url"), prop.getProperty("user"), prop.getProperty("pass"));
		} catch (SQLException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	static {
		connect();
	}
	public static PreparedStatement prepareStatement(String statement) throws SQLException {
		if(conn.isClosed()) {
			System.out.println("CONNETION WAS CLOSED");//Om het te vinden in de TomCat logs.
			connect();
		}
		return conn.prepareStatement(statement);
	}
}

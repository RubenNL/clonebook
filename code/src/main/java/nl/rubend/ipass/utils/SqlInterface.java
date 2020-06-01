package nl.rubend.ipass.utils;

import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
public class SqlInterface implements ServletContextListener {
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
	public static PreparedStatement prepareStatement(String statement) throws SQLException {
		try {
			PreparedStatement testStatement = conn.prepareStatement("select 1");
			ResultSet executed = testStatement.executeQuery();
			executed.next();
			executed.getInt("1");
		} catch(NullPointerException | SQLException e) {
			e.printStackTrace();
			System.out.println("CONNETION WAS CLOSED");//Om het te vinden in de TomCat logs.
			connect();
		}
		return conn.prepareStatement(statement);
	}
}

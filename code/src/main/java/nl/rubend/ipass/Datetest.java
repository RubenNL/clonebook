package nl.rubend.ipass;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Datetest extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			ResultSet set=SqlInterface.executeQuery("SELECT CURDATE(),CURTIME()");
			set.first();
			response.getWriter().write(set.getDate("CURDATE()")+" "+set.getTime("CURTIME()"));
		} catch (SQLException e) {
			e.printStackTrace(response.getWriter());
		}
	}
}

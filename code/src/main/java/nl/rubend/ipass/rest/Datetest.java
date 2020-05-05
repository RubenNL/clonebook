package nl.rubend.ipass.rest;

import nl.rubend.ipass.SqlInterface;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
@Path("/date")
@Produces("application/json")
public class Datetest {
	@GET
	public String getDate() {
		JsonObjectBuilder dateObject = Json.createObjectBuilder();
		try {
			ResultSet set=SqlInterface.executeQuery("SELECT CURDATE(),CURTIME()");
			set.first();
			dateObject.add("date",set.getDate("CURDATE()")+" "+set.getTime("CURTIME()"));
		} catch (SQLException e) {
			e.printStackTrace();
			dateObject.add("error",e.getMessage());
		}
		return dateObject.build().toString();
	}
}

package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.IpassException;
import nl.rubend.ipass.domain.User;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.StringReader;
import java.sql.Date;
import java.util.Properties;

import javax.mail.*;

import javax.mail.internet.*;

import com.sun.mail.smtp.*;
@Path("/register")
@Produces("application/json")
public class Register {
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public String register(String json) {
		JsonObjectBuilder response=Json.createObjectBuilder();
		JsonObject data=Json.createReader(new StringReader(json)).read().asJsonObject();
		String email=data.getString("email");
		User user;
		try {
			user=User.getUser(email);
		} catch (IpassException e) {
			response.add("message",e.getMessage());
			return response.build().toString();
			//e.printStackTrace();
			//user=new User(email);
		}
		user.sendPasswordForgottenUrl();
		response.add("status","OK");
		return response.build().toString();
	}
}

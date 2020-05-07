package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.*;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.StringReader;

@Path("/user")
@Produces("application/json")
public class Register {
	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_JSON)
	public String register(@Context HttpServletRequest req, String json) {
		JsonObjectBuilder response=Json.createObjectBuilder();
		JsonObject data=Json.createReader(new StringReader(json)).read().asJsonObject();
		String email=data.getString("email");
		User user;
		try {
			user=User.getUser(email);
		} catch (UnauthorizedException e) {
			user=new User(email);
		}
		user.sendPasswordForgottenUrl();
		response.add("status","OK");
		return response.build().toString();
	}
	@POST
	@Path("/newPassword")
	public String use(@Context HttpServletRequest req, String json) {
		JsonObjectBuilder response=Json.createObjectBuilder();
		JsonObject data=Json.createReader(new StringReader(json)).read().asJsonObject();
		if(data.getString("password")!=null && data.getString("password").length()>=8 && data.getString("code")!=null) {
			User user=NewPassword.use(data.getString("code"));
			user.setPassword(data.getString("password"));
			req.getSession(true).setAttribute("sessionId",new Session(user).getId());
			response.add("status","OK");
		} else {
			response.add("status","ERR");
			response.add("error","ongeldige waardes");
		}
		return response.build().toString();
	}
	@GET
	@Path("/")
	public String status(@Context HttpServletRequest req) {
		JsonObjectBuilder response=Json.createObjectBuilder();
		User user;
		try {
			user = Utils.getUser(req);
		} catch (UnauthorizedException e) {
			response.add("status","ERR");
			response.add("error",e.getMessage());
			return response.build().toString();
		}
		response.add("status","OK");
		return response.build().toString();
	}
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public String login(@Context HttpServletRequest req, String json) {
		JsonObjectBuilder response=Json.createObjectBuilder();
		JsonObject data=Json.createReader(new StringReader(json)).read().asJsonObject();
		User user;
		try {
			user = User.getUser(data.getString("email"));
		} catch (UnauthorizedException e) {
			response.add("status","ERR");
			response.add("error","gebruiker niet gevonden");
			return response.build().toString();
		}
		if(user.checkPassword(data.getString("password"))) {
			req.getSession(true).setAttribute("sessionId",new Session(user).getId());
			response.add("status","OK");
		} else {
			response.add("status","OK");
			response.add("error","wachtwoord niet geldig");
		}
		return response.build().toString();
	}
	@GET
	@Path("/settings")
	public String getSettings(@Context HttpServletRequest req) {
		JsonObjectBuilder response=Json.createObjectBuilder();
		User user;
		try {
			user = Utils.getUser(req);
		} catch (UnauthorizedException e) {
			response.add("status","ERR");
			response.add("error",e.getMessage());
			return response.build().toString();
		}
		response.add("email",user.getEmail());
		response.add("name",user.getName());
		response.add("status","OK");
		return response.build().toString();
	}
	@POST
	@Path("/settings")
	@Consumes(MediaType.APPLICATION_JSON)
	public String setSettings(@Context HttpServletRequest req, String json) {
		JsonObject data=Json.createReader(new StringReader(json)).read().asJsonObject();
		JsonObjectBuilder response=Json.createObjectBuilder();
		User user;
		try {
			user = Utils.getUser(req);
		} catch (UnauthorizedException e) {
			response.add("status","ERR");
			response.add("error",e.getMessage());
			return response.build().toString();
		}
		if(!user.getEmail().equals(data.getString("email"))) user.setEmail(data.getString("email"));
		if(!user.getName().equals(data.getString("name"))) user.setName(data.getString("name"));
		response.add("status","OK");
		return response.build().toString();
	}
}

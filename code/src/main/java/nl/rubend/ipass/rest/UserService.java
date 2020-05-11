package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserService {
	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response newPasswordMail(@Context HttpServletRequest req, String json) {
		JsonObject data=Json.createReader(new StringReader(json)).read().asJsonObject();
		String email=data.getString("email");
		User user;
		try {
			user=User.getUserByEmail(email);
		} catch (UnauthorizedException e) {
			user=new User(email);
		}
		user.sendPasswordForgottenUrl();
		return Response.ok(new HashMap<String,String>()).build();
	}
	@POST
	@Path("/newPassword")
	public Response use(@Context HttpServletRequest req, String json) {
		JsonObject data=Json.createReader(new StringReader(json)).read().asJsonObject();
		if(data.getString("password")!=null && data.getString("password").length()>=8 && data.getString("code")!=null) {
			User user=NewPassword.use(data.getString("code"));
			user.setPassword(data.getString("password"));
			req.getSession(true).setAttribute("sessionId",new Session(user).getId());
			return Response.ok(new HashMap<String,String>()).build();
		} else {
			Map<String,String> response=new HashMap<>();
			response.put("error","ongeldige waardes");
			return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
		}
	}
	@GET
	@Path("/")
	public Response status(@Context HttpServletRequest req) {
		User user;
		try {
			user = Utils.getUser(req);
		} catch (UnauthorizedException e) {
			Map<String,String> response=new HashMap<>();
			return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
		}
		return Response.ok(user).build();
	}
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(@Context HttpServletRequest req, String json) {
		JsonObject data=Json.createReader(new StringReader(json)).read().asJsonObject();
		User user;
		try {
			user = User.getUserByEmail(data.getString("email"));
		} catch (UnauthorizedException e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		if(user.checkPassword(data.getString("password"))) {
			req.getSession(true).setAttribute("sessionId",new Session(user).getId());
			return Response.ok(new HashMap<String,String>()).build();
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
	@GET
	@Path("/settings")
	public Response getSettings(@Context HttpServletRequest req) {
		User user;
		try {
			user = Utils.getUser(req);
		} catch (UnauthorizedException e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		Map<String,String> response=new HashMap<>();
		response.put("email",user.getEmail());
		response.put("name",user.getName());
		return Response.ok(response).build();
	}
	@POST
	@Path("/settings")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setSettings(@Context HttpServletRequest req, String json) {
		User user;
		try {
			user = Utils.getUser(req);
		} catch (UnauthorizedException e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		JsonObject data=Json.createReader(new StringReader(json)).read().asJsonObject();
		if(!user.getEmail().equals(data.getString("email"))) user.setEmail(data.getString("email"));
		if(!user.getName().equals(data.getString("name"))) user.setName(data.getString("name"));
		return Response.ok(new HashMap<String,String>()).build();
	}
}

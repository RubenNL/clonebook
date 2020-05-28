package nl.rubend.ipass.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import nl.rubend.ipass.domain.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Key;
import java.util.AbstractMap;
import java.util.Calendar;
@Path("/login")
public class AuthenticationResource {
	final static public Key key= MacProvider.generateKey();
	private String createToken(String userId,String role) {
		Calendar expiration=Calendar.getInstance();
		expiration.add(Calendar.MINUTE,30);
		return Jwts.builder()
				.setSubject(userId)
				.setExpiration(expiration.getTime())
				.claim("role",role)
				.signWith(SignatureAlgorithm.HS512, key)
				.compact();
	}
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response login(@FormParam("email") String email, @FormParam("password") String password) {
		User user = User.getUserByEmail(email);
		if(user==null) return Response.status(Response.Status.UNAUTHORIZED).build();
		if(user.checkPassword(password)) {
			String token=createToken(user.getId(),"user");
			AbstractMap.SimpleEntry<String, String> JWT=new AbstractMap.SimpleEntry<>("JWT",token);
			return Response.ok(JWT).build();
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}

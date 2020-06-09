package nl.rubend.ipass.security;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import nl.rubend.ipass.domain.User;

import javax.crypto.SecretKey;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.Key;
import java.util.AbstractMap;
import java.util.Calendar;
@Path("/login")
public class AuthenticationResource {
	final static public Key key= getKey();
	final static private String filename="/home/pi/ipass/key.ser";
	private static SecretKey getKey() {
		try {
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			SecretKey key= (SecretKey) in.readObject();
			in.close();
			fileIn.close();
			return key;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			SecretKey key=MacProvider.generateKey();
			try {
				FileOutputStream fileout = new FileOutputStream(filename);
				ObjectOutputStream out = new ObjectOutputStream(fileout);
				out.writeObject(key);
				out.close();
				fileout.close();
			} catch(IOException f) {
				f.printStackTrace();
			}
			return key;
		}
	}
	private String createToken(User user,boolean ingelogdBlijven) {
		JwtBuilder jwts=Jwts.builder()
				.setSubject(user.getId())
				.claim("userKey",user.getKey())
				.signWith(SignatureAlgorithm.HS512, key)
				.claim("long",ingelogdBlijven);
		if(!ingelogdBlijven) {
			Calendar expiration=Calendar.getInstance();
			expiration.add(Calendar.MINUTE,30);
			jwts.setExpiration(expiration.getTime());
		}
		return jwts.compact();
	}
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response login(@FormParam("email") String email, @FormParam("password") String password, @FormParam("long") String ingelogdBlijvenString) {
		User user = User.getUserByEmail(email);
		boolean ingelogdBlijven=ingelogdBlijvenString!=null;
		if(user==null) return Response.status(Response.Status.UNAUTHORIZED).build();
		if(user.checkPassword(password)) {
			String token=createToken(user,ingelogdBlijven);
			AbstractMap.SimpleEntry<String, String> JWT=new AbstractMap.SimpleEntry<>("JWT",token);
			return Response.ok(JWT).build();
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}

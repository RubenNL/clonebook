package nl.rubend.ipass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import nl.rubend.ipass.domain.User;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter {


	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
		scheme="https";//Gebruik van Apache2 reverse proxy, converteert HTTPS naar HTTP naar localhost.
		MySecurityContext msc = new MySecurityContext(null,scheme);
		String authHeader=requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (authHeader !=null && authHeader.startsWith("Bearer ")) {
			String token=authHeader.substring("Bearer ".length()).trim();
			try {
				JwtParser parser= Jwts.parser().setSigningKey(AuthenticationResource.key);
				Claims claims=parser.parseClaimsJws(token).getBody();
				String user=claims.getSubject();
				msc=new MySecurityContext(User.getUserById(user),scheme);
			} catch (JwtException | IllegalArgumentException e) {
				System.out.println("Invalid JWT!");
			}
		}
		requestContext.setSecurityContext(msc);
	}
}

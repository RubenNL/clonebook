package nl.rubend.ipass.security;

import nl.rubend.ipass.domain.User;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class MySecurityContext implements SecurityContext {
	private User user;
	private String scheme;
	public MySecurityContext(User user, String scheme) {
		this.user=user;
		this.scheme=scheme;
	}
	@Override
	public Principal getUserPrincipal() {
		return this.user;
	}

	@Override
	public boolean isUserInRole(String role) {
		if(user.getRole()!=null) return role.equals(user.getRole());
		return false;
	}

	@Override
	public boolean isSecure() {
		return "https".equals(this.scheme);
	}

	@Override
	public String getAuthenticationScheme() {
		return SecurityContext.BASIC_AUTH;
	}
}

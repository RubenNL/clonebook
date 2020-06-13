package nl.rubend.clonebook.security;

import nl.rubend.clonebook.domain.User;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class MySecurityContext implements SecurityContext {
	private User user;
	private boolean secure;
	public MySecurityContext(User user, boolean secure) {
		this.user=user;
		this.secure=secure;
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
		return secure;
	}

	@Override
	public String getAuthenticationScheme() {
		return SecurityContext.BASIC_AUTH;
	}
}

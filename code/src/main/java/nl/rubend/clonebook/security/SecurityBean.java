package nl.rubend.clonebook.security;

import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class SecurityBean {
	@PathParam("userId") private String id;
	@Context SecurityContext securityContext;
	public User getSender() {
		return (User) securityContext.getUserPrincipal();
	}
	public User getRequested() {
		User user=User.getUserById(this.id);
		if(user==null) throw new ClonebookException(Response.Status.NOT_FOUND,"requested user niet gevonden!");
		return user;
	}
	public boolean isAllowed() {
		User sender=getSender();
		User requested=getRequested();
		if(requested==null) throw new NotFoundException();
		if(!requested.equals(sender)) return false;
		return true;
	}
	public SecurityBean checkLogin() {
		if(isAllowed()) return this;
		else throw new ForbiddenException("geen toegang.");
	}
	public User allowedUser() {
		return checkLogin().getRequested();
	}
}

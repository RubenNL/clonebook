/*package nl.rubend.clonebook.security;

import nl.rubend.clonebook.data.SpringUserRepository;
import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component("securityBean")
public class SecurityBean {

	private final SpringUserRepository repository;
	Principal principal;
	//@Context SecurityContext securityContext;

	public SecurityBean(SpringUserRepository repository) {
		this.repository = repository;
	}

	public User getSender() {
		return (User) principal;
	}
	public User getRequested() {
		User user=repository.getOne(this.id);
		if(user==null) throw new ClonebookException("requested user niet gevonden!");
		return user;
	}
	public boolean isAllowed() {
		User sender=getSender();
		User requested=getRequested();
		if(requested==null) throw new ClonebookException("clonebook not found!");
		if(!requested.equals(sender)) return false;
		return true;
	}
	public SecurityBean checkLogin() {
		if(isAllowed()) return this;
		else throw new ClonebookException("geen toegang.");
	}
	public User allowedUser() {
		return checkLogin().getRequested();
	}
}
*/
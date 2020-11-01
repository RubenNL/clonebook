package nl.rubend.clonebook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.rubend.clonebook.UUIDGenerator;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;
import nl.rubend.clonebook.utils.SendEmail;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.*;
import javax.validation.constraints.Email;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
@Entity(name="Users")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User implements UserDetails {
	private static SecureRandom random = new SecureRandom();
	@Id
	@GeneratedValue(generator= UUIDGenerator.generatorName)
	@GenericGenerator(name = UUIDGenerator.generatorName, strategy = "nl.rubend.clonebook.UUIDGenerator")
	private String id;
	@Email
	@JsonIgnore
	private String email;
	@JsonIgnore
	private String password;
	@OneToOne(cascade=CascadeType.PERSIST)
	@JsonIgnore
	private Page privatePage;
	@JsonIgnore
	private String role="user";
	@JsonIgnore
	private String userKey;
	@OneToMany(mappedBy="owner",cascade=CascadeType.PERSIST)
	@JsonIgnore
	private List<Page> ownPages=new ArrayList<>();
	@OneToMany(mappedBy="user")
	@JsonIgnore
	private List<PushReceiver> pushReceivers;
	@PrePersist
	private void createPage() {
		privatePage=new Page();
		privatePage.setOwner(this);
		ownPages.add(privatePage);
	}
	public String getPrivatePageId() {
		return this.privatePage.getId();
	}
	@JsonIgnore
	public String getKey() {
		return userKey;
	}
	public boolean verifyKey(String key) {
		return userKey.equals(key);
	}
	public void logoutAll() {
		this.userKey=UUID.randomUUID().toString();
		sendToUser("LOGOUT","Apparaat uitgelogd.");

		//PushReceiver.deleteByUser(this); //TODO deleteByUser
	}
	@JsonIgnore
	public String getRole() {return this.role;}
	/*public boolean checkPassword(String password) {
		if(this.salt==null || this.salt.equals("")) return false;
		return Objects.equals(hash(password, this.salt), this.hash);
	}*/
	public void setName(String name) {
		this.privatePage.setName(name);
	}
	public String getName() {return privatePage.getName();}
	@JsonIgnore
	public Media getProfilePicture() {
		return privatePage.getLogo();
	}
	public void sendToUser(String message) {
		sendToUser(null, message);
	}

	public void sendToUser(String action, String message) {
		sendToUser(action,"",message);
	}
	public void sendToUser(String action, Media media, String message) {
		for (PushReceiver receiver : pushReceivers) {
			receiver.sendNotification(action, media, message);
		}
	}
	public void sendToUser(String action, String image, String message) {
		for (PushReceiver receiver : pushReceivers) {
			receiver.sendNotification(action, image, message);
		}
	}
	@JsonIgnore
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<SimpleGrantedAuthority> authorities=new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_user"));
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}
	@JsonIgnore
	@Override
	public String getUsername() {
		return email;
	}
	@JsonIgnore
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	@JsonIgnore
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	@JsonIgnore
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	@JsonIgnore
	@Override
	public boolean isEnabled() {
		return true;
	}
}

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
public class User implements Principal {
	private static SecureRandom random = new SecureRandom();
	@Id
	@GeneratedValue(generator= UUIDGenerator.generatorName)
	@GenericGenerator(name = UUIDGenerator.generatorName, strategy = "nl.rubend.clonebook.UUIDGenerator")
	private String id;
	@Email
	private String email;
	private String hash;
	private String salt;
	@OneToOne
	private Page privatePage;
	private String role="user";
	private String userKey;
	@OneToMany(mappedBy="owner")
	private List<Page> ownPages;
	@OneToMany(mappedBy="user")
	private List<PushReceiver> pushReceivers;
	/*public User(String email) throws ClonebookException {
		this.userId=UUID.randomUUID().toString();
		this.userKey=UUID.randomUUID().toString();
		this.email=email;
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO user(ID,email,userKey) VALUES (?,?,?)");
			statement.setString(1, userId);
			statement.setString(2, email);
			statement.setString(3, userKey);
			statement.executeUpdate();
			Page page=new Page(this,"Nieuwe gebruiker");
			this.privatePageId=page.getId();
			statement = SqlInterface.prepareStatement("UPDATE user SET privatePageId=? WHERE ID=?");
			statement.setString(1, this.privatePageId);
			statement.setString(2, userId);
			statement.executeUpdate();
			page.addLid(this);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}*/
	public static String hash(String password, String saltString) {
		byte[] salt = Base64.getUrlDecoder().decode(saltString);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 512);
		try {
			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			Base64.Encoder enc = Base64.getEncoder();
			return enc.encodeToString(f.generateSecret(spec).getEncoded());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			System.out.println("Systeem ondersteund delen van hashing niet,kan niet worden uitgevoerd op dit apparaat.");
		}
		return null;
	}
	public void setPassword(String password)  {
		if (password == null) throw new IllegalArgumentException("Wachtwoord is te kort!");
		if (password.length() < 8) throw new IllegalArgumentException("Wachtwoord is te kort!");
		byte[] salt = new byte[64];
		random.nextBytes(salt);
		Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
		this.salt = enc.encodeToString(salt);
		this.hash = hash(password, this.salt);
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
	public boolean checkPassword(String password) {
		if(this.salt==null || this.salt.equals("")) return false;
		return Objects.equals(hash(password, this.salt), this.hash);
	}
	public void sendPasswordForgottenUrl() {
		NewPassword newPassword=new NewPassword();
		newPassword.setUser(this);
		String url="https://clonebook.rubend.nl/#newAccount=" + newPassword.getCode();
		SendEmail.sendEmail(this.email,"CloneBook nieuw wachtwoord","<h1>Welkom bij Clonebook!</h1><br>Er is een nieuw account of nieuw wachtwoord aangevraagd op <a href=\"https://clonebook.rubend.nl\">Clonebook</a> voor uw e-mail adres.<br> Heeft u dat niet gedaan? dan kunt u deze e-mail gewoon negeren.<br>Gebruik <a href=\""+url+"\">Deze</a> url om je account te activeren.<br>Werkt de URL niet? Copieer/plak dan deze url in de browser: "+url+"<br>Deze url is 1 uur geldig.");
	}
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
}

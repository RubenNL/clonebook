package nl.rubend.ipass.domain;

import nl.rubend.ipass.utils.SqlInterface;
import nl.rubend.ipass.utils.SendEmail;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public class User {
	private static SecureRandom random = new SecureRandom();
	private int userId;
	private String name;
	private String email;
	private String hash;
	private String salt;
	public static User getUser(int userId) throws IpassException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM user WHERE ID=?");
			statement.setInt(1, userId);
			ResultSet set=statement.executeQuery();
			set.next();
			return new User(set.getInt("ID"),set.getString("name"),set.getString("email"),set.getString("hash"),set.getString("salt"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public static User getUser(String email) throws UnauthorizedException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM user WHERE email=?");
			statement.setString(1, email);
			ResultSet set=statement.executeQuery();
			set.next();
			return new User(set.getInt("ID"),set.getString("name"),set.getString("email"),set.getString("hash"),set.getString("salt"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UnauthorizedException(e.getMessage());
		}
	}
	public User(String email) throws IpassException {
		this.userId=ThreadLocalRandom.current().nextInt(0, 1000000000);
		this.email=email;
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO user(ID,email,name) VALUES (?,?,?)");
			statement.setInt(1, userId);
			statement.setString(2, email);
			statement.setString(3,"Nieuwe gebruiker");
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
		this.sendPasswordForgottenUrl();
	}
	public User(int userId,String name,String email,String hash,String salt) {
		this.userId=userId;
		this.name=name;
		this.email=email;
		this.hash=hash;
		this.salt=salt;
	}
	public static String hash(String password, String saltString) {
		byte[] salt = Base64.getUrlDecoder().decode(saltString);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
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
		if (password == null) throw new IpassException("Ongeldig wachtwoord");
		if (password.length() < 8) throw new IpassException("Wachtwoord is te kort!");
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
		this.salt = enc.encodeToString(salt);
		this.hash = hash(password, this.salt);
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE user SET salt = ?,hash = ?  WHERE ID = ?");
			statement.setString(1, this.salt);
			statement.setString(2, this.hash);
			statement.setInt(3, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public boolean checkPassword(String password) {
		return hash(password,this.salt).equals(this.hash);
	}
	public void sendPasswordForgottenUrl() {
		SendEmail.SendEmail(this.email,"CloneBook nieuw wachtwoord","Gebruik <a href=\"https://clonebook.rubend.nl/#newAccount=" + new NewPassword(this).getCode() + "\">Deze</a> url om je account te activeren.");
	}
	public int getId() {return this.userId;}
	public String getName() {return this.name;}
}

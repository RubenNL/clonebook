package nl.rubend.ipass.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import nl.rubend.ipass.utils.SqlInterface;
import nl.rubend.ipass.utils.SendEmail;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class User implements Principal {
	private static SecureRandom random = new SecureRandom();
	private String userId;
	private String name;
	private String email;
	private String hash;
	private String salt;
	private String privatePageId;
	private String role="user";
	public static User getUserById(String userId) throws IpassException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM user WHERE ID=?");
			statement.setString(1, userId);
			ResultSet set=statement.executeQuery();
			set.next();
			return new User(set.getString("ID"),set.getString("name"),set.getString("email"),set.getString("hash"),set.getString("salt"),set.getString("privatePageId"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public static User getUserByEmail(String email) throws UnauthorizedException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM user WHERE email=?");
			statement.setString(1, email);
			ResultSet set=statement.executeQuery();
			set.next();
			return new User(set.getString("ID"),set.getString("name"),set.getString("email"),set.getString("hash"),set.getString("salt"),set.getString("privatePageId"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UnauthorizedException(e.getMessage());
		}
	}
	public User(String email) throws IpassException {
		this.userId=UUID.randomUUID().toString();
		this.email=email;
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO user(ID,email,name) VALUES (?,?,?)");
			statement.setString(1, userId);
			statement.setString(2, email);
			statement.setString(3,"Nieuwe gebruiker");
			statement.executeUpdate();
			Page page=new Page(this);
			statement = SqlInterface.prepareStatement("UPDATE user SET privatePageId=? WHERE ID=?");
			statement.setString(1, page.getId());
			statement.setString(2, userId);
			statement.executeUpdate();
			page.addLid(this);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public User(String userId,String name,String email,String hash,String salt,String privatePageId) {
		this.userId=userId;
		this.name=name;
		this.email=email;
		this.hash=hash;
		this.salt=salt;
		this.privatePageId=privatePageId;
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
			statement.setString(3, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	@JsonIgnore
	public String getRole() {return this.role;}
	public String getPrivatePageId() {return this.privatePageId;}
	public void delete() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM user WHERE ID = ?");
			statement.setString(1, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public boolean checkPassword(String password) {
		if(this.salt==null || this.salt.equals("")) return false;
		return hash(password,this.salt).equals(this.hash);
	}
	public void sendPasswordForgottenUrl() {
		SendEmail.SendEmail(this.email,"CloneBook nieuw wachtwoord","Gebruik <a href=\"https://clonebook.rubend.nl/#newAccount=" + new NewPassword(this).getCode() + "\">Deze</a> url om je account te activeren.");
	}
	public void setName(String name) {
		this.name = name;
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE user SET name = ? WHERE ID = ?");
			statement.setString(1, this.name);
			statement.setString(2, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public void setEmail(String email) {
		this.email = email;
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE user SET email = ? WHERE ID = ?");
			statement.setString(1, this.email);
			statement.setString(2, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public String getId() {return this.userId;}
	public String getName() {return this.name;}
	@JsonIgnore
	public String getEmail() {return this.email;}
	@JsonIgnore
	public ArrayList<Page> getPages() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM pageLid WHERE userID=?");
			statement.setString(1,userId);
			ResultSet set=statement.executeQuery();
			ArrayList<Page> response=new ArrayList<Page>();
			while(set.next()) {
				response.add(Page.getPage(set.getString("pageID")));
			}
			return response;
		} catch (SQLException | NotFoundException e) {
			throw new IpassException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<Post> getPosts() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM post WHERE userID=?");
			statement.setString(1, userId);
			ResultSet set = statement.executeQuery();
			ArrayList<Post> response = new ArrayList<Post>();
			while (set.next()) {
				response.add(new Post(set.getString("ID"), set.getString("userID"), set.getString("pageID"),set.getString("repliedTo"),set.getString("text"),set.getTimestamp("date")));
			}
			return response;
		} catch (SQLException | NotFoundException e) {
			throw new IpassException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<Vote> getVotes() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM vote WHERE userID=?");
			statement.setString(1, userId);
			ResultSet set = statement.executeQuery();
			ArrayList<Vote> response = new ArrayList<Vote>();
			while (set.next()) {
				response.add(new Vote(set.getString("userID"), set.getString("postID"),set.getInt("vote")));
			}
			return response;
		} catch (SQLException | NotFoundException e) {
			throw new IpassException(e.getMessage());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return userId.equals(user.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}
}

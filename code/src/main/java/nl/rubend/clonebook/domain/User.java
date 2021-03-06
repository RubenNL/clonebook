package nl.rubend.clonebook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;
import nl.rubend.clonebook.utils.SendEmail;

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
import java.util.*;

public class User implements Principal {
	private static SecureRandom random = new SecureRandom();
	private String userId;
	private String email;
	private String hash;
	private String salt;
	private String privatePageId;
	private String role="user";
	private String userKey;
	public static User getUserById(String userId) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM user WHERE ID=?");
			statement.setString(1, userId);
			ResultSet set=statement.executeQuery();
			set.next();
			return new User(set.getString("ID"),set.getString("email"),set.getString("hash"),set.getString("salt"),set.getString("privatePageId"),set.getString("userKey"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static User getUserByEmail(String email) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM user WHERE email=?");
			statement.setString(1, email);
			ResultSet set=statement.executeQuery();
			set.next();
			return new User(set.getString("ID"),set.getString("email"),set.getString("hash"),set.getString("salt"),set.getString("privatePageId"),set.getString("userKey"));
		} catch (SQLException e) {
			return null;
		}
	}
	public User(String email) throws ClonebookException {
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
	}
	public User(String userId,String email,String hash,String salt,String privatePageId,String userKey) {
		this.userId=userId;
		this.email=email;
		this.hash=hash;
		this.salt=salt;
		this.privatePageId=privatePageId;
		this.userKey=userKey;
	}
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
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE user SET salt = ?,hash = ?  WHERE ID = ?");
			statement.setString(1, this.salt);
			statement.setString(2, this.hash);
			statement.setString(3, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<Page> getOwnPages() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM page WHERE ownerId=?");
			statement.setString(1,userId);
			ResultSet set=statement.executeQuery();
			ArrayList<Page> response=new ArrayList<>();
			while(set.next()) {
				response.add(Page.getPage(set.getString("ID")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
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
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE user SET userKey = ?  WHERE ID = ?");
			statement.setString(1, this.userKey);
			statement.setString(2, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
		sendToUser("LOGOUT","Apparaat uitgelogd.");
		PushReceiver.deleteByUser(this);
	}
	@JsonIgnore
	public String getRole() {return this.role;}
	public String getPrivatePageId() {return this.privatePageId;}
	@JsonIgnore
	public Page getPrivatePage() {
		return Page.getPage(this.privatePageId);
	}
	public void delete() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM user WHERE ID = ?");
			statement.setString(1, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public boolean checkPassword(String password) {
		if(this.salt==null || this.salt.equals("")) return false;
		return Objects.equals(hash(password, this.salt), this.hash);
	}
	public void sendPasswordForgottenUrl() {
		String url="https://clonebook.rubend.nl/#newAccount=" + new NewPassword(this).getCode();
		SendEmail.sendEmail(this.email,"CloneBook nieuw wachtwoord","<h1>Welkom bij Clonebook!</h1><br>Er is een nieuw account of nieuw wachtwoord aangevraagd op <a href=\"https://clonebook.rubend.nl\">Clonebook</a> voor uw e-mail adres.<br> Heeft u dat niet gedaan? dan kunt u deze e-mail gewoon negeren.<br>Gebruik <a href=\""+url+"\">Deze</a> url om je account te activeren.<br>Werkt de URL niet? Copieer/plak dan deze url in de browser: "+url+"<br>Deze url is 1 uur geldig.");
	}
	public void setName(String name) {
		this.getPrivatePage().setName(name);
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
			throw new ClonebookException(e.getMessage());
		}
	}
	public String getId() {return this.userId;}
	public String getName() {return getPrivatePage().getName();}
	@JsonIgnore
	public String getEmail() {return this.email;}
	@JsonIgnore
	public ArrayList<Page> getPages() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM pageLid WHERE userID=? AND blocked=false AND accepted=true");
			statement.setString(1,userId);
			ResultSet set=statement.executeQuery();
			ArrayList<Page> response=new ArrayList<>();
			while(set.next()) {
				response.add(Page.getPage(set.getString("pageID")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<Post> getPosts() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM post WHERE userID=?");
			statement.setString(1, userId);
			ResultSet set = statement.executeQuery();
			ArrayList<Post> response = new ArrayList<>();
			while (set.next()) {
				response.add(new Post(set.getString("ID"), set.getString("userID"), set.getString("pageID"),set.getString("repliedTo"),set.getString("text"),set.getTimestamp("date")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<Vote> getVotes() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM vote WHERE userID=?");
			statement.setString(1, userId);
			ResultSet set = statement.executeQuery();
			ArrayList<Vote> response = new ArrayList<>();
			while (set.next()) {
				response.add(new Vote(set.getString("userID"), set.getString("postID"),set.getInt("vote")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonIgnore
	public Media getProfilePicture() {
		return getPrivatePage().getLogo();
	}
	@JsonProperty("profilePicture")
	public String getProfilePictureId() {
		return getPrivatePage().getLogoId();
	}
	@JsonIgnore
	public ArrayList<HashMap<String,String>> getLidAanvragenOpPaginas() {
		ArrayList<HashMap<String,String>> response=new ArrayList<>();
		for(Page page:getPages()) {
			ArrayList<String> fromPage=page.getLidAanvragen();
			for(String userId:fromPage) {
				HashMap<String,String> tempHash=new HashMap<>();
				User user=User.getUserById(userId);
				tempHash.put("userId",userId);
				tempHash.put("userName",user.getName());
				tempHash.put("pageId",page.getId());
				tempHash.put("pageName",page.getName());
				Media picture=user.getProfilePicture();
				if(picture!=null) tempHash.put("picture",picture.getId());
				else tempHash.put("picture","");
				response.add(tempHash);
			}
		}
		return response;
	}
	private ArrayList<PushReceiver> getReceivers() {
		try {
			ArrayList<PushReceiver> response = new ArrayList<>();
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM pushReceiver WHERE userId=?");
			statement.setString(1, this.userId);
			ResultSet set = statement.executeQuery();
			while (set.next()) {
				response.add(new PushReceiver(set.getString("endpoint"), set.getString("key"), set.getString("auth"), set.getString("userID")));
			}
			return response;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public void sendToUser(String message) {
		sendToUser(null, message);
	}

	public void sendToUser(String action, String message) {
		sendToUser(action,"",message);
	}
	public void sendToUser(String action, Media media, String message) {
		for (PushReceiver receiver : getReceivers()) {
			receiver.sendNotification(action, media, message);
		}
	}
	public void sendToUser(String action, String image, String message) {
		for (PushReceiver receiver : getReceivers()) {
			receiver.sendNotification(action, image, message);
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

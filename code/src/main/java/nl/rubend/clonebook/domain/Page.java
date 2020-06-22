package nl.rubend.clonebook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class Page {
	private String id;
	private String ownerId;
	private String name;
	private String logo;
	public static Page getPage(String id) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM page WHERE ID=?");
			statement.setString(1, id);
			ResultSet set=statement.executeQuery();
			set.next();
			return new Page(set.getString("ownerId"),set.getString("name"),set.getString("ID"),set.getString("logo"));
		} catch (SQLException e) {
			return null;
		}
	}
	private Page(String ownerId,String name, String id,String logo) {
		this.id=id;
		this.ownerId=ownerId;
		this.name=name;
		this.logo=logo;
	}
	private Page(User owner,String name,String id,String logo) {
		this(owner.getId(),name,id,logo);
	}
	public Page(User owner,String name,String logo) {
		this(owner,name,UUID.randomUUID().toString(),logo);
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO page(ID,name,ownerId) VALUES (?,?,?)");
			statement.setString(1, this.id);
			statement.setString(2,this.name);
			statement.setString(3, this.ownerId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public Page(User owner,String naam) {
		this(owner,naam,null);
	}
	public Page(User owner) {
		this(owner,"Nieuwe pagina",null);
	}
	public void setName(String name) {
		this.name=name;
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE page SET name = ? WHERE ID = ?");
			statement.setString(1, this.name);
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public void setLogo(Media media) {
		try {
			media.cropSquare();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
		this.logo=media.getId();
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE page SET logo = ? WHERE ID = ?");
			statement.setString(1, this.logo);
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonProperty("logo")
	public String getLogoId() {
		return this.logo;
	}
	@JsonIgnore
	public Media getLogo() {
		if(this.logo==null) return null;
		return Media.getMedia(this.logo);
	}
	public void delete() {
		if(getOwner().getPrivatePage().equals(this)) throw new ClonebookException(Response.Status.BAD_REQUEST,"Kan niet prive pagina verwijderen!");
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM page WHERE ID = ?");
			statement.setString(1, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public void transferOwner(User newOwner) {
		this.ownerId=newOwner.getId();
		if(!this.isLid(newOwner)) this.addLid(newOwner);
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE page SET ownerId = ? WHERE ID = ?");
			statement.setString(1, this.ownerId);
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public String getId() {
		return this.id;
	}
	public void addLid(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO pageLid(userID,pageID,blocked,accepted) VALUES (?,?,false,true)");
			statement.setString(1,user.getId());
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public void blockUnblockLid(User user,boolean blocked) {
		if(user.equals(getOwner())) throw new ClonebookException(Response.Status.BAD_REQUEST,"kan admin niet blocken/unblocken!");
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE pageLid SET blocked = ? WHERE userID=? AND pageID=?");
			statement.setBoolean(1,blocked);
			statement.setString(2, user.getId());
			statement.setString(3, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public void removeLid(User user) {
		if(user.equals(getOwner())) throw new ClonebookException(Response.Status.BAD_REQUEST,"kan admin niet verwijderen!");
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM pageLid WHERE userID=? AND pageID=?");
			statement.setString(1, user.getId());
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public boolean isBlocked(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("select COUNT(userID) as count from pageLid where pageID=? and userID=? AND blocked=true");
			statement.setString(1, this.id);
			statement.setString(2,user.getId());
			ResultSet set=statement.executeQuery();
			set.next();
			return set.getInt("count")==1;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public boolean isLid(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("select COUNT(userID) as count from pageLid where pageID=? and userID=? AND blocked=false AND accepted=true");
			statement.setString(1, this.id);
			statement.setString(2,user.getId());
			ResultSet set=statement.executeQuery();
			set.next();
			return set.getInt("count")==1;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonIgnore
	public void addLidAanvraag(User user) {
		if(isLid(user)) throw new ClonebookException("user is al lid!");
		if(hasLidAanvraagVanUser(user)) throw new ClonebookException("aanvraag is al verstuurd!");
		if(isBlocked(user)) throw new ClonebookException("geblokkeerd!");
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO pageLid(userID,pageID,blocked,accepted) VALUES (?,?,false,false)");
			statement.setString(1, user.getId());
			statement.setString(2,this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
		this.getOwner().sendToUser("/#notifications",user.getProfilePicture(),user.getName()+" wilt lid worden van "+this.getName());
	}
	@JsonIgnore
	public boolean hasLidAanvraagVanUser(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("select COUNT(userID) as count from pageLid where pageID=? and userID=? AND accepted=false");
			statement.setString(1, this.id);
			statement.setString(2,user.getId());
			ResultSet set=statement.executeQuery();
			set.next();
			return set.getInt("count")==1;
		} catch(SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<User> getBlocked() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT userID FROM pageLid WHERE pageID=? AND blocked=true");
			statement.setString(1,id);
			ResultSet set=statement.executeQuery();
			ArrayList<User> response=new ArrayList<>();
			while(set.next()) {
				response.add(User.getUserById(set.getString("userID")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<String> getLidAanvragen() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT userID FROM pageLid WHERE pageID=? AND accepted=false AND blocked=false");
			statement.setString(1,id);
			ResultSet set=statement.executeQuery();
			ArrayList<String> response=new ArrayList<>();
			while(set.next()) {
				response.add(set.getString("userID"));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	public void acceptUser(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE pageLid SET accepted = true,blocked=false WHERE userID=? AND pageID=?");
			statement.setString(1, user.getId());
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<User> getLeden() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT userID FROM pageLid WHERE pageID=? AND blocked=false AND accepted=true");
			statement.setString(1,id);
			ResultSet set=statement.executeQuery();
			ArrayList<User> response=new ArrayList<>();
			while(set.next()) {
				response.add(User.getUserById(set.getString("userID")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<Post> getPosts() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT ID FROM post WHERE repliedTo IS NULL AND pageID=? ORDER BY date DESC");
			statement.setString(1,id);
			ResultSet set=statement.executeQuery();
			ArrayList<Post> response=new ArrayList<>();
			while(set.next()) {
				response.add(Post.getPost(set.getString("ID")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	public ArrayList<Post> getPostsLimit(Date date, int amount) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT ID FROM post WHERE repliedTo IS NULL AND pageID=? AND date < ? ORDER BY date DESC LIMIT ?");
			statement.setString(1,id);
			statement.setTimestamp(2,new Timestamp(date.getTime()));
			statement.setInt(3,amount);
			ResultSet set=statement.executeQuery();
			ArrayList<Post> response=new ArrayList<>();
			while(set.next()) {
				response.add(Post.getPost(set.getString("ID")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	public ArrayList<Post> getLast10Posts() {
		return getPostsLimit(new Date(Long.parseLong("253402128000000")),10);
	}
	public String getName() {
		return this.name;
	}
	@JsonIgnore
	public String getOwnerId() {
		return this.ownerId;
	}
	public User getOwner() {
		return User.getUserById(getOwnerId());
	}
	public void sendNotificationToAll(String message) {
		sendNotificationToAll(null,message);
	}
	public void sendNotificationToAll(String action,String message) {
		sendNotificationToAll(action,"",message);
	}
	public void sendNotificationToAll(String action, Media media,String message) {
		for(User user:getLeden()) user.sendToUser(action,media,message);
	}
	public void sendNotificationToAll(String action, String image,String message) {
		for(User user:getLeden()) user.sendToUser(action,image,message);
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Page)) return false;
		Page page = (Page) o;
		return getId().equals(page.getId());
	}
	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
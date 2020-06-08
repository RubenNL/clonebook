package nl.rubend.ipass.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.rubend.ipass.exceptions.IpassException;
import nl.rubend.ipass.utils.SqlInterface;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

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
			throw new IpassException(e.getMessage());
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
			throw new IpassException(e.getMessage());
		}
	}
	public void setLogo(Media media) {
		this.logo=media.getId();
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE page SET logo = ? WHERE ID = ?");
			statement.setString(1, this.logo);
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
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
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM page WHERE ID = ?");
			statement.setString(1, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
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
			throw new IpassException(e.getMessage());
		}
	}
	public String getId() {
		return this.id;
	}
	public void addLid(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO pageLid(userID,pageID) VALUES (?,?)");
			statement.setString(1,user.getId());
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public void removeLid(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM pageLid WHERE userID=? AND pageID=?");
			statement.setString(1, user.getId());
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public boolean isLid(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("select COUNT(userID) as count from pageLid where pageID=? and userID=?");
			statement.setString(1, this.id);
			statement.setString(2,user.getId());
			ResultSet set=statement.executeQuery();
			set.next();
			return set.getInt("count")==1;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	@JsonIgnore
	public void addLidAanvraag(User user) {
		if(isLid(user)) throw new IllegalArgumentException("user is al lid!");
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO lidAanvraag(userID,pageID) VALUES (?,?)");
			statement.setString(1, user.getId());
			statement.setString(2,this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	@JsonIgnore
	public boolean hasLidAanvraagVanUser(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("select COUNT(userID) as count from lidAanvraag where pageID=? and userID=?");
			statement.setString(1, this.id);
			statement.setString(2,user.getId());
			ResultSet set=statement.executeQuery();
			set.next();
			return set.getInt("count")==1;
		} catch(SQLException e) {
			throw new IpassException(e.getMessage());
		}
	}
	@JsonIgnore
	public ArrayList<String> getLidAanvragen() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT userID FROM lidAanvraag WHERE pageID=?");
			statement.setString(1,id);
			ResultSet set=statement.executeQuery();
			ArrayList<String> response=new ArrayList<>();
			while(set.next()) {
				response.add(set.getString("userID"));
			}
			return response;
		} catch (SQLException e) {
			throw new IpassException(e.getMessage());
		}
	}
	public void removeLidAanvraag(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM lidAanvraag WHERE userID=? AND pageID=?");
			statement.setString(1, user.getId());
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public void acceptUser(User user) {
		if(hasLidAanvraagVanUser(user)) {
			addLid(user);
			removeLidAanvraag(user);
		} else throw new IllegalArgumentException("gebruiker heeft geen aanvraag");
	}
	@JsonIgnore
	public ArrayList<User> getLeden() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT userID FROM pageLid WHERE pageID=?");
			statement.setString(1,id);
			ResultSet set=statement.executeQuery();
			ArrayList<User> response=new ArrayList<>();
			while(set.next()) {
				response.add(User.getUserById(set.getString("userID")));
			}
			return response;
		} catch (SQLException e) {
			throw new IpassException(e.getMessage());
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
			throw new IpassException(e.getMessage());
		}
	}
	public ArrayList<Post> getPostsLimit(int start,int amount) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT ID FROM post WHERE repliedTo IS NULL AND pageID=? ORDER BY date DESC LIMIT ?,?");
			statement.setString(1,id);
			statement.setInt(2,start);
			statement.setInt(3,amount);
			ResultSet set=statement.executeQuery();
			ArrayList<Post> response=new ArrayList<>();
			while(set.next()) {
				response.add(Post.getPost(set.getString("ID")));
			}
			return response;
		} catch (SQLException e) {
			throw new IpassException(e.getMessage());
		}
	}
	public ArrayList<Post> getLast10Posts() {
		return getPostsLimit(0,10);
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
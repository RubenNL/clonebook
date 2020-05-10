package nl.rubend.ipass.domain;

import nl.rubend.ipass.utils.SqlInterface;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Page {
	private String id;
	private String ownerId;
	private String name;
	public static Page getPage(String id) throws NotFoundException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM page WHERE ID=?");
			statement.setString(1, id);
			ResultSet set=statement.executeQuery();
			set.next();
			return new Page(set.getString("userId"),set.getString("name"),set.getString("ID"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new NotFoundException("Sessie niet meer geldig");
		}
	}
	private Page(String ownerId,String name, String id) {
		this.id=id;
		this.ownerId=ownerId;
		this.name=name;
	}
	private Page(User owner,String name,String id) {
		this(owner.getId(),name,id);
	}
	public Page(User owner,String name) {
		this(owner,name,UUID.randomUUID().toString());
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
	public Page(User owner) {
		this(owner,"Nieuwe pagina");
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
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO page_lid(userID,pageID) VALUES (?,?)");
			statement.setString(1,user.getId());
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public ArrayList<User> getLeden() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM page_lid WHERE pageID=?");
			statement.setString(1,id);
			ResultSet set=statement.executeQuery();
			ArrayList<User> response=new ArrayList<User>();
			while(set.next()) {
				response.add(User.getUserById(set.getString("userID")));
			}
			return response;
		} catch (SQLException e) {
			throw new IpassException(e.getMessage());
		}
	}
	public ArrayList<Post> getPosts() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM post WHERE pageID=?");
			statement.setString(1,id);
			ResultSet set=statement.executeQuery();
			ArrayList<Post> response=new ArrayList<Post>();
			while(set.next()) {
				response.add(Post.getPost(set.getString("postID")));
			}
			return response;
		} catch (SQLException | NotFoundException e) {
			throw new IpassException(e.getMessage());
		}
	}
}

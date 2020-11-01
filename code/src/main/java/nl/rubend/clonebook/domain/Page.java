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
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Page {
	@Id
	@GeneratedValue(generator= UUIDGenerator.generatorName)
	@GenericGenerator(name = UUIDGenerator.generatorName, strategy = "nl.rubend.clonebook.UUIDGenerator")
	private String id;
	@ManyToOne
	private User owner;
	private String name="Nieuwe pagina";
	@OneToOne
	private Media logo;
	@ManyToMany
	private List<User> leden;
	@OneToMany(mappedBy="page")
	private List<Post> posts;
	public void setLogo(Media media) {
		try {
			media.cropSquare();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
		this.logo=media;
	}
	public void delete() {
		if(getOwner().getPrivatePage().equals(this)) throw new ClonebookException("BAD_REQUEST","Kan niet prive pagina verwijderen!");
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
		this.owner=newOwner;
		if(!this.isLid(newOwner)) this.addLid(newOwner);
	}
	public void addLid(User user) {
		leden.add(user);
	}
	public void removeLid(User user) {
		if(user.equals(getOwner())) throw new ClonebookException("BAD_REQUEST","kan admin niet verwijderen!");
		leden.remove(user);
	}
	//TODO block unblock lid
	/*public void blockUnblockLid(User user,boolean blocked) {
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
	}*/
	public boolean isBlocked(User user) {
		return false;
	}
	public boolean isLid(User user) {
		return leden.contains(user);
	}
	@JsonIgnore
	public void addLidAanvraag(User user) {
		if(isLid(user)) throw new ClonebookException("user is al lid!");
		if(hasLidAanvraagVanUser(user)) throw new ClonebookException("aanvraag is al verstuurd!");
		if(isBlocked(user)) throw new ClonebookException("geblokkeerd!");
		//TODO lidaanvraag
		this.getOwner().sendToUser("/#notifications",user.getProfilePicture(),user.getName()+" wilt lid worden van "+this.getName());
	}
	@JsonIgnore
	public boolean hasLidAanvraagVanUser(User user) {
		return false;
		//TODO lidaanvraag
	}
	@JsonIgnore
	public List<User> getBlocked() {
		return new ArrayList<User>();
		//TODO blocked
	}
	@JsonIgnore
	public List<String> getLidAanvragen() {
		return new ArrayList<>();
		//TODO ?
	}
	public void acceptUser(User user) {
		//TODO acceptUser
	}
	public String getName() {
		return this.name;
	}
	public void sendNotificationToAll(String message) {
		sendNotificationToAll(null,message);
	}
	public void sendNotificationToAll(String action,String message) {
		sendNotificationToAll(action,"",message);
	}
	public void sendNotificationToAll(String action, Media media,String message) {
		for(User user:leden) user.sendToUser(action,media,message);
	}
	public void sendNotificationToAll(String action, String image,String message) {
		for(User user:leden) user.sendToUser(action,image,message);
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
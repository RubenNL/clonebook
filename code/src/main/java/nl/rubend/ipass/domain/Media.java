package nl.rubend.ipass.domain;

import nl.rubend.ipass.utils.SqlInterface;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Media {
	//TODO werkende save/load functies
	private String id;
	private String location;
	private String ownerId;
	public static Media getMedia(String id) throws NotFoundException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM media WHERE ID=?");
			statement.setString(1, id);
			ResultSet set=statement.executeQuery();
			set.next();
			return new Media(set.getString("ID"),set.getString("owner"),set.getString("location"));
		} catch (SQLException e) {
			throw new NotFoundException("Post niet gevonden");
		}
	}
	public Media(String id,String ownerId,String location) {
		this.id=id;
		this.ownerId=ownerId;
		this.location=location;
	}
	public Media(String ownerId,String location) {
		this(UUID.randomUUID().toString(),ownerId,location);
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO media(ID,owner,location) VALUES (?,?,?)");
			statement.setString(1, this.id);
			statement.setString(2,this.ownerId);
			statement.setString(3, this.location);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public String getId() {return this.id;}
	public User getOwner() {return User.getUserById(this.ownerId);}
}
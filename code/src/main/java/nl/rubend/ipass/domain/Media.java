package nl.rubend.ipass.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.rubend.ipass.utils.SqlInterface;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Media {
	private String id;
	private String location;
	private String ownerId;
	private String mime;
	private static File uploads = new File("/home/pi/ipassUploads");

	public static Media getMedia(String id) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM media WHERE ID=?");
			statement.setString(1, id);
			ResultSet set = statement.executeQuery();
			set.next();
			return new Media(set.getString("ID"), set.getString("owner"), set.getString("location"), set.getString("mime"));
		} catch (SQLException e) {
			return null;
		}
	}

	public Media(String id, String ownerId, String location, String mime) {
		this.id = id;
		this.ownerId = ownerId;
		this.location = location;
		this.mime = mime;
	}

	public Media(InputStream file, String ownerId, String location) {
		this(UUID.randomUUID().toString(), ownerId, location, "");
		File destination = new File(uploads, id);
		try {
			Files.copy(file, destination.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mime = new Tika().detect(destination.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO media(ID,owner,location,mime) VALUES (?,?,?,?)");
			statement.setString(1, this.id);
			statement.setString(2, this.ownerId);
			statement.setString(3, this.location);
			statement.setString(4, this.mime);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}

	@JsonIgnore
	public File getFile() {
		File file= new File(new File(uploads, location), id);
		if(!file.exists()) return null;
		return file;
	}

	public String getMime() {
		return this.mime;
	}

	public String getId() {
		return this.id;
	}

	public String getOwnerId() {
		return this.ownerId;
	}
	@JsonIgnore
	public User getOwner() {
		return User.getUserById(this.ownerId);
	}

	public void delete() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM media WHERE ID=?");
			statement.setString(1, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
		try {
			Files.delete(getFile().toPath());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public static void removeNotConnected() {
		try {
			ResultSet set=SqlInterface.executeQuery("SELECT ID FROM media WHERE ID NOT IN (SELECT mediaid FROM media_post");
			while(set.next()) {
				Media.getMedia(set.getString("ID")).delete();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
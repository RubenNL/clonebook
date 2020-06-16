package nl.rubend.clonebook.domain;

import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;

import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Chat {
	private String id;
	public static Chat getChat(String id) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM chat WHERE ID=?");
			statement.setString(1, id);
			ResultSet set = statement.executeQuery();
			set.next();
			return new Chat(set.getString("ID"));
		} catch (SQLException e) {
			throw new ClonebookException(Response.Status.NOT_FOUND,"Chat niet gevonden");
		}
	}
	private Chat(String id) {
		this.id=id;
	}
	private void addUser(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO user_chat(userID,chatID) VALUES (?,?)");
			statement.setString(1, user.getId());
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public Chat(User user1,User user2) {
		this.id=UUID.randomUUID().toString();
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO chat(ID) VALUES (?)");
			statement.setString(1, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
		addUser(user1);
		addUser(user2);
	}
	public ArrayList<User> getUsers() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT userID FROM user_chat WHERE chatID=?");
			statement.setString(1,this.id);
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
	public String getId() {
		return this.id;
	}
	public String getNameForUser(User current) {
		for (User found : getUsers()) if (current != found) return found.getName();
		throw new ClonebookException(Response.Status.NOT_FOUND, "geen andere user gevonden in chat.");
	}
	public ArrayList<ChatMessage> getMessagesBefore(Date date) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT ID,chatID,userID,message,date FROM chatMessage WHERE date > ? AND chatID=? ORDER BY date DESC limit 10");
			statement.setTimestamp(1, new Timestamp(date.getTime()));
			statement.setString(2,this.id);
			ResultSet set=statement.executeQuery();
			ArrayList<ChatMessage> response=new ArrayList<>();
			while(set.next()) {
				response.add(new ChatMessage(set.getString("ID"),set.getString("chatID"),set.getString("userID"),set.getTimestamp("date"),set.getString("message")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	public ArrayList<ChatMessage> getLastMessages() {
		return getMessagesBefore(new Date(9999,12,31));
	}
}

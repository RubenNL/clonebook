package nl.rubend.clonebook.domain;

import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class ChatMessage {
	private String id;
	private String chatId;
	private String userId;
	private String message;
	private Date date;
	public ChatMessage(String id,String chatId,String userId,Date date,String message) {
		this.id=id;
		this.chatId=chatId;
		this.userId=userId;
		this.message=message;
		this.date=date;
	}
	public ChatMessage(Chat chat,User user,String message) {
		this(UUID.randomUUID().toString(),chat.getId(),user.getId(),new Date(System.currentTimeMillis()),message);
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO chatMessage(ID,chatID,userID,message,date) VALUES (?,?,?,?,?)");
			statement.setString(1, this.id);
			statement.setString(2, this.chatId);
			statement.setString(3, this.userId);
			statement.setString(4, this.message);
			statement.setTimestamp(5, new Timestamp(this.date.getTime()));
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
}

package nl.rubend.ipass.domain;

import nl.rubend.ipass.utils.SqlInterface;

import java.sql.Timestamp;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class Post {
	private String id;
	private String userId;
	private String pageId;
	private String repliedToId;
	private String text;
	private Date date;
	public static Post getPost(String id) throws NotFoundException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM post WHERE ID=?");
			statement.setString(1, id);
			ResultSet set=statement.executeQuery();
			set.next();
			return new Post(set.getString("ID"),set.getString("userID"),set.getString("pageID"),set.getString("repliedTo"),set.getString("text"),(Date) set.getTimestamp("date"));
		} catch (SQLException e) {
			throw new NotFoundException("Post niet gevonden");
		}
	}
	public Post(String id,String userId,String pageId, String repliedToId,String text,Date date) {
		this.id=id;
		this.userId=userId;
		this.pageId=pageId;
		this.repliedToId=repliedToId;
		this.text=text;
		this.date=date;
	}
	public Post(String userId,String pageId, String repliedToId,String text) {
		this(UUID.randomUUID().toString(),userId,pageId,repliedToId,text,new Date(System.currentTimeMillis()));
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO post(ID,userID,pageID,repliedTo,text,date) VALUES (?,?,?,?,?,?)");
			statement.setString(1, this.id);
			statement.setString(2,this.userId);
			statement.setString(3, this.pageId);
			statement.setString(4, this.repliedToId);
			statement.setString(5, this.text);
			statement.setTimestamp(6, new Timestamp(this.date.getTime()));
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public Post(User user,Page page,Post repliedTo,String text) {
		this(user.getId(),page.getId(),repliedTo==null?null:repliedTo.getId(),text);
	}
	public String getId() {return this.id;}
	public String getText() {return this.text;}
	public Post getRepliedTo() throws NotFoundException {
		return getPost(this.repliedToId);
	}
	public ArrayList<Vote> getVotes() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM vote WHERE postID=?");
			statement.setString(1, id);
			ResultSet set = statement.executeQuery();
			ArrayList<Vote> response = new ArrayList<Vote>();
			while (set.next()) {
				response.add(new Vote(set.getString("userID"), set.getString("postID"), set.getInt("vote")));
			}
			return response;
		} catch (SQLException | NotFoundException e) {
			throw new IpassException(e.getMessage());
		}
	}
	public int getVoteTotal() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT SUM(vote) as sum FROM vote WHERE postID=?");
			statement.setString(1, id);
			ResultSet set = statement.executeQuery();
			ArrayList<Vote> response = new ArrayList<Vote>();
			set.next();
			return set.getInt("sum");
		} catch (SQLException | NotFoundException e) {
			throw new IpassException(e.getMessage());
		}
	}
}
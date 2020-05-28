package nl.rubend.ipass.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.rubend.ipass.utils.SqlInterface;

import java.sql.Timestamp;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class Post {
	private String id;
	private String userId;
	private String pageId;
	private String repliedToId;
	private String text;
	private Date date;
	public static Post getPost(String id) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM post WHERE ID=?");
			statement.setString(1, id);
			ResultSet set=statement.executeQuery();
			set.next();
			return new Post(set.getString("ID"),set.getString("userID"),set.getString("pageID"),set.getString("repliedTo"),set.getString("text"),(Date) set.getTimestamp("date"));
		} catch (SQLException e) {
			return null;
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
	public void delete() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM post WHERE ID = ?");
			statement.setString(1, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public void addFile(Media file) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO media_post(mediaid,postID) VALUES (?,?)");
			statement.setString(1, file.getId());
			statement.setString(2,this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public ArrayList<Media> getMedia() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT mediaid FROM media_post WHERE postID=?");
			statement.setString(1, id);
			ResultSet set = statement.executeQuery();
			ArrayList<Media> response = new ArrayList<Media>();
			while (set.next()) {
				response.add(Media.getMedia(set.getString("mediaid")));
			}
			return response;
		} catch (SQLException e) {
			throw new IpassException(e.getMessage());
		}
	}
	public String getId() {return this.id;}
	public String getText() {return this.text;}
	public String getRepliedTo() {
		return this.repliedToId;
	}
	@JsonIgnore
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
		} catch (SQLException e) {
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
		} catch (SQLException e) {
			throw new IpassException(e.getMessage());
		}
	}
	public String getPageId() {
		return this.pageId;
	}
	@JsonIgnore
	public Page getPage() {
		return Page.getPage(this.pageId);
	}
	public User getUser() {
		return User.getUserById(userId);
	}
	public ArrayList<Post> getChildren() {
		ArrayList<Post> response=new ArrayList<>();
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM post WHERE repliedTo=?");
			statement.setString(1, id);
			ResultSet set=statement.executeQuery();
			while(set.next()) {
				response.add(new Post(set.getString("ID"),set.getString("userID"),set.getString("pageID"),set.getString("repliedTo"),set.getString("text"),(Date) set.getTimestamp("date")));
			}
			return response;
		} catch (SQLException e) {
			throw new javax.ws.rs.InternalServerErrorException("get werkt niet?");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Post)) return false;
		Post post = (Post) o;
		return getId().equals(post.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
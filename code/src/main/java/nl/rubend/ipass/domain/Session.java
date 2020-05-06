package nl.rubend.ipass.domain;

import nl.rubend.ipass.utils.SqlInterface;

import java.sql.Timestamp;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class Session {
	private String id;
	private Date validUntil;
	private int userId;
	public static Session getSession(String id) throws UnauthorizedException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM session WHERE ID=?");
			statement.setString(1, id);
			ResultSet set=statement.executeQuery();
			set.next();
			return new Session(set.getString("ID"),(Date) set.getTimestamp("validUntil"),set.getInt("userID"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UnauthorizedException("Sessie niet meer geldig");
		}
	}
	public static ArrayList<Session> getSessions(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM session WHERE userID=?");
			statement.setInt(1,user.getId());
			ResultSet set=statement.executeQuery();
			ArrayList<Session> response=new ArrayList<Session>();
			while(set.next()) {
				response.add(new Session(set.getString("ID"),set.getDate("validUntil"),set.getInt("userID")));
			}
			return response;
		} catch (SQLException | UnauthorizedException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	private Session(String id, Date validUntil, int userId) throws UnauthorizedException {
		this.validUntil=validUntil;
		if(!isValid()) throw new UnauthorizedException("Sessie niet meer geldig");
		this.id=id;
		this.userId=userId;
	}
	public Session(Date validUntil,User user) {
		this.id=UUID.randomUUID().toString();
		this.validUntil=validUntil;
		this.userId=user.getId();
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO session(ID,validUntil,userID) VALUES (?,?,?)");
			statement.setString(1, this.id);
			statement.setTimestamp(2, new Timestamp(this.validUntil.getTime()));
			statement.setInt(3, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public Session(User user) {
		this(new Date(System.currentTimeMillis() + 3600 * 1000),user);//1 hour session
	}
	public boolean isValid() {
		return this.validUntil.after(new Date(System.currentTimeMillis()));
	}
	public String getId() {return this.id;}
	public Date getValidUntil() {return this.validUntil;}
	public User getUser() {return User.getUser(this.userId);}
	public void setValidUntil(Date validUntil) {
		this.validUntil=validUntil;
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE session SET validUntil = ? WHERE ID = ?");
			statement.setTimestamp(1, (Timestamp) validUntil);
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
}

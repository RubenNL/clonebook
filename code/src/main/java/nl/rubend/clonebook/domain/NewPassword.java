package nl.rubend.clonebook.domain;

import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;

import javax.ws.rs.core.Response;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NewPassword {
	private String uuid=UUID.randomUUID().toString();
	private User user;
	public NewPassword(User user) throws ClonebookException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO newPassword(validUntil,code,userID) VALUES(?,?,?)");
			statement.setDate(1, new Date(System.currentTimeMillis() + 3600 * 1000)); //1 hour in future
			statement.setString(2, uuid);
			statement.setString(3, user.getId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	private NewPassword(User user,String uuid) {
		this.user=user;
		this.uuid=uuid;
	};
	public String getCode() {
		return this.uuid;
	}
	public static NewPassword use(String code) throws ClonebookException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM newPassword WHERE code=?");
			statement.setString(1, code);
			ResultSet set = statement.executeQuery();
			try {
				set.next();
			} catch (SQLException e) {
				throw new ClonebookException(Response.Status.NOT_FOUND,"Code niet gevonden.");
			}
			User user;
			if (set.getDate("validUntil").before(new Date(System.currentTimeMillis())))
				user = User.getUserById(set.getString("userId"));
			else throw new ClonebookException(Response.Status.GONE,"Code is niet meer geldig.");
			return new NewPassword(user,code);
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	public User getUser() {
		return user;
	}
	public void delete() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM newPassword WHERE code=?");
			statement.setString(1, this.getCode());
			statement.executeUpdate();
		} catch(SQLException e) {
			throw new ClonebookException("Verwijderen mislukt");
		}
	}
}
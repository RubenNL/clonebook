package nl.rubend.ipass.domain;

import nl.rubend.ipass.utils.SqlInterface;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NewPassword {
	private String uuid=UUID.randomUUID().toString();
	public NewPassword(User user) throws IpassException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO newPassword(validUntil,code,userID) VALUES(?,?,?)");
			statement.setDate(1, new Date(System.currentTimeMillis() + 3600 * 1000)); //1 hour in future
			statement.setString(2, uuid);
			statement.setString(3, user.getId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public String getCode() {
		return this.uuid;
	}
	public static User use(String code) throws IpassException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM newPassword WHERE code=?");
			statement.setString(1, code);
			ResultSet set = statement.executeQuery();
			try {
				set.next();
			} catch (SQLException e) {
				throw new IpassException("Code niet gevonden.");
			}
			User user;
			if (set.getDate("validUntil").before(new Date(System.currentTimeMillis())))
				user = User.getUserById(set.getString("userId"));
			else throw new IpassException("Code is niet meer geldig.");
			statement = SqlInterface.prepareStatement("DELETE FROM newPassword WHERE code=?");
			statement.setString(1, set.getString("code"));
			statement.executeUpdate();
			return user;
		} catch (SQLException e) {
			throw new IpassException(e.getMessage());
		}
	}
}
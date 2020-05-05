package nl.rubend.ipass.domain;

import com.sun.mail.smtp.SMTPTransport;
import nl.rubend.ipass.SqlInterface;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class User {
	private static SecureRandom random = new SecureRandom();
	private int userId;
	private String name;
	private String email;
	private String hash;
	private String salt;
	public static User getUser(int userId) throws IpassException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM user WHERE ID=?");
			statement.setInt(1, userId);
			ResultSet set=statement.executeQuery();
			set.next();
			return new User(set.getInt("ID"),set.getString("name"),set.getString("email"),set.getString("hash"),set.getString("hash"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public static User getUser(String email) throws IpassException {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM user WHERE email=?");
			statement.setString(1, email);
			ResultSet set=statement.executeQuery();
			set.next();
			return new User(set.getInt("ID"),set.getString("name"),set.getString("email"),set.getString("hash"),set.getString("hash"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public User(String email) throws IpassException {
		this.userId=ThreadLocalRandom.current().nextInt(0, 1000000000);
		this.email=email;
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO user(ID,email) VALUES (?,?)");
			statement.setInt(1, userId);
			statement.setString(2, email);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
		this.sendPasswordForgottenUrl();
	}
	public User(int userId,String name,String email,String hash,String salt) {
		this.userId=userId;
		this.name=name;
		this.email=email;
		this.hash=hash;
		this.salt=salt;
	}
	public static String hash(String password, String saltString) {
		byte[] salt = Base64.getUrlDecoder().decode(saltString);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		try {
			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			Base64.Encoder enc = Base64.getEncoder();
			return enc.encodeToString(f.generateSecret(spec).getEncoded());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			System.out.println("Systeem ondersteund delen van hashing niet,kan niet worden uitgevoerd op dit apparaat.");
		}
		return null;
	}
	public void setPassword(String password)  {
		if (password != null) {
			if (password.length() >= 8) {
				byte[] salt = new byte[16];
				random.nextBytes(salt);
				Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
				this.salt = enc.encodeToString(salt);
				this.hash = hash(password, this.salt);
			} else {
				throw new IllegalArgumentException("Wachtwoord is te kort!");
			}
		} else throw new IllegalArgumentException("Ongeldige waarde");
	}
	public boolean checkPassword(String password) {
		return hash(password,this.salt).equals(this.hash);
	}
	public int getId() {return this.userId;}
	public void sendPasswordForgottenUrl() throws IpassException {
		try {
			Properties prop=new Properties();
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties"));
			Properties props = System.getProperties();
			props.put("mail.smtps.host",prop.getProperty("smtphost"));
			props.put("mail.smtps.auth","true");
			Session session = Session.getInstance(props, null);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("clonebook@rubend.nl"));;
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.email, false));
			msg.setSubject("CloneBook nieuw wachtwoord");
			msg.setContent("Gebruik <a href=\"https://clonebook.rubend.nl/#newAccount?code="+new NewPassword(this).getCode()+"\">Deze</a> url om je account te activeren.", "text/html");
			msg.setSentDate(new Date(System.currentTimeMillis()));
			SMTPTransport t = (SMTPTransport)session.getTransport("smtps");
			t.connect(prop.getProperty("smtphost"), prop.getProperty("smtpuser"), prop.getProperty("smtppass"));
			t.sendMessage(msg, msg.getAllRecipients());
			t.close();
		} catch (MessagingException | IOException e) {
			e.printStackTrace();
			throw new IpassException("failed to send email!");
		}
	}
}

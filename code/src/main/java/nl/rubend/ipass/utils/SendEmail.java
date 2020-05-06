package nl.rubend.ipass.utils;

import com.sun.mail.smtp.SMTPTransport;
import nl.rubend.ipass.domain.IpassException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.sql.Date;
import java.util.Properties;

public class SendEmail {
	public static void SendEmail(String to, String subject,String message) {
		try {
			Properties prop = new Properties();
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties"));
			Properties props = System.getProperties();
			props.put("mail.smtps.host", prop.getProperty("smtphost"));
			props.put("mail.smtps.auth", "true");
			Session session = Session.getInstance(props, null);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("clonebook@rubend.nl"));
			;
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			msg.setSubject(subject);
			msg.setContent(message, "text/html");
			msg.setSentDate(new Date(System.currentTimeMillis()));
			SMTPTransport t = (SMTPTransport) session.getTransport("smtps");
			t.connect(prop.getProperty("smtphost"), prop.getProperty("smtpuser"), prop.getProperty("smtppass"));
			t.sendMessage(msg, msg.getAllRecipients());
			t.close();
		} catch (MessagingException | IOException e) {
			e.printStackTrace();
			throw new IpassException("failed to send email!");
		}
	}
}
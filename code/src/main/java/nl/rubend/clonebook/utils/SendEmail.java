package nl.rubend.clonebook.utils;

import com.sun.mail.smtp.SMTPTransport;
import nl.rubend.clonebook.data.SpringNewPasswordRepository;
import nl.rubend.clonebook.domain.NewPassword;
import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Date;
import java.util.Properties;

@Component
public class SendEmail {
	@Value("${smtphost}")
	private String smtpHost;
	@Value("${smtpuser}")
	private String smtpUser;
	@Value("${smtppass}")
	private String smtpPass;
	private static SendEmail item;
	private final SpringNewPasswordRepository newPasswordRepository;
	public SendEmail(SpringNewPasswordRepository newPasswordRepository) {
		this.newPasswordRepository = newPasswordRepository;
		item=this;
	}
	public static void sendEmail(String to, String subject,String message) {
		item.sendEmailTo(to,subject,message);
	}
	private void sendEmailTo(String to, String subject,String message) {
		try {
			Properties props = System.getProperties();
			props.put("mail.smtps.host", smtpHost);
			props.put("mail.smtps.auth", "true");
			Session session = Session.getInstance(props, null);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("clonebook@rubend.nl"));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			msg.setSubject(subject);
			msg.setContent(message, "text/html");
			msg.setSentDate(new Date(System.currentTimeMillis()));
			SMTPTransport t = (SMTPTransport) session.getTransport("smtps");
			t.connect(smtpHost, smtpUser, smtpPass);
			t.sendMessage(msg, msg.getAllRecipients());
			t.close();
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new ClonebookException("failed to send email!");
		}
	}
	public void sendPasswordForgottenUrl(User user) {
		NewPassword newPassword=new NewPassword();
		newPassword.setUser(user);
		newPassword=newPasswordRepository.save(newPassword);
		String url="https://clonebook.rubend.nl/#newAccount=" + newPassword.getCode();
		sendEmail(user.getEmail(),"CloneBook nieuw wachtwoord","<h1>Welkom bij Clonebook!</h1><br>Er is een nieuw account of nieuw wachtwoord aangevraagd op <a href=\"https://clonebook.rubend.nl\">Clonebook</a> voor uw e-mail adres.<br> Heeft u dat niet gedaan? dan kunt u deze e-mail gewoon negeren.<br>Gebruik <a href=\""+url+"\">Deze</a> url om je account te activeren.<br>Werkt de URL niet? Copieer/plak dan deze url in de browser: "+url+"<br>Deze url is 1 uur geldig.");
	}
}
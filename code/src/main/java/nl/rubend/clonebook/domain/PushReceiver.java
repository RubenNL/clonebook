package nl.rubend.clonebook.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.rubend.clonebook.UUIDGenerator;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.hibernate.annotations.GenericGenerator;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.*;
import java.security.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PushReceiver {
	final static private String filename="pushfile";
	/*final static private String filename=getFileName();
	private static String getFileName() {
		Properties prop=new Properties();
		try {
			prop.load(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop.getProperty("folder")+"push.ser";
	}*/
	final static private KeyPair keyPair = getKey();
	final static private PushService pushService = new PushService(keyPair);

	public static String getPublicKeyString() {
		ECPublicKey key = (ECPublicKey) keyPair.getPublic();
		byte[] publicKey = key.getQ().getEncoded(false);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey);
	}

	private static KeyPair getKey() {
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.addProvider(new BouncyCastleProvider());
		try {
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			KeyPair key = (KeyPair) in.readObject();
			in.close();
			fileIn.close();
			return key;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			KeyPair keyPair = null;
			try {
				ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
				KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
				keyPairGenerator.initialize(parameterSpec);
				keyPair = keyPairGenerator.generateKeyPair();
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException ex) {
				ex.printStackTrace();
				throw new IllegalStateException(e);
			}
			try {
				FileOutputStream fileout = new FileOutputStream(filename);
				ObjectOutputStream out = new ObjectOutputStream(fileout);
				out.writeObject(keyPair);
				out.close();
				fileout.close();
			} catch (IOException f) {
				f.printStackTrace();
			}
			return keyPair;
		}
	}

	private String endpoint;
	private String key;
	@Id
	private String auth;
	@ManyToOne
	private User user;

	public void sendNotification(String message) {
		sendNotification(null,"null", message);
	}
	public void sendNotification(String action, Media image, String message) {
		sendNotification(action,"rest/media/"+image.getId(),message);
	}
	public void sendNotification(String action,String image,String message) {
		actualSend(action,image,message).start();
	}
	private Thread actualSend(String action, String image, String message) {
		//Geoptimaliseerd door op de achtergrond te werken, gaat veel sneller.
		return new Thread(() -> {
			Subscription.Keys keys = new Subscription.Keys(key, auth);
			Subscription subscription = new Subscription(endpoint, keys);
			JsonObjectBuilder payload = Json.createObjectBuilder();
			JsonObjectBuilder options = Json.createObjectBuilder();
			options.add("body", message);
			options.add("badge", "icons/grayscale.png");
			options.add("icon", "icons/256.png");
			options.add("timestamp",System.currentTimeMillis());
			if(!image.equals("")) options.add("image",image);
			if(action!=null) options.add("data",action);
			//action kan ook een URL zijn!
			payload.add("options", options);
			payload.add("title", "CloneBook");
			if(action!=null) payload.add("data",action);
			try {
				Notification notification = new Notification(subscription,payload.build().toString());
				HttpResponse response = pushService.send(notification);
				int code = response.getStatusLine().getStatusCode();
				if (code == 201) return;
				if (code == 410) return;
				System.out.println("code = " + code);
				return;
			} catch (JoseException | InterruptedException | IOException | ExecutionException | GeneralSecurityException e) {
				e.printStackTrace();
				System.out.println("NOTIFICATION ERROR!");
			}
			throw new UnsupportedOperationException();
		});
	}
}

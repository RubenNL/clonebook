package nl.rubend.ipass.domain;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.rubend.ipass.exceptions.IpassException;
import nl.rubend.ipass.utils.SqlInterface;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.jose4j.lang.JoseException;

import java.io.*;
import java.security.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PushReceiver {
	final static private KeyPair keyPair= getKey();
	final static private String filename="/home/pi/ipass/push.ser";
	public static String getPublicKeyString() {
		ECPublicKey key = (ECPublicKey) keyPair.getPublic();
		byte[] publicKey= key.getQ().getEncoded(false);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey);
	}
	private static KeyPair getKey() {
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.addProvider(new BouncyCastleProvider());
		try {
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			KeyPair key= (KeyPair) in.readObject();
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
				keyPair=keyPairGenerator.generateKeyPair();
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
			} catch(IOException f) {
				f.printStackTrace();
			}
			return keyPair;
		}
	}
	public static PushReceiver getReceiver(String id) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM pushReceiver WHERE ID=?");
			statement.setString(1, id);
			ResultSet set = statement.executeQuery();
			set.next();
			return new PushReceiver(set.getString("ID"), set.getString("endpoint"), set.getString("auth"), set.getString("key"),set.getString("userId"));
		} catch (SQLException e) {
			return null;
		}
	}
	private static ArrayList<PushReceiver> getReceivers(User user) {
		try {
			ArrayList<PushReceiver> response=new ArrayList<>();
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM pushReceiver WHERE userId=?");
			statement.setString(1, user.getId());
			ResultSet set = statement.executeQuery();
			while(set.next()) {
				response.add(new PushReceiver(set.getString("ID"), set.getString("endpoint"), set.getString("auth"), set.getString("key"), set.getString("userId")));
			}
			return response;
		} catch (SQLException e) {
			throw new IpassException(e.getMessage());
		}
	}
	public static void sendToUser(User user,String message) {
		for(PushReceiver receiver:getReceivers(user)) {
			receiver.sendNotification(message);
		}
	}
	private String id;
	private String endpoint;
	private String key;
	private String auth;
	private String userId;
	public PushReceiver(String endpoint,String key,String auth,User user) {
		this(UUID.randomUUID().toString(),endpoint,key,auth,user.getId());
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO pushReceiver(ID,endpoint,auth,`key`,userID) VALUES (?,?,?,?,?)");
			statement.setString(1, this.id);
			statement.setString(2, this.endpoint);
			statement.setString(3, this.auth);
			statement.setString(4, this.key);
			statement.setString(5, this.userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	private PushReceiver(String id,String endpoint,String key,String auth,String user) {
		this.id=id;
		this.endpoint=endpoint;
		this.key=key;
		this.auth=auth;
		this.userId=user;
	}
	public void sendNotification(String message) {
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.addProvider(new BouncyCastleProvider());
		PushService pushService = new PushService(keyPair);
		Subscription.Keys keys=new Subscription.Keys(key,auth);
		Subscription subscription=new Subscription(endpoint,keys);
		try {
			Notification notification = new Notification(subscription,message);
			HttpResponse response = pushService.send(notification);//deze call duurt 6 seconden.
			return;
		} catch (JoseException | InterruptedException | IOException | ExecutionException | GeneralSecurityException e) {
			e.printStackTrace();
			System.out.println("NOTIFICATION ERROR!");
		}
		throw new UnsupportedOperationException();
	}
	public String getUserId() {
		return this.userId;
	}
	public User getUser() {
		return User.getUserById(userId);
	}
	public void delete() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM pushReceiver WHERE ID=?");
			statement.setString(1, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
}

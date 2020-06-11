package nl.rubend.ipass.domain;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.jose4j.lang.JoseException;

import java.io.*;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

public class PushReceiver {
	final static private KeyPair keyPair= getKey();
	final static private String filename="/home/pi/ipass/push.ser";
	private static byte[] P256_HEAD = Base64.getDecoder()
			.decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgA");
	private static byte[] toUncompressedECPublicKey(ECPublicKey publicKey) {
		byte[] result = new byte[65];
		byte[] encoded = publicKey.getEncoded();
		System.arraycopy(encoded, P256_HEAD.length, result, 0,
				encoded.length - P256_HEAD.length);
		return result;
	}
	public static String getPublicKeyString() {
		ECPublicKey key = (ECPublicKey) keyPair.getPublic();
		String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(toUncompressedECPublicKey(key));
		return encoded;
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
	private String endpoint;
	private String key;
	private String auth;
	public PushReceiver(String endpoint,String key,String auth) {
		this.endpoint=endpoint;
		this.key=key;
		this.auth=auth;
	}
	public void sendNotification(String message) {
		System.out.println("NOTIFICATION START!");
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		Security.addProvider(new BouncyCastleProvider());
		PushService pushService = new PushService(keyPair);
		Subscription.Keys keys=new Subscription.Keys(key,auth);
		Subscription subscription=new Subscription(endpoint,keys);
		try {
			Notification notification = new Notification(subscription,message);
			HttpResponse response = pushService.send(notification);
			System.out.println("NOTIFICATION SEND!");
			return;
		} catch (JoseException | InterruptedException | IOException | ExecutionException | GeneralSecurityException e) {
			e.printStackTrace();
			System.out.println("NOTIFICATION ERROR!");
		}
		throw new UnsupportedOperationException();
	}
}

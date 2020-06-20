package nl.rubend.clonebook.utils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class Recaptcha {
	private static String privateKey=getPrivateKey();
	private static String publicKey=getPublicKey();
	private static String getPrivateKey() {
		try {
			Properties prop=new Properties();
			prop.load(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties")));
			return prop.getProperty("recaptchaPrivate");
		} catch (IOException e) {
			e.printStackTrace();
			return null; //Dit gaat foutmeldingen geven, maar is wel makkelijk op te sporen.
		}
	}
	private static String getPublicKey() {
		try {
			Properties prop=new Properties();
			prop.load(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties")));
			return prop.getProperty("recaptchaPublic");
		} catch (IOException e) {
			e.printStackTrace();
			return null; //Dit gaat foutmeldingen geven, maar is wel makkelijk op te sporen.
		}
	}
	public static String getKey() {
		return publicKey;
	}
	public static boolean isCaptchaValid(String response,String remoteIp) {
		try {
			String url = "https://www.google.com/recaptcha/api/siteverify",
					params = "secret=" + privateKey + "&response=" + response+"&remoteip="+ remoteIp;

			HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
			http.setDoOutput(true);
			http.setRequestMethod("POST");
			http.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded; charset=UTF-8");
			OutputStream out = http.getOutputStream();
			out.write(params.getBytes(StandardCharsets.UTF_8));
			out.flush();
			out.close();

			InputStream res = http.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(res, StandardCharsets.UTF_8));
			JsonStructure structure = Json.createReader(rd).read();
			res.close();
			if(structure.getValueType()!= JsonValue.ValueType.OBJECT) return false;
			JsonObject data=(JsonObject) structure;
			return data.getBoolean("success");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}

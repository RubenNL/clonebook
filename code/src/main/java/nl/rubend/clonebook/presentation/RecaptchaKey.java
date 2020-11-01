package nl.rubend.clonebook.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/recaptchaKey")
public class RecaptchaKey {
	@Value("${security.recaptchaPrivate}")
	private String privateKey;//=getPrivateKey();
	@Value("${security.recaptchaPublic}")
	private String publicKey;//=getPublicKey();

	@GetMapping
	public String getKey() {
		return publicKey;
	}
	public boolean isCaptchaValid(String response,String remoteIp) {
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
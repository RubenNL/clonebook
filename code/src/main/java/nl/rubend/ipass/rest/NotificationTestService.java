package nl.rubend.ipass.rest;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.concurrent.ExecutionException;

@Path("/notification")
public class NotificationTestService {
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response sendNotification(@FormParam("endpoint") String endpoint, @FormParam("key") String key, @FormParam("auth") String auth) {
		System.out.println("NOTIFICATION START!");
		Security.addProvider(new BouncyCastleProvider());
		PushService pushService = null;
		try {
			pushService = new PushService("BJqEt0KJ2PaOYrOZC1MQatRgm4ddt0Bc4O6hdriShTNFim1VvtDwpxA3YKkorzmljcwqObOnz2qs0n93H3fk1d8","ns29elFysYJ3IUMjpwqbzwm5hSvrySVpsuxANDzoa6o");
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
		Subscription.Keys keys=new Subscription.Keys(key,auth);
		Subscription subscription=new Subscription(endpoint,keys);
		try {
			Notification notification = new Notification(subscription,"test!");
			HttpResponse response = pushService.send(notification);
			System.out.println("NOTIFICATION SEND!");
			return Response.ok("ok!").build();
		} catch (JoseException | InterruptedException | IOException | ExecutionException | GeneralSecurityException e) {
			e.printStackTrace();
			System.out.println("NOTIFICATION ERROR!");
		}
		throw new UnsupportedOperationException();
	}
}

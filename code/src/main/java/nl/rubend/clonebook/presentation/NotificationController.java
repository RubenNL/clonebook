package nl.rubend.clonebook.presentation;

import nl.rubend.clonebook.data.SpringPushReceiverRepository;
import nl.rubend.clonebook.domain.PushReceiver;
import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;

@RestController
@RequestMapping("/notification")
@RolesAllowed("user")
public class NotificationController {
	private final SpringPushReceiverRepository repository;

	public NotificationController(SpringPushReceiverRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public KeyDTO getKey() {
		return new KeyDTO(PushReceiver.getPublicKeyString());
	}
	@PostMapping
	public void sendNotification(Principal principal,@RequestBody PushReceiver pushReceiver) {
		User user=(User) principal;
		pushReceiver.setUser(user);
		pushReceiver=repository.save(pushReceiver);
		pushReceiver.sendNotification("Meldingen ingesteld!");
	}
	@DeleteMapping("/{auth}")
	public void deletePushReceiver(Principal principal, @PathVariable String auth) {
		User user=(User) principal;
		PushReceiver receiver=repository.getOne(auth);
		if(!receiver.getUser().equals(user)) throw new ClonebookException("Niet toegestaan om andere listeners te verwijderen!");
		repository.delete(receiver);
	}
	@PostMapping("/{auth}")
	public void sendTestNotif(Principal principal, @PathVariable String auth) {
		User user=(User) principal;
		PushReceiver receiver=repository.getOne(auth);
		if(!receiver.getUser().equals(user)) throw new ClonebookException("Niet toegestaan om andere listeners te verwijderen!");
		receiver.sendNotification("testNotif!");
	}
	private static class KeyDTO {
		public String key;
		public KeyDTO(String key) {this.key=key;}
	}
}
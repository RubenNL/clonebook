package nl.rubend.clonebook.security.presentation.controller;

import nl.rubend.clonebook.data.SpringUserRepository;
import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.security.presentation.dto.Registration;
import nl.rubend.clonebook.utils.Recaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;

@RestController
@RequestMapping("/register")
public class RegistrationController {
	private final SpringUserRepository repository;

	public RegistrationController(SpringUserRepository repository) {
		this.repository = repository;
	}
	public static boolean isThisMyIpAddress(String stringAddr) {
		InetAddress addr;
		try {
			addr=InetAddress.getByName(stringAddr);
		} catch (UnknownHostException e) {
			return false;
		}
		if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) return true;
		try {
			return NetworkInterface.getByInetAddress(addr) != null;
		} catch (SocketException e) {
			return false;
		}
	}
	@PostMapping
	public void register(@Validated @RequestBody Registration registration, @RequestHeader("X-Forwarded-For") String forwardedFor, HttpServletRequest httpServletRequest) {
		String ip=httpServletRequest.getRemoteAddr();
		if(isThisMyIpAddress(ip)) ip=forwardedFor;//lokaal IP, grote kans dat het een apache2 aanvraag is.
		if(ip==null || ip.equals("")) ip=httpServletRequest.getRemoteAddr();//toch niet een apache2 proxy, dus toch maar het originele IP gebruiken.
		if(!Recaptcha.isCaptchaValid(registration.recaptchaResponse,ip)) throw new ClonebookException("invalid captcha!");//return Response.status(Response.Status.UNAUTHORIZED).entity(new AbstractMap.SimpleEntry<String,String>("error","captcha invalid")).build();
		Optional<User> optionalUser=repository.findById(registration.email);
		User user;
		if(optionalUser.isPresent()) user=optionalUser.get();
		else {
			user=new User();
			user.setEmail(registration.email);
		}
		user.sendPasswordForgottenUrl();
		this.repository.save(user);
	}
}


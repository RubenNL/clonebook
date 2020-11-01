package nl.rubend.clonebook.presentation;

import nl.rubend.clonebook.data.SpringNewPasswordRepository;
import nl.rubend.clonebook.data.SpringPageRepository;
import nl.rubend.clonebook.data.SpringUserRepository;
import nl.rubend.clonebook.domain.*;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.presentation.assembler.PageModelAssembler;
import nl.rubend.clonebook.security.SecurityBean;
import nl.rubend.clonebook.security.SecurityConfig;
import nl.rubend.clonebook.utils.SendEmail;
import nl.rubend.clonebook.websocket.WebSocket;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Email;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/user")
public class UserController {
	private final RecaptchaKey recaptchaKey;
	private final SpringUserRepository repository;
	private final SpringNewPasswordRepository newPasswordRepository;
	private final SpringPageRepository pageRepository;
	private final SendEmail sendEmail;
	private final SecurityConfig securityConfig;
	private final PageModelAssembler pageAssembler;
	public UserController(RecaptchaKey recaptchaKey, SpringUserRepository repository, SpringNewPasswordRepository newPasswordRepository, SpringPageRepository pageRepository, SendEmail sendEmail, SecurityConfig securityConfig, PageModelAssembler pageAssembler) {
		this.recaptchaKey = recaptchaKey;
		this.repository = repository;
		this.newPasswordRepository = newPasswordRepository;
		this.pageRepository = pageRepository;
		this.sendEmail = sendEmail;
		this.securityConfig = securityConfig;
		this.pageAssembler = pageAssembler;
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
	/*@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response newPasswordMail(@Context HttpHeaders httpHeaders, @Context HttpServletRequest httpServletRequest, @FormParam("email") String email, @FormParam("g-recaptcha-response") String recaptchaResponse) {
		String ip=httpServletRequest.getRemoteAddr();
		if(isThisMyIpAddress(ip)) ip=httpHeaders.getHeaderString("X-Forwarded-For");//lokaal IP, grote kans dat het een apache2 aanvraag is.
		if(ip==null || ip.equals("")) ip=httpServletRequest.getRemoteAddr();//toch niet een apache2 proxy, dus toch maar het originele IP gebruiken.
		if(!recaptchaKey.isCaptchaValid(recaptchaResponse,ip)) return Response.status(Response.Status.UNAUTHORIZED).entity(new AbstractMap.SimpleEntry<String,String>("error","captcha invalid")).build();
		User user=repository.findByEmail(email);
		if(user==null) user=new User();
		user.setEmail(email);
		user=repository.save(user);
		sendEmail.sendPasswordForgottenUrl(user);
		return Response.ok(new HashMap<String,String>()).build();
	}*/
	@PostMapping("/newPassword")
	public Response newPassword(@RequestBody newPasswordDTO newPasswordDTO) {
		NewPassword newPassword=newPasswordRepository.getOne(newPasswordDTO.code);
		User user=newPassword.use();
		try {
			user.setPassword(securityConfig.passwordEncoder().encode(newPasswordDTO.password));
			repository.save(user);
		} catch(IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new AbstractMap.SimpleEntry<String,String>("error",e.getMessage())).build();
		}
		newPasswordRepository.delete(newPassword);
		return Response.ok().build();
	}
	@GetMapping("/{userId}/settings")
	@RolesAllowed("user")
	public Response getSettings(@PathVariable String userId,@AuthenticationPrincipal User user) {
		if(!user.getId().equals(userId)) throw new ClonebookException("FORBIDDEN","Verkeerde gebruiker!");
		Map<String,String> map=new HashMap<>();
		map.put("email",user.getEmail());
		return Response.ok(map).build();
	}
	@PostMapping("/{userId}/settings")
	@RolesAllowed("user")
	public Response setProfile(@AuthenticationPrincipal User user,@PathVariable String userId,@RequestBody setEmailDTO setEmailDTO) {
		if (!user.getEmail().equals(setEmailDTO.email)) user.setEmail(setEmailDTO.email);
		repository.save(user);
		return Response.ok().build();
	}
	/*@GET
	@Path("/{userId}/lidAanvragen")
	@RolesAllowed("user")
	public Response getLidAanvragen(@BeanParam SecurityBean securityBean) {
		return Response.ok(securityBean.allowedUser().getLidAanvragenOpPaginas()).build();
	}*/
	@RolesAllowed("user")
	@GetMapping("/{userId}")
	public User publicUserProfile(@PathVariable String userId) {
		return repository.getOne(userId);
	}
	@DeleteMapping("/{userId}/sessions")
	@RolesAllowed("user")
	public Response resetSessions(@PathVariable String userId,@AuthenticationPrincipal User user) {
		if(!user.getId().equals(userId)) throw new ClonebookException("FORBIDDEN","geen toegang!");
		user.logoutAll();
		repository.save(user);
		return Response.ok(true).build();
	}
	@POST
	@RolesAllowed("user")
	@Path("/{userId}/notif")
	public Response sendTestNotif(@BeanParam SecurityBean securityBean) {
		securityBean.getSender().sendToUser("testNotif!");
		return Response.ok().build();
	}
	@PostMapping("/{userId}/ws")
	@RolesAllowed("user")
	public Response addSocket(@PathVariable String userId,@AuthenticationPrincipal User user) {
		if(!user.getId().equals(userId)) throw new ClonebookException("FORBIDDEN","GEEN TOEGANG");
		return Response.ok(new AbstractMap.SimpleEntry<String,String>("code",WebSocket.addWaiting(user))).build();
	}
	/*@GET
	@RolesAllowed("user")
	@Path("/{userId}/pages")
	public Response getPages(@BeanParam SecurityBean securityBean) {
		return Response.ok(securityBean.allowedUser().getPages()).build();
	}*/
	@RolesAllowed("user")
	@GetMapping("/{userId}/ownPages")
	public List<EntityModel<Page>> getOwnPages(@PathVariable String userId, @AuthenticationPrincipal String senderUserId) {
		System.out.println(senderUserId);
		System.out.println(userId);
		if(!userId.equals(senderUserId)) throw new ClonebookException("FORBIDDEN","geen toegang");
		List<EntityModel<Page>> pages = repository.getOne(userId).getOwnPages().stream()
				.map(pageAssembler::toModel)
				.collect(Collectors.toList());
		return pages;
	}
	private static class newPasswordDTO {
		public String code;
		public String password;
	}
	private static class setEmailDTO {
		@Email
		public String email;
	}
}
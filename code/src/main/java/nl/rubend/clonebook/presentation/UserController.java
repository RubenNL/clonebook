package nl.rubend.clonebook.presentation;

import nl.rubend.clonebook.data.SpringNewPasswordRepository;
import nl.rubend.clonebook.data.SpringPageRepository;
import nl.rubend.clonebook.data.SpringUserRepository;
import nl.rubend.clonebook.domain.*;
import nl.rubend.clonebook.security.SecurityBean;
import nl.rubend.clonebook.utils.SendEmail;
import nl.rubend.clonebook.websocket.WebSocket;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class UserController {
	private final RecaptchaKey recaptchaKey;
	private final SpringUserRepository repository;
	private final SpringNewPasswordRepository newPasswordRepository;
	private final SpringPageRepository pageRepository;
	private final SendEmail sendEmail;
	public UserController(RecaptchaKey recaptchaKey, SpringUserRepository repository, SpringNewPasswordRepository newPasswordRepository, SpringPageRepository pageRepository, SendEmail sendEmail) {
		this.recaptchaKey = recaptchaKey;
		this.repository = repository;
		this.newPasswordRepository = newPasswordRepository;
		this.pageRepository = pageRepository;
		this.sendEmail = sendEmail;
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
	@POST
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
	}
	@PostMapping("/newPassword")
	public Response use(@RequestBody newPasswordDTO newPasswordDTO) {
		NewPassword newPassword=newPasswordRepository.getOne(newPasswordDTO.code);
		User user=newPassword.use();
		try {
			user.setPassword(newPasswordDTO.password);
		} catch(IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new AbstractMap.SimpleEntry<String,String>("error",e.getMessage())).build();
		}
		newPasswordRepository.delete(newPassword);
		return Response.ok().build();
	}
	@GET
	@Path("/{userId}/settings")
	@RolesAllowed("user")
	public Response getSettings(@BeanParam SecurityBean securityBean) {
		User user = securityBean.allowedUser();
		Map<String,String> map=new HashMap<>();
		map.put("email",user.getEmail());
		return Response.ok(map).build();
	}
	@POST
	@Path("/{userId}/settings")
	@RolesAllowed("user")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setProfile(@FormParam("email") String email, @BeanParam SecurityBean securityBean) {
		User user = securityBean.allowedUser();
		if (!user.getEmail().equals(email)) user.setEmail(email);
		return Response.ok().build();
	}
	/*@GET
	@Path("/{userId}/lidAanvragen")
	@RolesAllowed("user")
	public Response getLidAanvragen(@BeanParam SecurityBean securityBean) {
		return Response.ok(securityBean.allowedUser().getLidAanvragenOpPaginas()).build();
	}*/
	@GET
	@RolesAllowed("user")
	@Path("/{userId}")
	public Response publicUserProfile(@BeanParam SecurityBean securityBean) {
		return Response.ok(securityBean.getRequested()).build();
	}
	@DELETE
	@RolesAllowed("user")
	@Path("/{userId}/sessions")
	public Response resetSessions(@BeanParam SecurityBean securityBean) {
		securityBean.allowedUser().logoutAll();
		return Response.ok(true).build();
	}
	@POST
	@RolesAllowed("user")
	@Path("/{userId}/notif")
	public Response sendTestNotif(@BeanParam SecurityBean securityBean) {
		securityBean.getSender().sendToUser("testNotif!");
		return Response.ok().build();
	}
	@POST
	@RolesAllowed("user")
	@Path("/{userId}/ws")
	public Response addSocket(@BeanParam SecurityBean securityBean) {
		return Response.ok(new AbstractMap.SimpleEntry<String,String>("code",WebSocket.addWaiting(securityBean.allowedUser()))).build();
	}
	/*@GET
	@RolesAllowed("user")
	@Path("/{userId}/pages")
	public Response getPages(@BeanParam SecurityBean securityBean) {
		return Response.ok(securityBean.allowedUser().getPages()).build();
	}*/
	@GET
	@RolesAllowed("user")
	@Path("/{userId}/ownPages")
	public Response getOwnPages(@BeanParam SecurityBean securityBean) {
		return Response.ok(securityBean.allowedUser().getOwnPages()).build();
	}
	private static class newPasswordDTO {
		public String code;
		public String password;
	}
}
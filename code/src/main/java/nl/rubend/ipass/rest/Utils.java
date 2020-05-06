package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.IpassException;
import nl.rubend.ipass.domain.Session;
import nl.rubend.ipass.domain.UnauthorizedException;
import nl.rubend.ipass.domain.User;

import javax.servlet.http.HttpServletRequest;

public class Utils {
	public static User getUser(HttpServletRequest req) throws UnauthorizedException {
		Object sessionId=req.getSession(true).getAttribute("sessionId");
		if(sessionId==null) throw new UnauthorizedException("niet ingelogd");
		Session session = Session.getSession((String) sessionId);
		return session.getUser();
	}
}

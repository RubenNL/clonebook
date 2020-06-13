package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.Media;
import nl.rubend.ipass.domain.User;
import nl.rubend.ipass.security.SecurityBean;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.*;

@Path("/media")
public class MediaService {
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("user")
	@POST
	public Response upload(@FormDataParam("file") InputStream file,@BeanParam SecurityBean securityBean) {
		Media media=new Media(file,securityBean.getSender().getId(), "/");
		if(!media.getMime().startsWith("image/")) {
			media.delete();
			return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
		}
		return Response.ok(media).build();
	}
	@GET
	@Path("/{fileId}")
	public Response download(@PathParam("fileId") String fileId) {
		Media media=Media.getMedia(fileId);
		if(media==null) throw new NotFoundException("Media niet gevonden");
		File file=media.getFile();
		if(file==null) throw new NotFoundException("File niet gevonden");
		return Response.ok(file).type(media.getMime()).build();
	}
	@GET
	@Path("/{fileId}/crop")
	public Response crop(@PathParam("fileId") String fileId) {
		Media media=Media.getMedia(fileId);
		if(media==null) throw new NotFoundException("Media niet gevonden");
		try {
			media.cropSquare();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.ok(media.getFile()).type(media.getMime()).build();
	}
}

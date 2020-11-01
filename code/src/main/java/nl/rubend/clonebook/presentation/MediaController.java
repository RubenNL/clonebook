package nl.rubend.clonebook.presentation;

import nl.rubend.clonebook.data.SpringMediaRepository;
import nl.rubend.clonebook.domain.Media;
import nl.rubend.clonebook.security.SecurityBean;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;

@Component
@Path("/media")
public class MediaController {
	private final SpringMediaRepository repository;

	public MediaController(SpringMediaRepository repository) {
		this.repository = repository;
	}

	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("user")
	@POST
	public Response upload(@Context HttpHeaders httpHeaders, @FormDataParam("file") InputStream file, @BeanParam SecurityBean securityBean) {
		//Jammergenoeg kan ik niet detecteren hoe groot het bestand is voordat het volledig is geupload...
		int size=httpHeaders.getLength();
		if(size>6*1024*1024) return Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE).build();
		//+1 mb, omdat er altijd padding bij zit.
		Media media=new Media(file,securityBean.getSender());
		if(!media.getMime().startsWith("image/")) {
			media.delete();
			return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
		}
		repository.save(media);
		return Response.ok(media).build();
	}
	@GET
	@Path("/{fileId}")
	public Response download(@PathParam("fileId") String fileId) {
		Media media=repository.getOne(fileId);
		File file=media.getFile();
		return Response.ok(file).type(media.getMime()).build();
	}
	@GET
	@Path("/{fileId}/crop")
	public Response crop(@PathParam("fileId") String fileId) {
		Media media=repository.getOne(fileId);
		try {
			media.cropSquare();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.ok(media.getFile()).type(media.getMime()).build();
	}
}
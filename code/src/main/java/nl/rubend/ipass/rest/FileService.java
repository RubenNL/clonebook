package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.FileStorage;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/file")
public class FileService {
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response upload(@FormDataParam("file") InputStream file) {
		return Response.ok(FileStorage.save(file)).build();
	}
	@GET
	@Path("/{fileId}")
	public Response download(@PathParam("fileId") String fileId) {
		return Response.ok(FileStorage.read(fileId)).build();
	}

}

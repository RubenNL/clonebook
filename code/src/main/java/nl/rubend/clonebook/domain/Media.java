package nl.rubend.clonebook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.rubend.clonebook.UUIDGenerator;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;
import org.apache.tika.Tika;
import org.hibernate.annotations.GenericGenerator;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import javax.persistence.*;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Media {
	@Id
	@GeneratedValue(generator= UUIDGenerator.generatorName)
	@GenericGenerator(name = UUIDGenerator.generatorName, strategy = "nl.rubend.clonebook.UUIDGenerator")
	private String id;
	@ManyToOne
	private User owner;
	private String mime;
	@ManyToOne
	private Post post;
	@Value("${uploads.folder}")
	private static File folder;
	public Media(InputStream file, User owner) {
		File destination = new File(folder, id);
		try {
			Files.copy(file, destination.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mime = new Tika().detect(destination.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@JsonIgnore
	public File getFile() {
		File file= new File(folder, String.valueOf(id));
		if(!file.exists()) throw new ClonebookException(Response.Status.NOT_FOUND,"file niet gevonden");
		return file;
	}

	public void delete() {
		try {
			Files.delete(getFile().toPath());
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public void cropSquare() throws IOException {
		BufferedImage file=ImageIO.read(getFile());
		int height=file.getHeight();
		int width=file.getWidth();
		int size=Math.min(height, width);
		int x=(width-size)/2;
		int y=(height-size)/2;
		BufferedImage cropped=Scalr.crop(file,x,y,size,size);
		ImageIO.write(cropped,"jpg",getFile());
		this.setMime("image/jpeg");
	}
}
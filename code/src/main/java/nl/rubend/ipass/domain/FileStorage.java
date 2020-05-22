package nl.rubend.ipass.domain;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

public class FileStorage {
	private static File uploads = new File("/home/pi/ipassUploads");
	public static String save(InputStream file) {
		String id=UUID.randomUUID().toString();
		File destination = new File(uploads, id);
		try {
			Files.copy(file, destination.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return id;
	}
	public static File read(String id) {
		return new File(uploads,id);
	}
}

package clamour;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Base64;

import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Test;

public class HelloAppEngineTest {

	private final static File imageFile = new File("src/main/java/image_processing/photos/dress.jpg");

	@Test
	public void test() throws IOException {
		Clamour clamourServlet = new Clamour();
		MockHttpServletResponse response = new MockHttpServletResponse();

		Reader inputString = new StringReader(encodeFileToBase64Binary(imageFile));
		BufferedReader bf = new BufferedReader(inputString);
		CLamourHttpServletRequest req = new CLamourHttpServletRequest();
		req.setBufferedReader(bf);

		clamourServlet.doGet(null, response);
		Assert.assertEquals("text/plain", response.getContentType());
		Assert.assertEquals("UTF-8", response.getCharacterEncoding());

		try {
			clamourServlet.doPost(req, response);
		} catch (ServletException e) {
			e.printStackTrace();
		}

		System.out.println(response.getWriterContent().toString());
	}

	private static String encodeFileToBase64Binary(File file) {
		String encodedfile = null;
		try {
			FileInputStream fileInputStreamReader = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			fileInputStreamReader.read(bytes);
			encodedfile = Base64.getEncoder().encodeToString(bytes);
			fileInputStreamReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return encodedfile;
	}
}

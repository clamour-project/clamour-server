package clamour;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


@WebServlet(name = "ClamourAppEngine", urlPatterns = {"/loader"})
public class Clamour extends HttpServlet {

	private static final long serialVersionUID = -5935880022820879027L;


@Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
      
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().print("Hello App Engine!\r\n");

  }
  

  public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
	  BufferedReader br  = request.getReader();
	  String line;
	  StringBuilder sb = new StringBuilder();
	  while((line = br.readLine()) != null)
		  sb.append(line.replaceAll("\n", ""));
	  
	  Image image = decoder(sb.toString());
	  
	  /*JFrame frame = new JFrame();
	  JLabel lblimage = new JLabel(new ImageIcon(image));
	  frame.getContentPane().add(lblimage, BorderLayout.CENTER);
	  frame.setSize(500, 500);
	  frame.setVisible(true);*/

	  response.setContentType("text/plain");
	  response.setCharacterEncoding("UTF-8");
	  response.getWriter().print("SUCCESS\n");
  }
  
  public static BufferedImage decoder(String base64Image) {
		
		BufferedImage image = null;
		try {
			byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
			image = ImageIO.read(new ByteArrayInputStream(imageByteArray));
			if (image == null) { System.out.println("Buffered Image is null"); }
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("SUCCESS!");
		return image;
	}
}
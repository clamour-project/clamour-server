package clamour;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import datastore_classes.ImageEntity;
import datastore_classes.OfyService;
import image_processing.ColorDetection;

@WebServlet(name = "ClamourSaveImage", urlPatterns = { "/save-image" })
public class SaveImageApi extends HttpServlet { 

	private static final Random rd = new Random();

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print("Hello App Engine!\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		DeepomaticApiClient apiClient = new DeepomaticApiClient();

		BufferedReader br;
		try {
			br = request.getReader();
			String line;
			StringBuilder base64Image = new StringBuilder();
			while ((line = br.readLine()) != null)
				base64Image.append(line.replaceAll("\n", ""));

			// data for api post request
			JSONObject requestToApi = new JSONObject();
			try {
				requestToApi.put("base64", base64Image);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			// response from api
			JSONObject respFromApi = apiClient.requestToApi(requestToApi);
			String type = getMainType(respFromApi);

			BufferedImage image = decoder(base64Image.toString());
			ColorDetection cd = new ColorDetection(image);
			Color mainColor = cd.getMainColor();

			ImageEntity imageForSaving = new ImageEntity(rd.nextLong(), base64Image.toString(), type, mainColor);
			OfyService.ofy().save().entity(imageForSaving).now();

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			response.getWriter().println("Image was saved to datastore");

		} catch (IOException e2) {
			e2.printStackTrace();
		}

		// BufferedReader br;
		// try {
		// br = request.getReader();
		// String line = "";
		// StringBuilder jsonImage = new StringBuilder();
		// while ((line = br.readLine()) != null)
		// jsonImage.append(line.replaceAll("\n", ""));
		//
		// ImageEntity imageForSaving;
		// JSONObject jsonObj;
		// try {
		// jsonObj = new JSONObject(jsonImage.toString());
		// String base64Image = jsonObj.getString("base64");
		// String type = jsonObj.getString("type");
		//
		// BufferedImage image = decoder(base64Image.toString());
		// ColorDetection cd = new ColorDetection(image);
		// Color mainColor = cd.getMainColor();
		//
		// imageForSaving = new ImageEntity(rd.nextLong(), base64Image, type,
		// mainColor);
		// OfyService.ofy().save().entity(imageForSaving).now();
		//
		// try {
		// TimeUnit.SECONDS.sleep(1);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		//
		// response.getWriter().println("Image was saved to datastore");
		// } catch (JSONException e1) {
		// e1.printStackTrace();
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	private String getMainType(JSONObject resp) {
		String priorityType = "";
		double maxArea = -1;

		Iterator<String> keys = resp.keys();
		String currKey = "";
		JSONArray currObj;
		double currArea = 0;
		while (keys.hasNext()) {
			currKey = keys.next();
			try {
				currObj = resp.getJSONArray(currKey);
				currArea = Math
						.abs(currObj.getJSONObject(0).getDouble("xmin") - currObj.getJSONObject(0).getDouble("xmax"))
						* Math.abs(currObj.getJSONObject(0).getDouble("ymin")
								- currObj.getJSONObject(0).getDouble("ymax"));

				if (currArea > maxArea) {
					maxArea = currArea;
					priorityType = currKey;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		return priorityType;
	}

	public static BufferedImage decoder(String base64Image) {

		BufferedImage image = null;
		try {
			byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
			image = ImageIO.read(new ByteArrayInputStream(imageByteArray));
			if (image == null) {
				System.out.println("Buffered Image is null");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("SUCCESS!");
		return image;
	}
}

package clamour;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.cloud.datastore.*;
import com.googlecode.objectify.cmd.Query;

import image_processing.ColorDetection;
import datastore_classes.ImageEntity;
import datastore_classes.OfyService;

@WebServlet(name = "ClamourAppEngine", urlPatterns = { "/clamour-api" })
public class Clamour extends HttpServlet {
 
	private static final long serialVersionUID = -5935880022820879027L;
 
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

	public void doPost(HttpServletRequest request, HttpServletResponse response){
		DeepomaticApiClient apiClient = new DeepomaticApiClient();
		
		System.out.println("Getting the photo...");
		
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
			String mainType = getMainType(respFromApi);
			
			List<String> suitableTypes = findSuitableTypes(mainType);
			
			BufferedImage image = decoder(base64Image.toString());
			ColorDetection cd = new ColorDetection(image);
			// matching colors
			ArrayList<Color> cmpColors = cd.getComplementaryColors(); 
			ArrayList<Color> adjColors = cd.getAdjacentColors();
			 
			adjColors.remove(0);
			cmpColors.remove(cmpColors.size()-1);
			cmpColors.remove(cmpColors.size()-1);
			
			List<Color> suitableColors = new ArrayList<>();
			suitableColors.addAll(cmpColors);
			suitableColors.addAll(adjColors);
			
			JSONObject resp = new JSONObject();
			try {
				// add detected types to response
				resp.put("main-type", mainType);
				
				// add suitable types to response
				for(String type:suitableTypes)
					resp.append("suitable-types", type);
				
				for (ImageEntity im : getSuitableClothes(suitableTypes, suitableColors))
					resp.append("suitable-clothes", im.getBase64Code());
				
				// add detected colors to response
				for(Color color : suitableColors)
				    resp.append("suitable-colors", getJSONColor(color));
				
			} catch (JSONException e) {
				e.printStackTrace();
			}

			response.setContentType("application/json, text/plain");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(resp.toString());
			response.getWriter().flush();
			
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	private List<ImageEntity> getSuitableClothes(List<String> types, List<Color> suitableColors) {
		Query query = OfyService.ofy().load().type(ImageEntity.class);
		Query currQuery = query;
		List<ImageEntity> suitableImages = new ArrayList<>();
		
		for(String type:types) {
			currQuery = query.filter("type =", type); 
			suitableImages.addAll(currQuery.list()); 
		}
		
		List<ImageEntity> res = new ArrayList<>();
		
		ImageEntity currImEntity;
		for(int i = 0; i<suitableImages.size(); i++) {
			currImEntity = suitableImages.get(i);
			for(Color color : suitableColors)
				if(ColorDetection.colorFits(color.getRGB(), currImEntity.getColor())) {
					res.add(currImEntity);
					break;
				}
		}
		return res; 
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

	
	private JSONObject getJSONColor(Color color) {
		JSONObject colorRGB = new JSONObject();
		try {
			colorRGB.put("red", color.getRed());
			colorRGB.put("green", color.getGreen());
			colorRGB.put("blue", color.getBlue());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return colorRGB;
	}
	
	private String getJsonData(String filename) {
		String path = getServletContext().getRealPath(filename);			
	
		BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader(path));
			StringBuilder sb = new StringBuilder();
			String line;
			try {
				line = bf.readLine();
				while (line != null) {
					sb.append(line);
					line = bf.readLine();
				}
				bf.close();
				return sb.toString();
				
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				if(bf != null)
					try {
						bf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return "";
	}

	private List<String> findSuitableTypes(String type) {
		String filename = "/suitable_types.json";
		String jsonData = getJsonData(filename);
		List<String> suitTypes = new ArrayList<>();
		try {
			JSONObject jsObj = new JSONObject(jsonData);
			
			JSONArray jsArr;
			
			jsArr = jsObj.getJSONArray(type);
				
			for(int i = 0; i<jsArr.length(); i++) {
				suitTypes.add(jsArr.getString(i));
			}
			return suitTypes;
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return suitTypes;
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

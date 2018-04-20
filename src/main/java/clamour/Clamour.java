package clamour;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import image_processing.ColorDetection;

@WebServlet(name = "ClamourAppEngine", urlPatterns = { "/clamour-api" })
public class Clamour extends HttpServlet {

	private static final long serialVersionUID = -5935880022820879027L;

	private String X_APP_ID = "921146564076";
	private String X_API_KEY = "10416c4d46344794b0f8af54b9462816";
	private String apiUrl = "https://api.deepomatic.com/v0.6/detect/fashion";

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print("Hello App Engine!\r\n");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		BufferedReader br = request.getReader();
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
		Iterator<String> clothesTypes = requestToApi(requestToApi);

		BufferedImage image = decoder(base64Image.toString());
		ColorDetection cd = new ColorDetection(image);
		// matching colors
		ArrayList<Color> cmpColors = cd.getComplementaryColors();
		ArrayList<Color> adjColors = cd.getAdjacentColors();
		
		JSONObject resp = new JSONObject();
		try {
			while (clothesTypes.hasNext())
			// add detected types to response
				resp.append("types", clothesTypes.next());
			
			// add detected colors to response
			resp.put("coplementary-colors", cmpColors);
			resp.put("adjacent-colors", adjColors);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		response.setContentType("application/json, text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(resp.toString());
		response.getWriter().flush();
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

	private Iterator<String> requestToApi(JSONObject image) {
		URL requestUrl = createUrl(apiUrl);
		String jsonResponse = postRequest(requestUrl, image);

		Pattern pattern = Pattern.compile("&quot;task_id&quot;:[ 0-9]*}");
		Matcher matcher = pattern.matcher(jsonResponse);
		String task_id = "";
		if (matcher.find()) {
			task_id = matcher.group(0).replaceAll("[^0-9]", "");
		}

		String succ = "";
		String apiEndpointUrl = "";
		JSONObject root = null;
		while (!succ.equals("success")) {
			apiEndpointUrl = "https://api.deepomatic.com/v0.6/tasks/" + task_id + "/?format=json";
			requestUrl = createUrl(apiEndpointUrl);
			jsonResponse = getRequest(requestUrl);

			try {
				root = new JSONObject(jsonResponse);
				root = root.getJSONObject("task");
				succ = root.getString("status");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			root = root.getJSONObject("data");
			root = root.getJSONObject("boxes");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return root.keys();
	}

	private URL createUrl(String stringUrl) {
		URL url = null;
		try {
			url = new URL(stringUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		return url;
	}

	private String getRequest(URL url) {
		String jsonResponse;
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;

		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("X-APP-ID", X_APP_ID);
			urlConnection.setRequestProperty("X-API-KEY", X_API_KEY);
			urlConnection.setReadTimeout(10000);
			urlConnection.setConnectTimeout(15000);
			urlConnection.connect();
			inputStream = urlConnection.getInputStream();
			jsonResponse = readFromStream(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonResponse;
	}

	private String postRequest(URL url, JSONObject data) {
		String jsonResponse = "";
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			urlConnection.setRequestProperty("X-APP-ID", X_APP_ID);
			urlConnection.setRequestProperty("X-API-KEY", X_API_KEY);
			urlConnection.setReadTimeout(10000);
			urlConnection.setConnectTimeout(15000);
			OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
			wr.write(data.toString());
			wr.flush();
			wr.close();
			urlConnection.connect();
			inputStream = urlConnection.getInputStream();
			jsonResponse = readFromStream(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonResponse;
	}

	private String readFromStream(InputStream inputStream) {
		StringBuilder output = new StringBuilder();
		if (inputStream != null) {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			try {
				String line = bufferedReader.readLine();
				while (line != null) {
					output.append(line);
					line = bufferedReader.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bufferedReader.close();
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return output.toString();
	}
}

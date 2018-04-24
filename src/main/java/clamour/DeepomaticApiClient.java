package clamour;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class DeepomaticApiClient {
	
	private String X_APP_ID = "921146564076";
	private String X_API_KEY = "10416c4d46344794b0f8af54b9462816";
	private String apiUrl = "https://api.deepomatic.com/v0.6/detect/fashion";
	
	public JSONObject requestToApi(JSONObject image) {
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

		return root;
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

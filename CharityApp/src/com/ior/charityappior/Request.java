package com.ior.charityappior;

import static com.ior.charityappior.Utils.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.ior.charityapp.models.Category;
import com.ior.charityapp.models.Helper;
import com.ior.charityapp.models.UserRequest;

/**
 * Created by android-dev on 27.08.13.
 */
public class Request {
	public static final int KM = 1000;
	//private static final String BASE_URL = "http://ior.applistore.mobi/api/";
	private static final String BASE_URL = "http://ior.applistore.mobi/apidemo/";
	// private static final String BASE_URL = "http://charity.mlsdev.com/api/";
	private static final String REGISTER = "register";
	private static final String TOKEN = "token";
	private static final String LOGIN = "login";
	private static final String DATA = "data";
	private static final String CATEGORIES = "categories";
	private static final String CATEGORY_NAME = "name";
	private static final String UPDATE_COORDINATES = "updateCoords";
	private static final String CATEGORY_ID = "category_id";
	private static final String ID = "id";
	private static final String PEOPLE = "getHelpers";
	private static final String HELPER_NAME = "name";
	private static final String DISTANCE = "distance";
	private static final String PHONE_NUMBER = "phone";
	private static final String HELPER_ID = "id";
	private static final String HELPER_PUSH_ID = "push_id";
	private static final String REQUEST_ID = "request_id";
	private static final String MESSAGE = "addHelpRequest";
	private static final String ACCEPTED_HELPERS = "getRequestHelpers";
	private static final String ACCEPT_REQUEST = "acceptHelpRequest";
	private static final String UPDATE_PROFILE = "updateProfile";
	private static final String HELPER_REGISTRATION = "helpersregister";
	private static final String GET_PROFILE = "getProfile";
	private static final String GET_INVITE_TEXT = "invitetext";
	private static final String MY_REQUESTS = "MyRequests";
	private static final String REQUEST_DESCRIPTION = "description";
	private static final String REQUEST_DATE_TIME = "date_time";
	private static final String REQUEST_HELPERS_COUNT = "heplers_count";
	private static final String HELPER_DATE_TIME = "date_time";
	private static final String REMOVE_REQUEST = "removeRequest";
	private static final String CHANGE_SPONSOR = "ChangeSponsor";
	private static final String CHANGE_LANGUAGE = "langauge";
	
	private static final String SET_AVAILABILITY = "setavailability";
	

	public static ArrayList<Category> getCategories(Context context) {
		String url = BASE_URL + CATEGORIES + "?token="
				+ Utils.getToken(context) + "&lang=" + getLang(context);
		
		HttpPost post = new HttpPost(url);
		ArrayList<Category> categories = new ArrayList<Category>();
		try {
			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("login", "==" + post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				return null;
			}
			InputStream inputStream = null;
			inputStream = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String result = builder.toString();
			log("login", result);
			if (result.contains("false"))
				return null;
			JSONObject jsonResult = new JSONObject(result);
			//JSONArray jsonCategories = jsonResult.getJSONArray(DATA);
			JSONObject getCategoryList = jsonResult.getJSONObject(DATA) ;
			JSONArray jsonCategories = getCategoryList.getJSONArray("categoryList");
			for (int i = 0; i < jsonCategories.length(); i++) {
				JSONObject category = jsonCategories.getJSONObject(i);
				categories.add(new Category(category.getInt(ID), category
						.getString(CATEGORY_NAME), getCategoryList.getString("availability")));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return categories;
	}

	public static ArrayList<Helper> getPeople(Context context, int minRadius,
			int maxRadius, int categoryId) {
		ArrayList<Helper> helpers = new ArrayList<Helper>();

		String url = BASE_URL + PEOPLE + "?token=" + Utils.getToken(context)
				+ "&min_radius=" + minRadius + "&max_radius=" + maxRadius
				+ "&category_id=" + categoryId + "&lang=" + getLang(context);

		log("People", "URL ===" + url);

		HttpPost post = new HttpPost(url);

		try {
			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("getPeople", post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				return null;
			}
			InputStream inputStream = null;

			inputStream = response.getEntity().getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			String result = builder.toString();
			log("getPeople", result);
			if (result.contains("false"))
				return helpers;
			JSONObject jsonResult = new JSONObject(result);
			JSONArray jsonHelpers = jsonResult.getJSONArray(DATA);
			for (int i = 0; i < jsonHelpers.length(); i++) {
				JSONObject helper = jsonHelpers.getJSONObject(i);
				helpers.add(new Helper(helper.getString(HELPER_NAME), helper
						.getString(DISTANCE), helper.getString(PHONE_NUMBER),
						helper.getInt(HELPER_ID), helper
								.getString(HELPER_PUSH_ID), null));
			}
			log("getPeople", result);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return helpers;
	}

	public static String login(Context context, String code, String phone) {
		HttpPost post = new HttpPost(BASE_URL + LOGIN);

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("User[phone]", phone));
		postParameters.add(new BasicNameValuePair("User[sms_code]", code));
		postParameters.add(new BasicNameValuePair("User[lang]",
				getLang(context)));
		postParameters.add(new BasicNameValuePair("User[country_code]", context
				.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
				.getString("countryCode", "")));

		try {
			post.setEntity(new UrlEncodedFormEntity(postParameters, "utf-8"));
			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("login", post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				return "Error";
			}
			InputStream inputStream = null;
			inputStream = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String result = builder.toString();
			log("login", result);
			if (result.contains("false"))
				return "Error";
			log("login", "result=" + result);
			JSONObject jsonResult = null;
			try {
				jsonResult = new JSONObject(result);
			} catch (Exception ex) {
				JSONTokener t = new JSONTokener(result);
				t.nextValue();
				jsonResult = new JSONObject(t);
			}
			String token = jsonResult.getJSONObject(DATA).getString(TOKEN);
			log("login", "token=" + token);
			Utils.saveToken(context, token);
		} catch (IOException e) {
			e.printStackTrace();
			return "Error";
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "Success";
	}

	public static String registerUser(String name, String pushId, String phone,
			Context context) {
		HttpPost post = new HttpPost(BASE_URL + REGISTER);

		try {
			log("registerUser", "registerUser pushId " + pushId);

			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("User[name]", name));
			postParameters.add(new BasicNameValuePair("User[phone]", phone));
			postParameters.add(new BasicNameValuePair("User[push_id]", pushId));
			postParameters
					.add(new BasicNameValuePair("User[country_code]", context
							.getSharedPreferences("app_settings",
									Context.MODE_PRIVATE).getString(
									"countryCode", "")));

			post.setEntity(new UrlEncodedFormEntity(postParameters, "utf-8"));

			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("register", post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				log("register", response.getStatusLine().toString());
				return "Error";
			}
			InputStream inputStream = null;

			inputStream = response.getEntity().getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			log("register", builder.toString());
			if (builder.toString().contains("false"))
				return "Error";
		} catch (IOException e) {
			e.printStackTrace();
			return "Error";
		}
		return "Success";
	}

	private static Location getLastBestLocation(Context context) {

		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		Location locationGPS = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location locationNet = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		long GPSLocationTime = 0;
		if (null != locationGPS) {
			GPSLocationTime = locationGPS.getTime();
		}

		long NetLocationTime = 0;

		if (null != locationNet) {
			NetLocationTime = locationNet.getTime();
		}

		if (0 < GPSLocationTime - NetLocationTime) {
			return locationGPS;
		} else {
			return locationNet;
		}

	}

	public static String updateCoordinates(Context context) {
		String url = BASE_URL + UPDATE_COORDINATES + "?token="
				+ Utils.getToken(context) + "&lang=" + getLang(context);

		HttpPost post = new HttpPost(url);

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		Location location = getLastBestLocation(context);
		if (location == null) {
			return "Error";
		}

		log("Latitude === ", String.valueOf(location.getLatitude()));
		log("Longitude ==== ", String.valueOf(location.getLongitude()));

		postParameters.add(new BasicNameValuePair("User[lat]", String
				.valueOf(location.getLatitude())));
		postParameters.add(new BasicNameValuePair("User[lon]", String
				.valueOf(location.getLongitude())));
		log("lat lon",
				"lat lon" + location.getLatitude() + " "
						+ location.getLongitude());

		try {
			post.setEntity(new UrlEncodedFormEntity(postParameters, "utf-8"));
			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("lat lon", post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				return "Error";
			}
			InputStream inputStream = null;

			inputStream = response.getEntity().getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			log("lat lon", builder.toString());
			if (builder.toString().contains("false"))
				return "Error";
		} catch (IOException e) {
			e.printStackTrace();
			return "Error";
		}
		return "Success";
	}

	public static String updateCoordinates(Context context, Location location) {
		String url = BASE_URL + UPDATE_COORDINATES + "?token="
				+ Utils.getToken(context) + "&lang=" + getLang(context);

		HttpPost post = new HttpPost(url);

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

		log("Latitude === ", String.valueOf(location.getLatitude()));
		log("Longitude ==== ", String.valueOf(location.getLongitude()));

		postParameters.add(new BasicNameValuePair("User[lat]", String
				.valueOf(location.getLatitude())));
		postParameters.add(new BasicNameValuePair("User[lon]", String
				.valueOf(location.getLongitude())));
		log("lat lon",
				"lat lon" + location.getLatitude() + " "
						+ location.getLongitude());

		try {
			post.setEntity(new UrlEncodedFormEntity(postParameters, "utf-8"));
			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("lat lon", post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				return "Error";
			}
			InputStream inputStream = null;

			inputStream = response.getEntity().getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			log("lat lon", builder.toString());
			if (builder.toString().contains("false"))
				return "Error";
		} catch (IOException e) {
			e.printStackTrace();
			return "Error";
		}
		return "Success";
	}

	public static String setAvailability(Context context, int availableFlag) {
		String idResult = null;
		try {
			
			String url = BASE_URL + SET_AVAILABILITY + "?token="
					+ Utils.getToken(context) + "&availability=" + availableFlag ;
			HttpPost post = new HttpPost(url);
			log("Availability URL", "URL====" + url);

			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("Availability", "==" + post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			log("response", "response====" + response);
			int status = response.getStatusLine().getStatusCode();
			log("status", "status====" + status);
			if (status != 200) {
				return null;
			}
			InputStream inputStream = null;

			inputStream = response.getEntity().getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			String result = builder.toString();
			log("Availability", "==" + result);
			if (result.contains("false"))
				return null;
			JSONObject jsonResult = new JSONObject(result);

			//JSONObject data = jsonResult.getJSONObject(DATA);
			idResult = jsonResult.getString("status");

			log("result", "id == " + idResult);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return idResult;
	}
	
	public static String sendMessage(Context context, int minRadius,
			int maxRadius, String description, String search, String requestId) {
		String idResult = null;
		try {
			description = URLEncoder.encode(description, "utf-8");

			String url = BASE_URL + MESSAGE + "?token="
					+ Utils.getToken(context) + "&min_radius=" + minRadius
					+ "&max_radius=" + maxRadius + "&description="
					+ description + "&lang=" + getLang(context) + "&search=" + search + "&request_id=" + requestId;
			HttpPost post = new HttpPost(url);
			log("Message URL", "URL====" + url);

			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("sendMessage", post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			log("response", "response====" + response);
			int status = response.getStatusLine().getStatusCode();
			log("status", "status====" + status);
			if (status != 200) {
				return null;
			}
			InputStream inputStream = null;

			inputStream = response.getEntity().getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			String result = builder.toString();
			log("sendMessage", result);
			if (result.contains("false"))
				return null;
			JSONObject jsonResult = new JSONObject(result);

			JSONObject data = jsonResult.getJSONObject(DATA);
			idResult = data.getString(REQUEST_ID);

			log("sendMessage", "id " + idResult);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return idResult;
	}

	public static ArrayList<Helper> getRequestHelpers(Context context,
			int requestId) {
		ArrayList<Helper> helpers = new ArrayList<Helper>();
		String url = BASE_URL + ACCEPTED_HELPERS + "?token="
				+ Utils.getToken(context) + "&request_id=" + requestId
				+ "&lang=" + getLang(context);

		log("Get Request", "URL === " + url);

		String result = sendRequest("getRequestHelpers", url, null);
		try {
			if (result == null)
				return null;
			if (result.contains("false"))
				return helpers;
			JSONObject jsonResult = new JSONObject(result);

			JSONArray jsonHelpers = jsonResult.getJSONArray(DATA);
			for (int i = 0; i < jsonHelpers.length(); i++) {
				JSONObject helper = jsonHelpers.getJSONObject(i);
				helpers.add(new Helper(helper.getString(HELPER_NAME), helper
						.getString(DISTANCE), helper.getString(PHONE_NUMBER),
						0, null, helper.getString(HELPER_DATE_TIME)));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return helpers;
	}

	private static String sendRequest(String tag, String url,
			List<NameValuePair> nameValuePairs) {
		String result;
		HttpPost post = new HttpPost(url);
		try {
			if (nameValuePairs != null)
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log(tag, post.getURI().toString());
			HttpResponse response = null;
			response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				log(tag, response.getStatusLine().toString());
				return null;
			}
			InputStream inputStream = null;
			inputStream = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			result = builder.toString();
			log(tag, result);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	public static String acceptHelpRequest(Context context, String requestId) {
		String url = BASE_URL + ACCEPT_REQUEST + "?token="
				+ Utils.getToken(context) + "&request_id=" + requestId
				+ "&lang=" + getLang(context);

		log("acceptHelpRequest", "URL ====" + url);
		String result = sendRequest("acceptHelpRequest", url, null);
		if (result == null)
			return null;
		if (result.contains("false"))
			return null;
		else
			return result;
	}

	public static String updateProfile(Context context,
			ArrayList<Integer> checkedItems) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		for (int i = 0; i < checkedItems.size(); i++) {
			nameValuePairs.add(new BasicNameValuePair(
					"category_ids[" + i + "]", String.valueOf(checkedItems
							.get(i))));
		}
		String url = BASE_URL + UPDATE_PROFILE + "?token="
				+ Utils.getToken(context) + "&lang=" + getLang(context);
		String result = sendRequest("updateProfile", url, nameValuePairs);
		if (result == null)
			return null;
		if (result.contains("false"))
			return null;

		return result;
	}
	
	//public static String login(Context context, String code, String phone) {
	public static String HelperRegistration(Context context, String fname, String lname, String emailAddress, String phoneNumber, String occupation, String address, String faceBookID, String twitterID, String sponserID) {
		
		HttpPost post = new HttpPost(BASE_URL + HELPER_REGISTRATION + "?token="
				+ Utils.getToken(context));

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("User[first_name]", fname));
		postParameters.add(new BasicNameValuePair("User[last_name]", lname));
		postParameters.add(new BasicNameValuePair("User[email]", emailAddress));
		postParameters.add(new BasicNameValuePair("User[homephone]", phoneNumber));
		postParameters.add(new BasicNameValuePair("User[occupation]", occupation));
		postParameters.add(new BasicNameValuePair("User[address]", address));
		postParameters.add(new BasicNameValuePair("User[facebook]", faceBookID));
		postParameters.add(new BasicNameValuePair("User[twitter]", twitterID));
		postParameters.add(new BasicNameValuePair("User[sponsor]", sponserID));
		
		try {
			post.setEntity(new UrlEncodedFormEntity(postParameters, "utf-8"));
			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("login", post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				return "Error";
			}
			InputStream inputStream = null;
			inputStream = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			String result = builder.toString();
			log("login", result);
			if (result.contains("false"))
				return "Error";
			log("login", "result=" + result);
			JSONObject jsonResult = null;
			try {
				jsonResult = new JSONObject(result);
			} catch (Exception ex) {
				JSONTokener t = new JSONTokener(result);
				t.nextValue();
				jsonResult = new JSONObject(t);
			}
			String token = jsonResult.getJSONObject(DATA).getString(TOKEN);
			log("login", "token=" + token);
			Utils.saveToken(context, token);
		} catch (IOException e) {
			e.printStackTrace();
			return "Error";
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "Success";
	}
	
	/*public static String HelperRegistration(Context context, String fname, String lname, String emailAddress, String phoneNumber, String occupation, String address, String faceBookID, String twitterID, String sponserID) {
		 
		String idResult = null;
		try {
			fname = URLEncoder.encode(fname, "utf-8");
			lname = URLEncoder.encode(lname, "utf-8");
			emailAddress = URLEncoder.encode(emailAddress, "utf-8");
			occupation = URLEncoder.encode(occupation, "utf-8");
			address = URLEncoder.encode(address, "utf-8");
			faceBookID = URLEncoder.encode(faceBookID, "utf-8");
			twitterID = URLEncoder.encode(twitterID, "utf-8");
			sponserID = URLEncoder.encode(sponserID, "utf-8");
			

			String url = BASE_URL + HELPER_REGISTRATION + "?token="
					+ Utils.getToken(context) + "&first_name="+ fname + "&last_name=" + lname+ "&email=" + emailAddress + "&homephone=" + phoneNumber 
					+ "&occupation="+ occupation + "&address="+ address + "&facebook="+ faceBookID + "&twitter=" + twitterID + "&sponsor=" + sponserID +"&lang=" + getLang(context);
			log("url", "==" + url) ;
			
			HttpPost post = new HttpPost(url);
			log("Message URL", "URL====" + url);

			HttpClient client = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			log("sendMessage", post.getURI().toString());

			HttpResponse response = null;

			response = client.execute(post);
			log("response", "response====" + response);
			int status = response.getStatusLine().getStatusCode();
			log("status", "status====" + status);
			if (status != 200) {
				return null;
			}
			InputStream inputStream = null;

			inputStream = response.getEntity().getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			String result = builder.toString();
			log("sendMessage", result);
			if (result.contains("false"))
				return null;
			JSONObject jsonResult = new JSONObject(result);

			JSONObject data = jsonResult.getJSONObject(DATA);
			idResult = data.getString(REQUEST_ID);

			log("sendMessage", "id " + idResult);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return idResult;
	}*/
	
	/*
	public static String HelperRegistration(Context context, String fname, String lname, String emailAddress, String phoneNumber, String occupation, String address, String faceBookID, String twitterID, String sponserID) {
		
		String url = BASE_URL + HELPER_REGISTRATION + "?token="
				+ Utils.getToken(context) + "&first_name="+ fname + "&last_name=" + lname+ "&email=" + emailAddress + "&homephone=" + phoneNumber 
				+ "&occupation="+ occupation + "&address="+ address + "&facebook="+ faceBookID + "&twitter=" + twitterID + "&sponsor=" + sponserID +"&lang=" + getLang(context);
		log("url", "==" + url) ;
		String result = sendRequest("helpersregister", url, null);
		log("result", "==" + result) ;
		if (result == null)
			return null;
		if (result.contains("false"))
			return null;

		return result;
	} */

	public static String updateLanguage(Context context, String langValue) {

		String url = BASE_URL + CHANGE_LANGUAGE + "?token="
				+ Utils.getToken(context) + "&lang=" + langValue;

		String result = sendRequest("updateProfile", url, null);
		if (result == null)
			return null;
		if (result.contains("false"))
			return null;

		return result;
	}

	/**
	 * Returns invite text to be sent as invitation message
	 * 
	 * @param context
	 * @return
	 */
	public static void getInviteText(Context context,
			OnInviteTextLoadListener listener, boolean isHebrew) {
		String url = BASE_URL + GET_INVITE_TEXT + "?token="
				+ Utils.getToken(context) + "&lang=" + getLang(context);
		String result = sendRequest("invitetext", url, null);
		if (result == null)
			return;
		if (result.contains("false"))
			return;

		try {
			JSONObject jsonResult = null;
			try {
				jsonResult = new JSONObject(result);
			} catch (Exception ex) {
				JSONTokener t = new JSONTokener(result);
				t.nextValue();
				jsonResult = new JSONObject(t);
			}

			JSONObject jsonData = jsonResult.getJSONObject(DATA);

			String email = jsonData.getString(isHebrew ? "gmail_hebrew"
					: "gmail");
			String facebook = jsonData.getString(isHebrew ? "facebook_hebrew"
					: "facebook");
			String phone = jsonData.getString(isHebrew ? "phone_number_hebrew"
					: "phone_number");

			// callback
			listener.onLoadEmailInviteText(email);
			listener.onLoadFacebookInviteText(facebook);
			listener.onLoadPhoneInviteText(phone);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Integer> getProfile(Context context) {
		ArrayList<Integer> categoryIds = new ArrayList<Integer>();
		String url = BASE_URL + GET_PROFILE + "?token="
				+ Utils.getToken(context) + "&lang=" + getLang(context);
		String result = sendRequest("getProfile", url, null);
		if (result == null)
			return null;
		if (result.contains("false"))
			return null;
		try {
			JSONObject jsonResult = new JSONObject(result);
			JSONArray jsonData = jsonResult.getJSONArray(DATA);
			for (int i = 0; i < jsonData.length(); i++) {
				JSONObject categoryId = jsonData.getJSONObject(i);
				categoryIds.add(categoryId.getInt(CATEGORY_ID));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return categoryIds;
	}

	public static ArrayList<UserRequest> myRequests(Context context) {
		ArrayList<UserRequest> requests = new ArrayList<UserRequest>();
		String url = BASE_URL + MY_REQUESTS + "?token="
				+ Utils.getToken(context) + "&lang=" + getLang(context);
		String result = sendRequest("myRequests", url, null);
		if (result == null)
			return null;
		if (result.contains("false"))
			return null;
		try {
			JSONObject jsonResult = new JSONObject(result);
			JSONArray jsonData = jsonResult.getJSONArray(DATA);
			for (int i = 0; i < jsonData.length(); i++) {
				JSONObject request = jsonData.getJSONObject(i);
				requests.add(new UserRequest(request.getString(ID), request
						.getString(REQUEST_DESCRIPTION), request
						.getString(REQUEST_DATE_TIME), request
						.getString(REQUEST_HELPERS_COUNT)));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return requests;
	}

	public static String removeRequest(Context context, Integer requestId) {
		String url = BASE_URL + REMOVE_REQUEST + "?token="
				+ Utils.getToken(context) + "&request_id=" + requestId
				+ "&lang=" + getLang(context);
		String result = sendRequest("removeRequest", url, null);
		if (result == null)
			return null;
		if (result.contains("false"))
			return null;
		return result;
	}

	public static String changeSponsor(Context context, String name,
			String phone) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("User[sponsor_name]", name));
		nameValuePairs
				.add(new BasicNameValuePair("User[sponsor_phone]", phone));
		String url = BASE_URL + CHANGE_SPONSOR + "?token="
				+ Utils.getToken(context) + "&lang=" + getLang(context);
		String result = sendRequest("changeSponsor", url, nameValuePairs);
		if (result == null)
			return null;
		if (result.contains("false"))
			return null;
		return result;
	}

	private static String getLang(Context context) {
		String lang = context.getSharedPreferences("app_settings",
				Context.MODE_PRIVATE).getString("lang", "English");
		if (lang.equalsIgnoreCase("hebrew")) {
			return "he";
		}

		return "";
	}

	public static interface OnInviteTextLoadListener {
		public void onLoadEmailInviteText(String text);

		public void onLoadPhoneInviteText(String text);

		public void onLoadFacebookInviteText(String text);
	}
}
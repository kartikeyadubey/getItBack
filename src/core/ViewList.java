package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import session.SessionEvents.LogoutListener;

import charting.PersonTotal;

import com.facebook.android.Facebook;

import android.app.ListActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;

/**
 * Super class for ViewBills and ViewReturns
 * contains core functions for both ViewBills and ViewReturns
 * helps in initializing the data for the graph
 * @author Kartikeya
 *
 */
public class ViewList extends ListActivity{
	// Facebook object
		protected Facebook mFacebook;
		// Set facebook app id
		public static final String APP_ID = "161076967321553";

		// Adapter to create the scrollable list
		protected ArrayAdapter<IdDescription> _adapter;

		// Bill list: Name - Description: Amount
		protected ArrayList<IdDescription> _returnList;
		// User id
		protected String _userid;

		// Graphing button
		protected Button _graphButton;
		

		// Total collections a person needs to make
		// for graphing
		protected PersonTotal[] _personTotal;
		
		//Logout button
		protected LoginButton _logoutButton;
		
		/**
		 * Set the total amount to collect or return for the given person
		 * @param uri the uri from which the data needs to be received
		 * @param person the person(unique facebook userid) for which the data is being fetched
		 */
		protected void setPersonTotal(URI uri, String person) {
			String page = get(uri);
			JSONArray total = null;
			JSONObject object = null;
			try {
				object = (JSONObject) new JSONTokener(page).nextValue();
				total = object.getJSONArray(_userid);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				_personTotal = new PersonTotal[total.length()];
				for (int i = 0; i < total.length(); i++) {
					Log.v("Got some data",
							total.getJSONObject(i).getString("total"));
					_personTotal[i] = new PersonTotal(total.getJSONObject(i)
							.getString(person), total.getJSONObject(i)
							.getDouble("total"));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/**
		 * Logout and return to home screen if back key is pressed
		 */
	    @Override 
	    public boolean onKeyDown(int keyCode, KeyEvent event)
	    {
	    	super.onKeyDown(keyCode, event);
	    	switch (keyCode)
	    	{
	    		case KeyEvent.KEYCODE_BACK:
	    			if (getParent() == null) {
	    			    setResult(StartPage.MOVED_BACK);
	    			} else {
	    			    getParent().setResult(StartPage.MOVED_BACK);
	    			}
	    			finish();
	    			return true;
	    	}
			return false;
	    	
	    }
		
		/**
		 * Post to server and delete the entry for given user id and other
		 * details
		 */
		public static boolean remove(String personOne, String personTwo,
				String description, String date, String amount) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://kartikeyadubey.com/getItBackServer/removeBill.php");

			try {
				// Add data
				Log.v("Date", date);
				Log.v("Money", amount);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("personOne",
						personOne));
				nameValuePairs.add(new BasicNameValuePair("personTwo",
						personTwo));
				nameValuePairs.add(new BasicNameValuePair("description",
						description));
				nameValuePairs.add(new BasicNameValuePair("date", date));
				nameValuePairs.add(new BasicNameValuePair("amount", amount));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				String postResponse = getResponse(response);
				Log.d("Post response", postResponse);
				// get readable response
				if (postResponse.equalsIgnoreCase("S")) {
					Log.d("Successfully removed the entry", "..");
					return true;
				} else {
					return false;
				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		
		/**
		 * Fetch data for a given uri
		 * return json formatted string
		 * @param uri the uri from which the data is to be fetched
		 * @return json formatted data that was received by the http request
		 */
		public static String get(URI uri)
		{
			String retVal = null;
			BufferedReader in = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(uri);
				HttpResponse response = client.execute(request);
				in = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();
				retVal = sb.toString();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return retVal;
		}
		
		/**
		 * Get readable POST response in order to ensure
		 * that the post was succesfully made and the database was updated
		 * @param response
		 * @return string what the server returned in response to the POST request
		 */
		public static String getResponse(HttpResponse response)
		{
			BufferedReader in;
			StringBuffer sb = new StringBuffer("");
			try {
				in = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				String line = "";
				while ((line = in.readLine()) != null) {
					sb.append(line);
				}
				in.close();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String page = sb.toString();
			return page;
		}
		
		/**
		 * Get the total amount of money that needs to be collected or returned based on param3
		 * @param uri the uri from which the data is to be fetched
		 * @param id the userid of the person for which the data is fetched
		 * @param choice "totalReturn" - total return amount or "totalToCollect" - total amount to be collected
		 * @return
		 */
		public double getAmount(URI uri, String id, String choice)
		{
			Log.d("User id ", id);
			double retVal = 0;
			String page = null;
			page = get(uri);
			JSONArray total = null;
			JSONObject object = null;
			String tmp = id;
			try {
				object = (JSONObject) new JSONTokener(page).nextValue();
				total = object.getJSONArray(tmp);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			try {
				for (int i = 0; i < total.length(); i++) {
					Log.v("Got some data",
							total.getJSONObject(i).getString(choice));
					retVal = total.getJSONObject(i).getDouble(choice);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return retVal;
		}
		
		
		public class ListLogoutListener implements LogoutListener {
			public void onLogoutBegin() {
				Log.v("Intent Logging out...", "....");
			}

			public void onLogoutFinish() {
				Log.v("Intent Logging out...", "Complete");
				Log.d("Activity is returning result", Integer.toString(StartPage.RETURN_FROM_TABS));

				/*
				 * Send back activity result to top activity
				 */
				if (getParent() == null) {
				    setResult(StartPage.RETURN_FROM_TABS);
				} else {
				    getParent().setResult(StartPage.RETURN_FROM_TABS);
				}
				finish();
			}
		}
}

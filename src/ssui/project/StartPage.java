package ssui.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
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

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.facebook.android.Util;
import ssui.project.SessionEvents.AuthListener;
import ssui.project.SessionEvents.LogoutListener;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class StartPage extends Activity {
	// Facebook object
	private Facebook _mFacebook;
	// Set facebook app id
	public static final String APP_ID = "161076967321553";

	//Text views providing description
	//of what data needs to be entered and amount ofmoney to be returned
	//and collected
	private TextView mText;
	private TextView _date;
	private AutoCompleteTextView _name;
	private TextView _mCollect;
	private TextView _mReturn;

	
	
	// Logout button
	private LogoutButton _mLogoutButton;
	//Submission button
	private Button _submit;
	//Change date button
	private Button dateButton;

	//Auto complete friend name
	private AutoCompleteTextView _personOne;
	
	//Amount of money
	private EditText _moneyAmount;
	//Description of bill
	private EditText _description;
	
	//List of all friends
	public String[] _friendNames;
	private JSONArray _friendList;
	
	//Amount of money to collect
	double _collectMoney;
	//Amount of money to return
	double _returnMoney;
	
	static final int DATE_DIALOG_ID  = 0;
	
	//Date of bill
	private int mYear;
	private int mMonth;
	private int mDay;
	
	//Current userid
	private String _currUID;
	//Current user's name
	private String _currUName;
	//Date for bill stored in mysql format
	private String _postDate;

	/**
	 * Called when Activity is created Generate the login screen for the user to
	 * sign in
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v("Intent", "is created");
		// Create an instance of Facebook
		_mFacebook = new Facebook(APP_ID);
		new AsyncFacebookRunner(_mFacebook);

		// Restore information from the facebook variable
		SessionStore.restore(_mFacebook, this);
		SessionEvents.addAuthListener(new StartPageAuthListener());
		SessionEvents.addLogoutListener(new StartPageLogoutListener());

		GetDataTask task = new GetDataTask();
		task.execute(new String[] { "me", "me/friends" });

		if (_mFacebook.isSessionValid())
			Log.v("Intent session", "is valid");
		setContentView(R.layout.userlogin);
		dateButton = (Button) findViewById(R.id.dateButton);
		_mCollect = (TextView) findViewById(R.id.collectMoney);
		_mReturn = (TextView) findViewById(R.id.returnMoney);
		_date = (TextView) findViewById(R.id.date);
		_submit = (Button) findViewById(R.id.submitButton);
		_description = (EditText) findViewById(R.id.description);
		_name = (AutoCompleteTextView) findViewById(R.id.autocompleteFriends);
		_moneyAmount = (EditText) findViewById(R.id.moneyAmount);
		mText = (TextView) findViewById(R.id.testText);
		_mLogoutButton = (LogoutButton) findViewById(R.id.fb_logout);
		_mLogoutButton.init(this, _mFacebook);


		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = _name.getText().toString();
				String id = getFriendId(name);
				String description = _description.getText().toString();
				String date = _date.getText().toString();
				String amount = _moneyAmount.getText().toString();
				postData(name, id, description, date,amount);
			}
		});
		

		_date.setText(Integer.toString(mMonth)+ "-" + Integer.toString(mDay) + "-" + Integer.toString(mYear));
		_postDate = Integer.toString(mYear) + "-" + Integer.toString(mMonth)+ "-" + Integer.toString(mDay);

		dateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID );
			}
		});
	}

	/**
	 * Post the data and add it into database
	 * @param amount amount of money
	 * @param date date of bill
	 * @param description description of bill
	 * @param personTwoName name of person who borrowed money
	 * @param personTwo id of person who borrowed money
	 */
	private void postData(String personTwoName, String personTwo, String description, String date, String amount)
	{
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://www.kartikeyadubey.com/getitbackserver/addBill.php");

	    try {
	        // Add data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("personOne", _currUID));
	        nameValuePairs.add(new BasicNameValuePair("personOneName", _currUName));
	        nameValuePairs.add(new BasicNameValuePair("personTwo", personTwo));
	        nameValuePairs.add(new BasicNameValuePair("personTwoName", personTwoName));
	        nameValuePairs.add(new BasicNameValuePair("description", description));
	        nameValuePairs.add(new BasicNameValuePair("date", date));
	        nameValuePairs.add(new BasicNameValuePair("amount", amount));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	    } catch (ClientProtocolException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	
	/**
	 * Traverse the friendList array and find the appropriate id for a given name
	 */
	private String getFriendId(String name)
	{
		String retVal = null;
		for (int i = 0; i < _friendList.length(); i++) {
			JSONObject temp;
			try {
				temp = _friendList.getJSONObject(i);
				if(temp.get("name").equals(name))
				{
					retVal = temp.get("id").toString();
					return retVal;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return retVal;
	}
	
	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener =
			new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, 
				int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			//Update UI
			updateDisplay();
		}
	};


	// updates the date in the TextView
	private void updateDisplay() {
		_date.setText(
				new StringBuilder()
				// Month is 0 based so add 1
				.append(mMonth + 1).append("-")
				.append(mDay).append("-")
				.append(mYear).append(" "));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this,
					mDateSetListener,
					mYear, mMonth, mDay);
		}
		return null;
	}

	/**
	 * Asynchronous data collection
	 * update UI when data has been received
	 * show progress box while data is loading
	 * @author Kartikeya
	 *
	 */
	private class GetDataTask extends AsyncTask<String, Integer, Long> {
		private final ProgressDialog dialog = new ProgressDialog(StartPage.this);

		protected void onPreExecute() {
			this.dialog.setMessage("Refreshing...");
			this.dialog.show();
		}

		protected Long doInBackground(String... urls) {
			try {
				JSONObject jsonNames = Util.parseJson(_mFacebook
						.request(urls[1]));
				_friendList = jsonNames.getJSONArray("data");
				setFriendNames();
				JSONObject jsonMoney = Util.parseJson(_mFacebook
						.request(urls[0]));
				_currUName = jsonMoney.getString("name");
				_currUID = jsonMoney.getString("id");
				
				final double totalMoney[] = getData(_currUID);
				_collectMoney = totalMoney[0];
				_returnMoney = totalMoney[1];
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (FacebookError e) {
				e.printStackTrace();
			}
			return (long) 1;
		}

		protected void setFriendNames() {
			_friendNames = new String[_friendList.length()];
			final String[] tempfriendNames = new String[_friendList.length()];
			for (int i = 0; i < _friendList.length(); i++) {
				JSONObject temp;
				try {
					temp = _friendList.getJSONObject(i);
					tempfriendNames[i] = temp.get("name").toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			_friendNames = tempfriendNames;
		}

		/**
		 * Httprequest to connect to server and post data and receive and parse
		 * response
		 */
		private double[] getData(String id) {
			// Index 0 is money to collect
			// Index 1 is money to return
			double[] retVal = { 0, 0 };
			Log.v("HTTP try to get", "data");
			BufferedReader in = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				try {
					request.setURI(new URI(
							"http://kartikeyadubey.com/getItBackServer/getUserTotal.php?id="
									+ id));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
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
				String page = sb.toString();
				JSONArray total = null;
				JSONObject object = null;
				try {
					object = (JSONObject) new JSONTokener(page).nextValue();
					total = object.getJSONArray(id);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				try {
					for (int i = 0; i < total.length(); i++) {
						Log.v("Got some data", total.getJSONObject(i)
								.getString("totalToCollect"));
						retVal[0] = total.getJSONObject(i).getDouble(
								"totalToCollect");
						retVal[1] = total.getJSONObject(i).getDouble(
								"totalReturn");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Long result) {
			if (this.dialog.isShowing() && result == 1) {
				_personOne = (AutoCompleteTextView) findViewById(R.id.autocompleteFriends);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						StartPage.this, R.layout.list_item, _friendNames);
				_personOne.setAdapter(adapter);
				_mCollect.append(Double.toString(_collectMoney));
				_mReturn.append(Double.toString(_returnMoney));
				this.dialog.dismiss();
			}
		}
	}

	public class StartPageAuthListener implements AuthListener {

		public void onAuthSucceed() {
			mText.setText("Intent You have logged in! ");
			dateButton.setVisibility(View.VISIBLE);
		}

		public void onAuthFail(String error) {
			mText.setText("Login Failed: " + error);
		}
	}

	public class StartPageLogoutListener implements LogoutListener {
		public void onLogoutBegin() {
			Log.v("Intent Logging out...", "....");
		}

		public void onLogoutFinish() {
			setResult(RESULT_OK);

			Log.v("Intent Logging out...", "Complete");
			finish();
		}
	}

}

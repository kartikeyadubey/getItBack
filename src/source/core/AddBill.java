package source.core;

import java.io.IOException;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.facebook.android.Util;
import session.SessionEvents;
import session.SessionStore;
import session.SessionEvents.AuthListener;
import session.SessionEvents.LogoutListener;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Class that handles addition of new bills
 * @author Kartikeya
 * TODO error handling
 *
 */
public class AddBill extends Activity {
	// Facebook object
	private Facebook mFacebook;
	// Set facebook app id
	public static final String APP_ID = "161076967321553";

	//Text views providing description
	//of what data needs to be entered and amount ofmoney to be returned
	//and collected
	private TextView mText;
	private AutoCompleteTextView _name;
	//Submission button
	private Button _submit;
	//Change date button
	private Button _dateButton;
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
	//Logout button
	private LoginButton _logoutButton;

	/**
	 * Called when Activity is created Generate the login screen for the user to
	 * sign in
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v("Intent", "has been created");
		// Create an instance of Facebook
		mFacebook = new Facebook(APP_ID);

		// Restore information from the facebook variable
		SessionStore.restore(mFacebook, getApplicationContext());
		SessionEvents.addLogoutListener(new StartPageLogoutListener());

		GetDataTask task = new GetDataTask();
		task.execute(new String[] { "me", "me/friends" });

		if (mFacebook.isSessionValid())
			Log.v("Intent session", "is valid");
		
		setContentView(R.layout.add_bill);
		_dateButton = (Button) findViewById(R.id.dateButton);
		
		_submit = (Button) findViewById(R.id.submitButton);

		_description = (EditText) findViewById(R.id.description);
		_name = (AutoCompleteTextView) findViewById(R.id.autocompleteFriends);
		
		_moneyAmount = (EditText) findViewById(R.id.moneyAmount);
		
		_logoutButton = new LoginButton(getApplicationContext());
		_logoutButton.init(this, mFacebook, new String[] {});
		
		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		
		_dateButton.setText(Integer.toString(mMonth + 1)+ "-" + Integer.toString(mDay) + "-" + Integer.toString(mYear));
		_postDate = Integer.toString(mYear) + "-" + Integer.toString(mMonth)+ "-" + Integer.toString(mDay);
		
		_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = _name.getText().toString();
				String id = getFriendId(name);
				String description = _description.getText().toString();
				String date = _postDate;
				String amount = _moneyAmount.getText().toString();
				postData(name, id, description, date,amount);
			}
		});

		_dateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
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
     * Options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add(0, Menu.FIRST, Menu.NONE, "Refresh");
    	menu.add(0, Menu.FIRST + 1, Menu.NONE, "Logout");
    	return super.onCreateOptionsMenu(menu);
    }
    
    /**
     * Handle options menu button click
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    		case Menu.FIRST: GetDataTask task = new GetDataTask();
    		task.execute(new String[] { "me", "me/friends" }); 
    		return true;	
    		case (Menu.FIRST+1): 
    			_logoutButton.logout();
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
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
	    HttpPost httppost = new HttpPost("http://kartikeyadubey.com/getItBackServer/addBill.php");

	    try {
	        // Add data
	    	Log.v("Date", date);
	    	Log.v("Money", amount);
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
	        String postResponse = ViewList.getResponse(response);
	        Log.d("Post response", postResponse);
	        //get readable response
	        if(postResponse.equalsIgnoreCase("S"))
	        {
	        	Toast.makeText(this, "Bill Added Successfully", Toast.LENGTH_LONG).show();
	        	clearOptions();
	        }
	        else
	        {
	        	Toast.makeText(this, "Please Try Submitting Again", Toast.LENGTH_LONG).show();
	        }
	        
	    } catch (ClientProtocolException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	
	/**
	 * Clear all the options
	 */
	private void clearOptions()
	{
		UpdateDataTask update = new UpdateDataTask();
		update.execute(new String[]{"me"});
	}
	
	/**
	 * Async task to clear UI and update the total to be 
	 * collected and returned
	 */
	private class UpdateDataTask extends AsyncTask<String, Integer, Long> {

		protected void onPreExecute() {
		}

		protected Long doInBackground(String... urls) {
			try {
				JSONObject jsonMoney = Util.parseJson(mFacebook
						.request(urls[0]));
				_currUName = jsonMoney.getString("name");
				_currUID = jsonMoney.getString("id");
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return (long) -1;
			} catch (IOException e) {
				e.printStackTrace();
				return (long) -1;
			} catch (JSONException e) {
				e.printStackTrace();
				return (long) -1;
			} catch (FacebookError e) {
				e.printStackTrace();
				return (long) -1;
			}
			return (long) 1;
		}
		
		protected void onPostExecute(Long result) {
			if (result == 1) {
				_dateButton.setText(Integer.toString(mMonth)+ "-" + Integer.toString(mDay) + "-" + Integer.toString(mYear));
				_name.setText("");
				_description.setText("");
				_moneyAmount.setText("");
			}
			else if(result == -1)
			{
				//Finish activity here and return to StartPage
				setResult(StartPage.RESULT_CANCELED);
				finish();
			}
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
		_dateButton.setText(
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
		private final ProgressDialog dialog = new ProgressDialog(AddBill.this);

		protected void onPreExecute() {
			dialog.setMessage("Loading...");
			dialog.show();
		}

		protected Long doInBackground(String... urls) {
			try {
				JSONObject jsonNames = Util.parseJson(mFacebook
						.request(urls[1]));
				_friendList = jsonNames.getJSONArray("data");
				setFriendNames();
				JSONObject jsonMoney = Util.parseJson(mFacebook
						.request(urls[0]));
				_currUName = jsonMoney.getString("name");
				_currUID = jsonMoney.getString("id");
				SessionStore.setUser(_currUID);
				double totalMoney[] = {0,0};
				try {
					totalMoney = getData(new URI(
							"http://kartikeyadubey.com/getItBackServer/getUserTotal.php?id="
									+ _currUID));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				_collectMoney = totalMoney[0];
				_returnMoney = totalMoney[1];
			} catch (MalformedURLException e) {
				return (long) -1;
			} catch (IOException e) {
				e.printStackTrace();
				return (long) -1;
			} catch (JSONException e) {
				e.printStackTrace();
				return (long) -1;
			} catch (FacebookError e) {
				e.printStackTrace();
				return (long) -1;
			}
			return (long) 1;
		}
		
		/**
		 * Create UI
		 */
		protected void onPostExecute(Long result) {
			if (result == 1) {
				dialog.hide();
				_personOne = (AutoCompleteTextView) findViewById(R.id.autocompleteFriends);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						AddBill.this, R.layout.list_item, _friendNames);
				_personOne.setAdapter(adapter);
				this.dialog.dismiss();
			}
			else if(result == -1)
			{
				//Go back to start page
				setResult(StartPage.RESULT_CANCELED);
				finish();
			}
		}
		
		/**
		 * Set the auto complete text
		 */
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
		private double[] getData(URI uri) {
			// Index 0 is money to collect
			// Index 1 is money to return
			double[] retVal = {0,0};
			String page = ViewList.get(uri);
				JSONArray total = null;
				JSONObject object = null;
				try {
					object = (JSONObject) new JSONTokener(page).nextValue();
					total = object.getJSONArray(_currUID);
				} catch (JSONException e1) {
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
					e.printStackTrace();
				}
			return retVal;
		}

		protected void onProgressUpdate(Integer... progress) {

		}
	}

	public class StartPageAuthListener implements AuthListener {

		public void onAuthSucceed() {
			mText.setText("Intent You have logged in! ");
			_dateButton.setVisibility(View.VISIBLE);
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

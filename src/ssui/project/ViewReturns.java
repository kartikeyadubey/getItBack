package ssui.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import session.SessionStore;

import com.facebook.android.Facebook;
import com.facebook.android.R;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ViewReturns extends ListActivity 
{
	// Facebook object
	private Facebook mFacebook;
	// Set facebook app id
	public static final String APP_ID = "161076967321553";
	
	//Adapter to create the scrollable list
	private ArrayAdapter<IdDescription> adapter;
	
	//Bill list: Name - Description: Amount
	private ArrayList<IdDescription> returnList;
	
	private String userid;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mFacebook = new Facebook(APP_ID);
		SessionStore.restore(mFacebook, getApplicationContext());
		
		setContentView(R.layout.view_bills);
		
		userid = getIntent().getStringExtra("userid").toString();
		
		Log.d("User id received", userid);
		
		GetDataTask t = new GetDataTask();
		t.execute(userid);
	}
	
	
	/**
	 * Asynchronous data collection
	 * update UI when data has been received
	 * show progress box while data is loading
	 * @author Kartikeya
	 *
	 */
	private class GetDataTask extends AsyncTask<String, Integer, Long> {

		protected void onPreExecute() {
		}

		protected Long doInBackground(String... id) {
			getData(id[0]);
			return (long) 1;
		}


		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Long result) {
			adapter = new BillListAdapter(ViewReturns.this, R.layout.row_layout,returnList);
			setListAdapter(adapter);
			ListView list = getListView();
			registerForContextMenu(list);	
		}

		/**
		 * Httprequest to connect to server and post data and receive and parse
		 * response
		 */
		private void getData(String id) {
			// Index 0 is money to collect
			// Index 1 is money to return
			Log.v("HTTP trying to get", "data");
			BufferedReader in = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				try {
					request.setURI(new URI(
							"http://kartikeyadubey.com/getItBackServer/getUserReturns.php?id="
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
				String bills = sb.toString();
				JSONObject bList = null;
				try {
					bList = (JSONObject) new JSONTokener(bills).nextValue();
				} catch (JSONException e1) {
					ViewReturns.this.runOnUiThread(new Runnable(){

						@Override
						public void run() {
							Toast.makeText(getApplicationContext(), "Please check you internet connection and try again", Toast.LENGTH_LONG).show();
						}	
					});
					e1.printStackTrace();
				}
				try {
					returnList = new ArrayList<IdDescription>();
					for (int i = 0; i < bList.getJSONArray(id).length(); i++) {
						JSONObject j = bList.getJSONArray(id).getJSONObject(i);
						returnList.add(new IdDescription(j.getString("personOne"),
										j.getString("personOneName") + "\n" + j.getString("description") + " : $" + j.getString("amount")
										, j.getString("amount"), j.getString("date"), j.getString("description")));
					}
				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(), "Please check you internet connection and try again", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			} catch (ClientProtocolException e) {
				Toast.makeText(getApplicationContext(), "Please check you internet connection and try again", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), "Please check you internet connection and try again", Toast.LENGTH_LONG).show();
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
		}
	}
}

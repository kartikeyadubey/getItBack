package ssui.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ViewBills extends ListActivity
{
	
	// Facebook object
	private Facebook mFacebook;
	// Set facebook app id
	public static final String APP_ID = "161076967321553";
	
	//Adapter to create the scrollable list
	private ArrayAdapter<IdDescription> adapter;
	
	//Bill list: Name - Description: Amount
	private ArrayList<IdDescription> billList;
	
	private String userid;
	/**
	 * Called when activity is first created
	 * do the layout, get the data through an async task
	 */
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
	
	/*@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
	}*/
	
	
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    	super.onCreateContextMenu(menu, v, menuInfo);  
        menu.setHeaderTitle("Options");  
        menu.add(0, v.getId(), 0, "Mark as Paid");
        // TODO publish to wall
        //menu.add(0, v.getId(), 0, "Send Message");  
        
    } 
    
    
    @Override  
    public boolean onContextItemSelected(MenuItem item) {  
    	if(item.getTitle() == "Mark as Paid")
    	{
	    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    	String personTwo = billList.get(info.position).userId;
	    	String short_description = billList.get(info.position).short_description;
	    	String date = billList.get(info.position).date;
	    	String amount = billList.get(info.position).amount;
	    	
	    	GetDataTask t = new GetDataTask();
			t.execute(new String[]{userid, "remove", personTwo, short_description, date, amount});		
	    	
			return true;
    	}
    	else if(item.getTitle() == "Send Message")
    	{
    		
    	}
    	return false;
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
			if(id.length > 1 && id[1] == "remove" && id.length == 6)
			{
				Log.d("User is now", "removing a post");
				removeData(id[0], id[2], id[3], id[4], id[5]);
			}
			getData(id[0]);
			return (long) 1;
		}


		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Long result) {
			adapter = new BillListAdapter(ViewBills.this, R.layout.row_layout,billList);
			setListAdapter(adapter);
			ListView list = getListView();
			registerForContextMenu(list);	
		}

		/**
		 * Post to server and delete the entry for given user id and other details
		 */
		private boolean removeData(String personOne, String personTwo, String description, String date, String amount)
		{
				// Create a new HttpClient and Post Header
			    HttpClient httpclient = new DefaultHttpClient();
			    HttpPost httppost = new HttpPost("http://kartikeyadubey.com/getItBackServer/removeBill.php");

			    try {
			        // Add data
			    	Log.v("Date", date);
			    	Log.v("Money", amount);
			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			        nameValuePairs.add(new BasicNameValuePair("personOne", personOne));
			        nameValuePairs.add(new BasicNameValuePair("personTwo", personTwo));
			        nameValuePairs.add(new BasicNameValuePair("description", description));
			        nameValuePairs.add(new BasicNameValuePair("date", date));
			        nameValuePairs.add(new BasicNameValuePair("amount", amount));
			        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			        // Execute HTTP Post Request
			        HttpResponse response = httpclient.execute(httppost);
			        String postResponse = getResponse(response);
			        Log.d("Post response", postResponse);
			        //get readable response
			        if(postResponse.equalsIgnoreCase("S"))
			        {
			        	Log.d("Successfully removed the entry", "..");
			        	return true;
			        }
			        else
			        {
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
		 * Get readable POST response
		 * @param response
		 * @return string what the server returned in response to the POST request
		 */
		private String getResponse(HttpResponse response)
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
							"http://kartikeyadubey.com/getItBackServer/getBills.php?id="
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
					Toast.makeText(getApplicationContext(), "Please check you internet connection and try again", Toast.LENGTH_LONG).show();
					e1.printStackTrace();
				}
				try {
					billList = new ArrayList<IdDescription>();
					for (int i = 0; i < bList.getJSONArray(id).length(); i++) {
						JSONObject j = bList.getJSONArray(id).getJSONObject(i);
						billList.add(new IdDescription(j.getString("personTwo"),
										j.getString("personTwoName") + "\n" + j.getString("description") + " : $" + j.getString("amount")
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

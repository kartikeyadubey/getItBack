package source.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import session.SessionEvents;
import session.SessionStore;
import charting.PieChart;
import com.facebook.android.Facebook;
import com.facebook.android.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class that handles the display for all returns
 * @author Kartikeya
 *
 */
public class ViewReturns extends ViewList {
	
	// Returns
	private TextView _returns;

	public double _returnMoney;

	/**
	 * Called when the application is first created
	 * sets up initial display and starts lazy loading of images
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFacebook = new Facebook(APP_ID);
		SessionStore.restore(mFacebook, getApplicationContext());
		SessionEvents.addLogoutListener(new ListLogoutListener());
		setContentView(R.layout.view_list);
		_graphButton = (Button) findViewById(R.id.graphButton);
		_returns = (TextView) findViewById(R.id.money);
		_userid = SessionStore.getUser();
		_logoutButton = new LoginButton(getApplicationContext());
		_logoutButton.init(this, mFacebook, new String[] {});
		Log.d("User id received", _userid);

		GetDataTask t = new GetDataTask();
		t.execute(_userid);
	}
	
	
	/**
	 * Refreshes data onResume
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		GetDataTask t = new GetDataTask();
		t.execute(_userid);
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
    	Log.d("option", Integer.toString(item.getItemId()));
    	switch(item.getItemId())
    	{
    		case Menu.FIRST: GetDataTask task = new GetDataTask();
    		task.execute(_userid); 
    		return true;	
    		case (Menu.FIRST+1): 
    			_logoutButton.logout();
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
	
	/**
	 * Create context menu
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Options");
		menu.add(0, Menu.FIRST, 0, "Send Reminder");
	}
	

	/**
	 * Handle on click for context menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle() == "Send Reminder") 
		{
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			String personTwo = _returnList.get(info.position).userId;
			Bundle params = new Bundle();
		    params.putString("to", String.valueOf(personTwo));
		    mFacebook.dialog(this, "feed", params, new WallPostDialogListener());
		}
		return false;
	}
    
	/**
	 * Asynchronous data collection, update UI when data has been received, show
	 * progress box while data is loading
	 * 
	 * @author Kartikeya
	 * 
	 */
	private class GetDataTask extends AsyncTask<String, Integer, Long> {

		protected void onPreExecute() {
		}

		/**
		 * Fetch the data in the background
		 */
		protected Long doInBackground(String... id) {
			getData(id[0]);
			try {
				setPersonTotal(new URI(
						"http://kartikeyadubey.com/getItBackServer/getGraphReturn.php?id="
								+ _userid), "personOneName");
				_returnMoney = getAmount(new URI(
						"http://kartikeyadubey.com/getItBackServer/getUserTotal.php?id="+ _userid), _userid, "totalReturn");
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return (long) -1;
			}
			return (long) 1;
		}

		protected void onProgressUpdate(Integer... progress) {

		}
		/**
		 * Finished getting data
		 * Update the UI
		 */
		protected void onPostExecute(Long result) {
			if(result == 1)
			{
				Log.d("Inside view returns", "should get here");
				_adapter = new BillListAdapter(ViewReturns.this,
						R.layout.row_layout, _returnList);
				setListAdapter(_adapter);
				ListView list = getListView();
				registerForContextMenu(list);
				_returns.setText("You need to return: " + Double.toString(_returnMoney));
				_graphButton.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						Intent i = new Intent();
						PieChart b = new PieChart();
						i = b.execute(getApplicationContext(), _personTotal, "Who do I owe?");
						startActivity(i);
					}
				});
			}
			else if(result == -1)
			{
				setResult(StartPage.RESULT_CANCELED);
				finish();
			}
		}

		/**
		 * Httprequest to connect to server and post data and receive and parse
		 * response
		 */
		private void getData(String id) {
			String bills = null;
			try {
				bills = ViewReturns.get(new URI(
						"http://kartikeyadubey.com/getItBackServer/getUserReturns.php?id="
								+ id));
			} catch (URISyntaxException e2) {
				e2.printStackTrace();
			}
			JSONObject bList = null;
			try {
				bList = (JSONObject) new JSONTokener(bills).nextValue();
			} catch (JSONException e1) {
				ViewReturns.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								getApplicationContext(),
								"Please check you internet connection and try again",
								Toast.LENGTH_LONG).show();
					}
				});
				e1.printStackTrace();
			}
			try {
				_returnList = new ArrayList<IdDescription>();
				for (int i = 0; i < bList.getJSONArray(id).length(); i++) {
					JSONObject j = bList.getJSONArray(id).getJSONObject(i);
					_returnList.add(new IdDescription(j.getString("personOne"),
							j.getString("personOneName") + "\n"
									+ j.getString("description") + " : $"
									+ j.getString("amount"), j
									.getString("amount"), j.getString("date"),
									j.getString("description")));
				}
			} catch (JSONException e) {
				Toast.makeText(getApplicationContext(),
						"Please check you internet connection and try again",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
	}
	
	
	public class WallPostDialogListener extends facebook.BaseDialogListener {

        public void onComplete(Bundle values) {
            final String postId = values.getString("post_id");
            if (postId != null) {
                Log.d("Facebook", "Dialog Success! post_id=" + postId);
                ViewReturns.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Posted Reminder Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.d("Facebook", "No wall post made");
            }
        }
	}
}

package core;

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
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that handles displaying the list of collections for a given user
 * 
 * @author Kartikeya
 * 
 */
public class ViewBills extends ViewList {

	// Bill list: Name - Description: Amount
	private ArrayList<IdDescription> billList;

	// Collect string
	private TextView _collect;

	public double _collectMoney;

	/**
	 * Called when activity is first created do the layout, get the data through
	 * an async task
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("Creating new ", "View bill");

		mFacebook = new Facebook(APP_ID);
		SessionStore.restore(mFacebook, getApplicationContext());
		SessionEvents.addLogoutListener(new ListLogoutListener());
		setContentView(R.layout.view_list);

		_graphButton = (Button) findViewById(R.id.graphButton);
		_collect = (TextView) findViewById(R.id.money);
		_userid = SessionStore.getUser();
		_logoutButton = new LoginButton(getApplicationContext());
		_logoutButton.init(this, mFacebook, new String[] {});

		Log.d("User id received", _userid);

		GetDataTask t = new GetDataTask();
		t.execute(_userid);
	}

	/**
	 * Refresh data onResume
	 */
	@Override
	public void onResume() {
		super.onResume();
		GetDataTask t = new GetDataTask();
		t.execute(_userid);
	}

	/**
	 * Create context menu
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Options");
		menu.add(0, Menu.FIRST, 0, "Mark as Paid");
		menu.add(0, Menu.FIRST + 1, 0, "Send Reminder");
	}

	/**
	 * Handle on click for context menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle() == "Mark as Paid") {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			String personTwo = billList.get(info.position).userId;
			String short_description = billList.get(info.position).short_description;
			String date = billList.get(info.position).date;
			String amount = billList.get(info.position).amount;

			GetDataTask t = new GetDataTask();
			t.execute(new String[] { _userid, "remove", personTwo,
					short_description, date, amount });
			return true;
		} 
		else if (item.getTitle() == "Send Reminder") 
		{
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			String personTwo = billList.get(info.position).userId;
			Bundle params = new Bundle();
		    params.putString("to", String.valueOf(personTwo));
		    mFacebook.dialog(this, "feed", params, new WallPostDialogListener());
		}
		return false;
	}
	
	
	/**
	 * Options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, Menu.FIRST, Menu.NONE, "Refresh");
		menu.add(0, Menu.FIRST + 1, Menu.NONE, "Logout");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handle options menu button click
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("option", Integer.toString(item.getItemId()));
		switch (item.getItemId()) {
		case Menu.FIRST:
			GetDataTask task = new GetDataTask();
			task.execute(_userid);
			return true;
		case (Menu.FIRST + 1):
			_logoutButton.logout();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Asynchronous data collection update UI when data has been received show
	 * progress box while data is loading
	 * 
	 * @author Kartikeya
	 * 
	 */
	private class GetDataTask extends AsyncTask<String, Integer, Long> {

		protected void onPreExecute() {
		}

		protected Long doInBackground(String... id) {
			if (id.length > 1 && id[1] == "remove" && id.length == 6) {
				Log.d("User is now", "removing a post");
				remove(id[0], id[2], id[3], id[4], id[5]);
			}
			getData(id[0]);
			try {
				_collectMoney = getAmount(new URI(
						"http://kartikeyadubey.com/getItBackServer/getUserTotal.php?id="
								+ _userid), _userid, "totalToCollect");
				setPersonTotal(new URI(
						"http://kartikeyadubey.com/getItBackServer/getGraphCollect.php?id="
								+ _userid), "personTwoName");
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
				return (long) -1;
			}
			return (long) 1;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Long result) {
			if(result == 1)
			{
				Log.d("inside", "bills pe");
				_adapter = new BillListAdapter(ViewBills.this, R.layout.row_layout,
						billList);
				setListAdapter(_adapter);
				ListView list = getListView();
				registerForContextMenu(list);
				_collect.setText("You need to collect: "
						+ Double.toString(_collectMoney));
				_graphButton.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						Intent i = new Intent();
						PieChart b = new PieChart();
						i = b.execute(getApplicationContext(), _personTotal,
								"Who owes me?");
						startActivity(i);
					}
				});
				Log.d("inside", "bills pe 2");
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
				bills = ViewBills.get(new URI(
						"http://kartikeyadubey.com/getItBackServer/getBills.php?id="
								+ id));
			} catch (URISyntaxException e2) {
				e2.printStackTrace();
			}
			JSONObject bList = null;
			try {
				bList = (JSONObject) new JSONTokener(bills).nextValue();
			} catch (JSONException e1) {
				Toast.makeText(getApplicationContext(),
						"Please check you internet connection and try again",
						Toast.LENGTH_LONG).show();
				e1.printStackTrace();
			}
			try {
				billList = new ArrayList<IdDescription>();
				for (int i = 0; i < bList.getJSONArray(id).length(); i++) {
					JSONObject j = bList.getJSONArray(id).getJSONObject(i);
					billList.add(new IdDescription(j.getString("personTwo"), j
							.getString("personTwoName")
							+ "\n"
							+ j.getString("description")
							+ " : $"
							+ j.getString("amount"), j.getString("amount"), j
							.getString("date"), j.getString("description")));
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
                ViewBills.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Posted Reminder Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.d("Facebook", "No wall post made");
                ViewBills.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Post failed please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

}

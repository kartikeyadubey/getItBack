package ssui.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.facebook.android.AsyncFacebookRunner;
import ssui.project.BaseRequestListener;

import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.facebook.android.Util;
import ssui.project.SessionEvents.AuthListener;
import ssui.project.SessionEvents.LogoutListener;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StartPage extends Activity 
{
    //Facebook object
    private Facebook mFacebook;
    //Asynchronous API requests through this variable
    private AsyncFacebookRunner mAsyncRunner;
    // Set facebook app id
    public static final String APP_ID = "161076967321553";
    private Button mRequestButton;
    private TextView mText;
    //Logout button
    private LogoutButton mLogoutButton;
	/**
	 * Called when Activity is created
	 * Generate the login screen for the user to sign in
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Log.v("Intent", "is created");
		  //Create an instance of Facebook
        mFacebook = new Facebook(APP_ID);
        //Instantiate the request api variable to the instance of facebook
       	mAsyncRunner = new AsyncFacebookRunner(mFacebook);
       	
       	//Restore information from the facebook variable
        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new StartPageAuthListener());
        SessionEvents.addLogoutListener(new StartPageLogoutListener());
        
       	if(mFacebook.isSessionValid())Log.v("Intent session", "is valid");
		setContentView(R.layout.userlogin);
		mRequestButton = (Button) findViewById(R.id.testRequestButton);
		mText = (TextView) findViewById(R.id.testText);
		mLogoutButton = (LogoutButton) findViewById(R.id.fb_logout);
		mLogoutButton.init(this, mFacebook);

        
        mRequestButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	mAsyncRunner.request("me", new SampleRequestListener());
            }
        });
        mRequestButton.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);
	}
	
	
    public class StartPageAuthListener implements AuthListener {

        public void onAuthSucceed() {
            mText.setText("Intent You have logged in! ");
            mRequestButton.setVisibility(View.VISIBLE);
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
    
	
	public class SampleRequestListener extends BaseRequestListener {

	    public void onComplete(final String response, final Object state) {
	        try {
	            // process the response here: executed in background thread
	            Log.d("Facebook-Examplef", "Response: " + response.toString());
	            JSONObject json = Util.parseJson(response);
	            final String name = json.getString("id");
                getData(name);

	            // then post the processed result back to the UI thread
	            // if we do not do this, an runtime exception will be generated
	            // e.g. "CalledFromWrongThreadException: Only the original
	            // thread that created a view hierarchy can touch its views."
	            StartPage.this.runOnUiThread(new Runnable() {
	                public void run() {
	                    mText.setText("Hello there, " + name + "!");
	                }
	            });
	        } catch (JSONException e) {
	            Log.w("Facebook-Example", "JSON Error in response");
	        } catch (FacebookError e) {
	            Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
	        }
	    }
	    	  
	    
	    /**
         * Httprequest to connect to server and post data
         * and receive and parse response
         */
        private void getData(String id)
        {
            Log.v("HTTP try to get", "data");
        	BufferedReader in = null;
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                try {
					request.setURI(new URI("http://kartikeyadubey.com/getItBackServer/getUserData.php?id="+id));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
                HttpResponse response = client.execute(request);
                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }
                in.close();
                String page = sb.toString();
                JSONArray money = null;
                JSONObject object = null;
				try {
					object = (JSONObject) new JSONTokener(page).nextValue();
	                money = object.getJSONArray(id);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

                
                try {
                	for(int i = 0; i < money.length(); i++)
                	{
                		Log.v("Got some data", money.getJSONObject(i).getString("money"));	
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
        }
	}

}



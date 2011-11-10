/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ssui.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import ssui.project.SessionEvents.AuthListener;
import ssui.project.SessionEvents.LogoutListener;


public class GetItBackActivity extends Activity {

    // Your Facebook Application ID must be set before running this example
    // See http://www.facebook.com/developers/createapp.php
    public static final String APP_ID = "161076967321553";

    private LoginButton mLoginButton;
    private TextView mText;
    private Button mRequestButton;
    private Button mPostButton;
    private Button mDeleteButton;
    private Button mUploadButton;

    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APP_ID == null) {
            Util.showAlert(this, "Warning", "Facebook Applicaton ID must be " +
                    "specified before running this example: see Example.java");
        }

        setContentView(R.layout.main);
        mLoginButton = (LoginButton) findViewById(R.id.login);
        mText = (TextView) GetItBackActivity.this.findViewById(R.id.txt);
        mRequestButton = (Button) findViewById(R.id.requestButton);
      
       	mFacebook = new Facebook(APP_ID);
       	mAsyncRunner = new AsyncFacebookRunner(mFacebook);

        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
        
        
        //check if mFacebook is logged in
        //if already logged in start pulling data
        //else show login in button
        if(mFacebook.isSessionValid()) Log.v("User", "Logged in");
        mLoginButton.init(this, mFacebook);

        mRequestButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	mAsyncRunner.request("me", new SampleRequestListener());
            }
        });
        mRequestButton.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
    }

    public class SampleAuthListener implements AuthListener {

        public void onAuthSucceed() {
            mText.setText("You have logged in! ");
            mRequestButton.setVisibility(View.VISIBLE);
        }

        public void onAuthFail(String error) {
            mText.setText("Login Failed: " + error);
        }
    }

    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
            mText.setText("Logging out...");
        }

        public void onLogoutFinish() {
        	Log.v("User has", "logged out");
            mText.setText("You have logged out! ");
            mRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    public class SampleRequestListener extends BaseRequestListener {

        public void onComplete(final String response, final Object state) {
            try {
                // process the response here: executed in background thread
                Log.d("Facebook-Example", "Response: " + response.toString());
                JSONObject json = Util.parseJson(response);
                final String name = json.getString("id");
                
                getData(name);
                
                // then post the processed result back to the UI thread
                // if we do not do this, an runtime exception will be generated
                // e.g. "CalledFromWrongThreadException: Only the original
                // thread that created a view hierarchy can touch its views."
                GetItBackActivity.this.runOnUiThread(new Runnable() {
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
					request.setURI(new URI("http://10.0.2.2/getItBackServer/getUserData.php?id="+id));
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
                Log.v("Got some data", page);
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

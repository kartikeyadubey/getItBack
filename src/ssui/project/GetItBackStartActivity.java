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

import com.facebook.android.Facebook;
import com.facebook.android.R;
import com.facebook.android.Util;

import facebook.LoginButton;
import facebook.LoginDialogListener;
import session.SessionEvents;
import session.SessionStore;
import session.SessionEvents.AuthListener;
import session.SessionEvents.LogoutListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class GetItBackStartActivity extends Activity {

    // Set facebook app id
    public static final String APP_ID = "161076967321553";

    private static final int REQUEST_OK = 1;
    
    //Login button
    private LoginButton mLoginButton;
    //Facebook object
    private Facebook mFacebook;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APP_ID == null) {
            Util.showAlert(this, "Warning", "Facebook Applicaton ID must be " +
                    "specified before running this example: see Example.java");
        }

        initialize();
    }
    
    /**
     * Initialize all variables
     * check if session is valid
     * if it is valid then start the StartPage activity
     * otherwise open a webview and make the user login
     */
    private void initialize()
    {
        //Create an instance of Facebook
        mFacebook = new Facebook(APP_ID);
        SessionStore.clear(getApplicationContext());
       	//Restore information from the facebook variable
        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());

        //check if mFacebook is logged in
        //if already logged in start pulling data
        //else show login in button
        if(mFacebook.isSessionValid() && mFacebook.getAccessExpires() != 0)
    	{
        	//Start activity to 
        	Log.v("User is", "Logged in");
        	Intent i = new Intent(this.getApplicationContext(), StartPage.class);
        	SessionStore.save(mFacebook, getApplicationContext());
        	startActivityForResult(i, REQUEST_OK);
    	}
        else
        {
        	Log.v("User is currently", "not logged in...");
        	mFacebook.authorize(this, new String[]{}, new LoginDialogListener());
        }
    }
    
    private void makeButton()
    {
    	if(mFacebook.isSessionValid()) Log.d("Making button", "Session is valid");
       	//Restore information from the facebook variable
        SessionStore.restore(mFacebook, getApplicationContext());
        
    	setContentView(R.layout.main);
        mLoginButton = (LoginButton) findViewById(R.id.login);
        mLoginButton.init(this, mFacebook);
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_OK)
        {
        	Log.d("Should make new", "button.");
        	makeButton();
        }
        else if(resultCode == RESULT_CANCELED)
        {
        	Log.d("Should create", "new intent");
        	//mFacebook.logout(getApplicationContext());
			//SessionStore.clear(getApplicationContext());
			//SessionStore.save(mFacebook, getApplicationContext());
			mFacebook.authorize(this, new String[]{}, new LoginDialogListener());
        }
    }


    public class SampleAuthListener implements AuthListener {

        public void onAuthSucceed() {
        	Log.v("Auth succeeded.", "...");
        	SessionStore.save(mFacebook, getApplicationContext());
        	Intent i = new Intent(getBaseContext(), StartPage.class);
        	startActivityForResult(i, REQUEST_OK);
        }

        public void onAuthFail(String error) {
        	
        }
    }
    
    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
        	Log.v("Main Activity Logging out...", "Complete");
        }

        public void onLogoutFinish() {
        	Log.v("Main activity Logging out...", "Complete");
        }
    }
}

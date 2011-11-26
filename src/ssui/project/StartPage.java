package ssui.project;

import com.facebook.android.Facebook;
import com.facebook.android.R;
import com.facebook.android.Util;

import facebook.LoginDialogListener;
import session.SessionEvents;
import session.SessionStore;
import session.SessionEvents.AuthListener;
import session.SessionEvents.LogoutListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity which creates the initial UI based on whether the user
 * is already signed in or not
 * @author Kartikeya
 *
 */
public class StartPage extends Activity {

    // Set facebook app id
    public static final String APP_ID = "161076967321553";

    private static final int REQUEST_OK = 1;
    protected static final int RETURN_FROM_TABS = 2;
    protected static final int MOVED_BACK = 3;
    
    //Login button
    private LoginButton mLoginButton;
    //Facebook object
    private Facebook mFacebook;
    private Button enterButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APP_ID == null) {
            Util.showAlert(this, "Warning", "Facebook Applicaton ID must be " +
                    "specified before running this example: see Example.java");
        }
        
        //Initialize User interface
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
        SessionEvents.addAuthListener(new AuthenticationListener());
        SessionEvents.addLogoutListener(new LogoutAuthListener());
        //check if mFacebook is logged in
        //if already logged in start pulling data
        //else show login in button
        if(mFacebook.isSessionValid() && mFacebook.getAccessExpires() != 0)
    	{
        	//Start actual application
        	Log.v("User is", "Logged in");
        	Intent i = new Intent(this.getApplicationContext(), TabDisplay.class);
        	SessionStore.save(mFacebook, getApplicationContext());
        	startActivityForResult(i, REQUEST_OK);
    	}
        else
        {
        	Log.v("User is currently", "not logged in...");
        	makeUI(false);
        	//mFacebook.authorize(this, new String[]{}, new LoginDialogListener());
        }
    }
    
    /**
     * Display the login button
     */
    private void makeUI(boolean isEnter)
    {
    	if(mFacebook.isSessionValid()) Log.d("Making button", "Session is valid");
       	//Restore information from the facebook variable
        SessionStore.restore(mFacebook, getApplicationContext());
        
    	setContentView(R.layout.main);
    	TextView txt = (TextView) findViewById(R.id.custom_font);
        Typeface font = Typeface.createFromAsset(getAssets(), "Arkitech_Light.otf");
        txt.setTypeface(font);
        mLoginButton = (LoginButton) findViewById(R.id.login);
        mLoginButton.init(this, mFacebook);
        if(isEnter && mFacebook.isSessionValid())
        {
        	enterButton = (Button) findViewById(R.id.Enter);
        	enterButton.setVisibility(Button.VISIBLE);
        	enterButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
		        	Intent i = new Intent(getApplicationContext(), TabDisplay.class);
		        	SessionStore.save(mFacebook, getApplicationContext());
		        	startActivityForResult(i, REQUEST_OK);
				}
			});
        }
    }
    
    /**
     * Wait for the TabHost to return a result
     * @param requestCode the code that is passed in when the activity was started
     * @param resultCode the code that is returned by the activity
     * @param data the data returned by the activity that finished
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
        Log.d("Activity has returned result", Integer.toString(resultCode));
        if(resultCode == RESULT_OK && requestCode == REQUEST_OK)
        {
        	Log.d("Should make new", "button.");
        	makeUI(false);
        }
        else if(resultCode == RESULT_CANCELED)
        {
        	Log.d("Should create", "new intent");
			mFacebook.authorize(this, new String[]{}, new LoginDialogListener());
        }
        else if(resultCode == RETURN_FROM_TABS)
        {
        	Log.d("Returning from tabs", "button");
        	makeUI(false);
        }
        else if(resultCode == MOVED_BACK)
        {
        	Log.d("Display Enter", "button");
        	makeUI(true);
        }
    }

    /**
     * Waits for facebook authentication to finish
     * after authentication has finished it starts a new activity to display the tabs
     */
    public class AuthenticationListener implements AuthListener {

        public void onAuthSucceed() {
        	Log.v("Auth succeeded.", "...");
        	SessionStore.save(mFacebook, getApplicationContext());
        	Intent i = new Intent(getApplicationContext(), TabDisplay.class);
        	startActivityForResult(i, REQUEST_OK);
        }

        public void onAuthFail(String error) {
        	
        }
    }
    
    /**
     * Logout listener just prints to log
     * when the user has logged out
     * @author Kartikeya
     *
     */
    public class LogoutAuthListener implements LogoutListener {
        public void onLogoutBegin() {
        	Log.v("Main Activity Logging out...", "Complete");
        }

        public void onLogoutFinish() {
        	Log.v("Main activity Logging out...", "Complete");
        	enterButton.setVisibility(Button.INVISIBLE);
        }
    }
}

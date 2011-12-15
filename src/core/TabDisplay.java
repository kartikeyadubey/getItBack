package core;

import session.SessionStore;
import ssui.project.R;

import com.facebook.android.Facebook;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/**
 * Class that creates tabs and starts the activity for each tab
 * @author Kartikeya
 *
 */
public class TabDisplay extends TabActivity {
	
	// Facebook object
	private Facebook mFacebook;
	// Set facebook app id
	public static final String APP_ID = "161076967321553";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab);
        
		mFacebook = new Facebook(APP_ID);
		SessionStore.restore(mFacebook, getApplicationContext());
		SessionStore.getUser();
        
		/* TabHost will have Tabs */
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
        
        //TabSpec used to create a new tab. 
        
        TabSpec firstTabSpec = tabHost.newTabSpec("tid1");
        TabSpec secondTabSpec = tabHost.newTabSpec("tid2");
        TabSpec thirdTabSpec = tabHost.newTabSpec("tid3");
        
        
        
        
        //TabSpec setIndicator() is used to set name for the tab
        //TabSpec setContent() is used to set content for a particular tab
        firstTabSpec.setIndicator("Add Bill", getResources().getDrawable(R.drawable.ic_tab_add)).setContent(new Intent(getApplicationContext(),AddBill.class));
        secondTabSpec.setIndicator("Who Owes me?", getResources().getDrawable(R.drawable.ic_tab_collect)).setContent(new Intent(getApplicationContext(), ViewBills.class));
        thirdTabSpec.setIndicator("Who do I owe?", getResources().getDrawable(R.drawable.ic_tab_return)).setContent(new Intent(getApplicationContext(), ViewReturns.class));
        
        /* Add tabSpec to the TabHost to display. */
        tabHost.addTab(firstTabSpec);
        tabHost.addTab(secondTabSpec);
        tabHost.addTab(thirdTabSpec);

    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) 
    {
    	Log.d("Got result code", Integer.toString(resultCode));
    	setResult(resultCode);
    	finish();
    }
}

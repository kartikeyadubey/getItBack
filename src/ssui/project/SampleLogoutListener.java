package ssui.project;

import ssui.project.SessionEvents.LogoutListener;
import android.util.Log;

public class SampleLogoutListener implements LogoutListener{
    public void onLogoutBegin() {
        Log.v("Logout", "Begun");
    }

    public void onLogoutFinish() {
    	Log.v("User has", "logged out");
    }
}

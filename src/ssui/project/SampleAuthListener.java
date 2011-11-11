package ssui.project;

import ssui.project.SessionEvents.AuthListener;
import android.util.Log;

public class SampleAuthListener implements AuthListener {

        public void onAuthSucceed() {
            Log.v("Login", "Successful");
        }

        public void onAuthFail(String error) {
            Log.v("Login", "failed");
        }
    }
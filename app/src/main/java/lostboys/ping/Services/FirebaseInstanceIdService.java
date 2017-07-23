package lostboys.ping.Services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by Asus on 17-Jul-17.
 */

public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh(){
        // Get updated InstanceID token
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(FirebaseInstanceIdService.class.getSimpleName(), "Refreshed token: " + refreshedToken);
    }


}

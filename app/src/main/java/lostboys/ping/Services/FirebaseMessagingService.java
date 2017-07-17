package lostboys.ping.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.RemoteMessage;
import lostboys.ping.MapsActivity;
import lostboys.ping.R;

/**
 * Created by Asus on 17-Jul-17.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("FMS", "onMessageReceived: " + remoteMessage.getMessageId());
        Intent resultIntent = new Intent(this, MapsActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification;
        if (remoteMessage.getNotification() != null) {
            // Firebase in foreground
            String sound = remoteMessage.getNotification().getSound();
            NotificationCompat.Builder builder = new  NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setSound(sound != null && !sound.isEmpty() && !sound.equals("default") ? Uri.parse(sound) : Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent);
            notification = builder.build();
        } else {
            // Mixpanel
            NotificationCompat.Builder builder = new  NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(remoteMessage.getData().get("mp_message"))
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent);
            notification = builder.build();
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(remoteMessage.hashCode(), notification);
    }
}

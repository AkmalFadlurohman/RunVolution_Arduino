package com.AlForce.android.runvolution.messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.AlForce.android.runvolution.LoginActivity;
import com.AlForce.android.runvolution.MainActivity;
import com.AlForce.android.runvolution.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by akmalfadlurohman on 21/02/18.
 */

public class FCMService extends FirebaseMessagingService {
    private String LOG_TAG = FCMService.class.getSimpleName();


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(LOG_TAG, "Message data payload: " + remoteMessage.getData());
            Map data = remoteMessage.getData();
            String title = (String) data.get("title");
            String content = (String) data.get("content");
            sendNotification(title, content);
        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(LOG_TAG, "Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(LOG_TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            //sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void sendNotification(String messageTitle, String messageBody) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "my_channel_id_01";
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }

}

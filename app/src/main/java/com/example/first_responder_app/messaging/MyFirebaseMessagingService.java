package com.example.first_responder_app.messaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.first_responder_app.MainActivity;
import com.example.first_responder_app.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            generateNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
        remoteMessage.getNotification().getBody();
    }

    public void generateNotification(String title, String msg) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bypassDND = prefs.getBoolean("bypass", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder;
        if (bypassDND == true) {
            builder = new NotificationCompat.Builder(getApplicationContext(), "n")
                    .setSmallIcon(R.drawable.circle)
                    .setContentTitle("N")
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setContentIntent(pendingIntent);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), "n")
                    .setSmallIcon(R.drawable.circle)
                    .setContentTitle("N")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent);
        }


        builder = builder.setContent(getRemoteView(title, msg));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("n", "e", NotificationManager.IMPORTANCE_HIGH);

            if (bypassDND == true) {
                channel.canBypassDnd();
            }

            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, builder.build());

    }

    public RemoteViews getRemoteView(String title, String msg) {
        RemoteViews remoteView = new RemoteViews("com.example.first_responder_app", R.layout.notification);
        remoteView.setTextViewText(R.id.notification_title, title);
        remoteView.setTextViewText(R.id.notification_description, msg);
        remoteView.setImageViewResource(R.id.notification_logo, R.drawable.circle);

        return remoteView;
    }


}

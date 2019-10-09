package com.jimmy.skripsi.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.activities.ChatActivity;
import com.jimmy.skripsi.helpers.Gxon;
import com.jimmy.skripsi.models.ChatModel;

import org.json.JSONException;
import org.json.JSONObject;

public class FcmService extends FirebaseMessagingService {
    private static final String TAG = FcmService.class.getSimpleName();
    private static final String CHANNEL_ID = "Agenda";
    private static final String CHANNEL_NAME = "AgendaChannel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            JSONObject data = null;
            try {
                data = new JSONObject(remoteMessage.getData().get("data"));

                ChatModel msg = Gxon.from(data.toString(), ChatModel.class);
                sendNotification(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
       // Util.showToast(getApplicationContext(), s);
    }

    private void sendNotification(ChatModel message) {
        Intent intent = ChatActivity.fromNotif(this, Gxon.to(message));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int unique_id = (int) System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(message.getName())
                .setContentText(message.getMessage())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationManager.notify(unique_id, notificationBuilder.build());
    }
}

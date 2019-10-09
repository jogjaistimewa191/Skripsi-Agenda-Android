package com.jimmy.skripsi.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.jimmy.skripsi.R;
import com.jimmy.skripsi.activities.HomeActivity;

import java.util.Random;


public class AlarmService extends IntentService {

    private static final String NOTIFICATION_CHANNEL_ID = "ChannelAgenda";

    public AlarmService() {
        super("AlarmService");
        setIntentRedelivery(true);

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onHandleIntent(Intent intent) {
     showNotification("PENGINGAT !, CEK KEMBALI AGENDA YANG ADA", "Sebentar Lagi Ada Acara Yang Akan Segera Dimulai !");
    }

    private void showNotification(String title, String body) {
        createChannel();
        Notification mNotification;
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            mNotification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_alarm_on_black_24dp))
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(body))
                    .setContentText(body).build();
        } else {
            mNotification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_alarm_on_black_24dp))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentTitle(title)
                    .setContentText(body).build();
        }
        int id = new Random().nextInt();
        notificationManager.notify(id, mNotification);
        startForeground(1, mNotification);
    }

    private void createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Agenda", NotificationManager.IMPORTANCE_HIGH);
            mChannel.setShowBadge(true);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{500, 500, 500, 500, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);
        }
    }

}
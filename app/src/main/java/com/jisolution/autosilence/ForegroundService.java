package com.jisolution.autosilence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {
    public ForegroundService() {
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        createChannel();
//        Intent notificationIntent = new Intent(this, MapsActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 102, notificationIntent, 0);
//        Notification notification;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            notification = new NotificationCompat.Builder(this, "1001")
//                    .setContentTitle("Auto Silent")
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setContentText("Listening for your place")
//                    .setSmallIcon(R.drawable.icon2)
//                    .setContentIntent(pendingIntent)
//                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                    .build();
//        }else{
//            notification = new Notification.Builder(this)
//                    .setContentTitle("Auto Silent")
//                    .setContentText("Listening for your place")
//                    .setSmallIcon(R.drawable.icon2)
//                    .setContentIntent(pendingIntent)
//                    .build();
//        }
//        startForeground(1002, notification);
//        return START_NOT_STICKY;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.icon2)
                .setContentTitle("Auto Silent needs your location")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public void createChannel() {
        String CHANNEL_ID = "com.example.notifications.ForegroundService";
        NotificationChannel notificationChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, "ForegroundService", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setDescription("this is channel is for ForegroundService.");
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            Log.d("Channel", "Channel created");
        }
    }
}
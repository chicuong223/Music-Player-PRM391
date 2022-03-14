package com.chicuong.audioplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

public class ApplicationClass extends Application {
    public static final String CHANNEL_ID_1 = "channel1";
    public static final String CHANNEL_ID_2 = "channel2";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_NEXT = "actionnext";
    public static final String ACTION_PLAY = "actionplay";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Toast.makeText(this, "Terminated", Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 =
                    new NotificationChannel(CHANNEL_ID_1
                    , "Channel(1)", NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel channel2 =
                    new NotificationChannel(CHANNEL_ID_2
                            , "Channel(2)", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Channel 2 Desc...");
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);
        }
    }
}

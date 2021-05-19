package com.atcnetz.de.notification;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.Objects;


public class NifityService extends NotificationListenerService {
    public static String TAG = "Test";
    private LocalBroadcastManager localBroadcastManager;
    final String SERVICE_RESULT = "com.service.result";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        Log.i(TAG, "ID:" + sbn.getId());
        Log.i(TAG, "Posted by:" + sbn.getPackageName());
        Log.i(TAG, "tickerText:" + sbn.getNotification().tickerText);

        String PackageName1 = "";
        String tickerText1 = "";
        String Text1 = "";
        String Title1 = "";
        if (sbn.getPackageName() != null) PackageName1 = sbn.getPackageName();
        if (sbn.getNotification().tickerText != null)
            tickerText1 = sbn.getNotification().tickerText.toString();
        if (sbn.getNotification().extras.get("android.text") != null)
            Text1 = Objects.requireNonNull(sbn.getNotification().extras.get("android.text")).toString();
        if (sbn.getNotification().extras.get("android.title") != null)
            Title1 = Objects.requireNonNull(sbn.getNotification().extras.get("android.title")).toString();

        for (String key : sbn.getNotification().extras.keySet()) {
            String NotifiMessage = "Nothing";
            if (sbn.getNotification().extras.get(key) != null)
                NotifiMessage = Objects.requireNonNull(sbn.getNotification().extras.get(key)).toString();
            Log.i(TAG, key + "=" + NotifiMessage);
        }

        if(PackageName1.equals("com.whatsapp")&&sbn.getId()==11)return;// prevent showing search for new Messages from WhatsApp
        Intent intent = new Intent(SERVICE_RESULT);
        intent.putExtra("IDnr", sbn.getId());
        intent.putExtra("PackageName1", PackageName1);
        intent.putExtra("tickerText1", tickerText1);
        intent.putExtra("Text1", Text1);
        intent.putExtra("Title1", Title1);
        if(isMyServiceRunning() && prefs.getBoolean("isNotificationEnabled", true))localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        if(!isMyServiceRunning() && prefs.getBoolean("isNotificationEnabled", true))startService(new Intent(this, BLEservice.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BLEservice.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

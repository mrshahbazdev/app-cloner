package com.titanclone.engine.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * Virtual Server — runs in the :x process.
 * Central service manager handling:
 * - System service registrations for clone processes
 * - IPC routing between clones
 * - Clone lifecycle management
 * - Virtual PackageManager queries
 *
 * This service starts as a foreground service to prevent
 * the system from killing it while clones are running.
 */
public class VirtualServerService extends Service {

    private static final String TAG = "VirtualServer";
    private static final String CHANNEL_ID = "titanclone_server";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Virtual Server starting in :x process");

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());

        // Initialize the engine in the server process
        VirtualCore.get().doAttachBaseContext(this);
        VirtualCore.get().doCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return Binder interface for clone processes to communicate
        // with the virtual server for service lookups, intent routing, etc.
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "Virtual Server shutting down");
        VirtualCore.get().getProcessManager().killAll();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "TitanClone Engine",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Keeps clone processes running");
            channel.setShowBadge(false);

            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
                .setContentTitle("TitanClone")
                .setContentText("Clone engine running")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setOngoing(true)
                .build();
    }
}

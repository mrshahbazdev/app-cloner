package com.titanclone.titan_clone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Persistent foreground service that keeps clone processes alive.
 *
 * Android aggressively kills background processes, especially on OEM
 * ROMs (MIUI, ColorOS, OneUI). This service maintains a persistent
 * notification showing the number of running clones and prevents
 * the system from killing clone processes.
 *
 * Required on Android 14+ with foregroundServiceType="specialUse".
 */
class CloneForegroundService : Service() {

    companion object {
        private const val TAG = "CloneFGService"
        private const val CHANNEL_ID = "titan_clone_service"
        private const val CHANNEL_NAME = "TitanClone Service"
        private const val NOTIFICATION_ID = 1001
        private const val WAKELOCK_TAG = "TitanClone:CloneService"

        const val ACTION_START = "com.titanclone.action.START_SERVICE"
        const val ACTION_STOP = "com.titanclone.action.STOP_SERVICE"
        const val ACTION_UPDATE = "com.titanclone.action.UPDATE_NOTIFICATION"
        const val EXTRA_RUNNING_COUNT = "running_count"
        const val EXTRA_CLONE_NAMES = "clone_names"

        fun start(context: Context, runningCount: Int, cloneNames: List<String> = emptyList()) {
            val intent = Intent(context, CloneForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RUNNING_COUNT, runningCount)
                putStringArrayListExtra(EXTRA_CLONE_NAMES, ArrayList(cloneNames))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CloneForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun updateNotification(context: Context, runningCount: Int, cloneNames: List<String> = emptyList()) {
            val intent = Intent(context, CloneForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_RUNNING_COUNT, runningCount)
                putStringArrayListExtra(EXTRA_CLONE_NAMES, ArrayList(cloneNames))
            }
            context.startService(intent)
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var runningCount = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "Foreground service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                runningCount = intent.getIntExtra(EXTRA_RUNNING_COUNT, 0)
                val names = intent.getStringArrayListExtra(EXTRA_CLONE_NAMES) ?: arrayListOf()
                startForeground(NOTIFICATION_ID, buildNotification(runningCount, names))
                acquireWakeLock()
                Log.d(TAG, "Service started with $runningCount running clones")
            }
            ACTION_UPDATE -> {
                runningCount = intent.getIntExtra(EXTRA_RUNNING_COUNT, 0)
                val names = intent.getStringArrayListExtra(EXTRA_CLONE_NAMES) ?: arrayListOf()
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIFICATION_ID, buildNotification(runningCount, names))

                if (runningCount == 0) {
                    releaseWakeLock()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                Log.d(TAG, "Service stopped")
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps clone processes running in the background"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(count: Int, names: List<String>): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val title = if (count > 0) "TitanClone: $count app${if (count > 1) "s" else ""} running"
                    else "TitanClone: Ready"

        val text = if (names.isNotEmpty()) names.joinToString(", ")
                   else "Tap to manage your clones"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKELOCK_TAG
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes max, renewed on updates
            }
            Log.d(TAG, "WakeLock acquired")
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "WakeLock released")
            }
        }
        wakeLock = null
    }
}

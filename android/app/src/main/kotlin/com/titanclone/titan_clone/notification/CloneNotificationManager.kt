package com.titanclone.titan_clone.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

class CloneNotificationManager(private val context: Context) {

    companion object {
        private const val TAG = "CloneNotification"
        private const val CHANNEL_ID_PREFIX = "clone_"
        private const val FOREGROUND_CHANNEL_ID = "titan_clone_service"
        private const val FOREGROUND_CHANNEL_NAME = "TitanClone Service"
    }

    fun createForegroundChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                FOREGROUND_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps clone processes running in the background"
                setShowBadge(false)
            }

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun createCloneChannel(cloneId: String, appName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "$CHANNEL_ID_PREFIX$cloneId"
            val channel = NotificationChannel(
                channelId,
                "Clone: $appName",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for clone: $appName"
            }

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun rewriteNotificationForClone(
        cloneId: String,
        appName: String,
        notification: Notification
    ): Notification {
        // TODO: Implement notification rewriting for clone process
        // 1. Modify notification tag with clone prefix
        // 2. Wrap PendingIntent to route through engine
        // 3. Add clone badge/label
        Log.d(TAG, "Rewriting notification for clone: $cloneId ($appName)")
        return notification
    }

    fun deleteCloneChannel(cloneId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "$CHANNEL_ID_PREFIX$cloneId"
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.deleteNotificationChannel(channelId)
        }
    }
}

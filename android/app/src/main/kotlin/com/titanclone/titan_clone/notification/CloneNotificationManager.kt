package com.titanclone.titan_clone.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Manages notification channels and routing for clone processes.
 *
 * Each clone gets its own notification channel so the user can
 * independently control notifications per clone. When a cloned app
 * posts a notification, this manager:
 *   1. Rewrites the notification tag with a clone prefix
 *   2. Wraps the PendingIntent to route through the engine
 *   3. Adds a clone badge/label so the user knows which clone it's from
 *   4. Forwards to the real NotificationManager
 *
 * Notification flow:
 *   Clone app → NotificationManagerStub → intercept → rewrite → system tray
 *   User taps → our PendingIntent → engine → correct clone :pN process
 */
class CloneNotificationManager(private val context: Context) {

    companion object {
        private const val TAG = "CloneNotification"
        private const val CHANNEL_ID_PREFIX = "clone_"
        private const val FOREGROUND_CHANNEL_ID = "titan_clone_service"
        private const val FOREGROUND_CHANNEL_NAME = "TitanClone Service"
        private const val NOTIFICATION_TAG_PREFIX = "tc_clone_"
    }

    private val cloneChannels = mutableSetOf<String>()

    /**
     * Create the foreground service notification channel.
     */
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

    /**
     * Create a notification channel for a specific clone.
     */
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
            cloneChannels.add(channelId)
        }
    }

    /**
     * Rewrite a notification from a cloned app to include clone identity.
     *
     * This wraps the original notification with:
     *   - Clone-specific notification tag (prevents collisions between clones)
     *   - Wrapped PendingIntent that routes through our engine
     *   - Clone badge label in the notification content
     */
    fun rewriteNotificationForClone(
        cloneId: String,
        appName: String,
        notification: Notification
    ): RewrittenNotification {
        val channelId = "$CHANNEL_ID_PREFIX$cloneId"
        val tag = "$NOTIFICATION_TAG_PREFIX$cloneId"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(notification.smallIcon?.resId ?: android.R.drawable.ic_popup_reminder)
            .setSubText("Clone: $appName")
            .setWhen(notification.`when`)
            .setAutoCancel(true)
            .setGroup("clone_$cloneId")

        // Copy content from original notification extras
        val extras = notification.extras
        extras?.getString(Notification.EXTRA_TITLE)?.let {
            builder.setContentTitle(it)
        }
        extras?.getString(Notification.EXTRA_TEXT)?.let {
            builder.setContentText(it)
        }
        extras?.getString(Notification.EXTRA_BIG_TEXT)?.let {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(it))
        }

        // Wrap the content intent to route through our engine
        notification.contentIntent?.let { originalIntent ->
            val wrapperIntent = createCloneRoutingIntent(cloneId, originalIntent)
            builder.setContentIntent(wrapperIntent)
        }

        Log.d(TAG, "Rewrote notification for clone: $cloneId ($appName)")
        return RewrittenNotification(
            notification = builder.build(),
            tag = tag,
            id = cloneId.hashCode()
        )
    }

    /**
     * Post a rewritten notification to the system tray.
     */
    fun postNotification(rewritten: RewrittenNotification) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(rewritten.tag, rewritten.id, rewritten.notification)
    }

    /**
     * Cancel all notifications for a specific clone.
     */
    fun cancelCloneNotifications(cloneId: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val tag = "$NOTIFICATION_TAG_PREFIX$cloneId"
        nm.cancel(tag, cloneId.hashCode())
        Log.d(TAG, "Cancelled notifications for clone $cloneId")
    }

    /**
     * Delete a clone's notification channel.
     */
    fun deleteCloneChannel(cloneId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "$CHANNEL_ID_PREFIX$cloneId"
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.deleteNotificationChannel(channelId)
            cloneChannels.remove(channelId)
        }
    }

    /**
     * Get all registered clone notification channel IDs.
     */
    fun getCloneChannels(): Set<String> {
        return cloneChannels.toSet()
    }

    /**
     * Create a PendingIntent that routes notification tap through the engine
     * to launch the correct clone process.
     */
    private fun createCloneRoutingIntent(
        cloneId: String,
        originalIntent: PendingIntent
    ): PendingIntent {
        val routingIntent = Intent(context, CloneNotificationRouter::class.java).apply {
            putExtra("cloneId", cloneId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            context,
            cloneId.hashCode(),
            routingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    data class RewrittenNotification(
        val notification: Notification,
        val tag: String,
        val id: Int
    )
}

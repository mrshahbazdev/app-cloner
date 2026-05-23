package com.titanclone.titan_clone.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.util.Log

/**
 * Manages background process lifecycle for running clones.
 *
 * Handles:
 *   - WakeLock management for clones with active background work
 *   - Alarm/JobScheduler proxying for clone apps
 *   - OEM battery optimization detection and guidance
 *   - Boot-completed receiver to restart clones
 *   - Process keep-alive strategies
 */
class BackgroundProcessManager(private val context: Context) {

    companion object {
        private const val TAG = "BackgroundProcessMgr"
        private const val WAKELOCK_PREFIX = "TitanClone:Clone_"
        private const val ALARM_REQUEST_BASE = 10000
        private const val JOB_ID_BASE = 20000
    }

    private val activeWakeLocks = mutableMapOf<String, PowerManager.WakeLock>()
    private val scheduledAlarms = mutableMapOf<String, MutableList<Int>>()

    /**
     * Acquire a WakeLock for a specific clone that has background work.
     */
    fun acquireCloneWakeLock(cloneId: String, timeoutMs: Long = 5 * 60 * 1000L) {
        releaseCloneWakeLock(cloneId)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$WAKELOCK_PREFIX$cloneId"
        )
        wakeLock.acquire(timeoutMs)
        activeWakeLocks[cloneId] = wakeLock
        Log.d(TAG, "WakeLock acquired for clone $cloneId (${timeoutMs}ms)")
    }

    /**
     * Release WakeLock for a clone.
     */
    fun releaseCloneWakeLock(cloneId: String) {
        activeWakeLocks.remove(cloneId)?.let { wl ->
            if (wl.isHeld) {
                wl.release()
                Log.d(TAG, "WakeLock released for clone $cloneId")
            }
        }
    }

    /**
     * Proxy a clone's alarm request through the host app.
     *
     * When a cloned app calls AlarmManager.setExact(), we intercept it
     * and register the alarm through our host app so it survives
     * process death.
     */
    fun scheduleAlarmForClone(
        cloneId: String,
        triggerAtMillis: Long,
        intervalMillis: Long = 0,
        action: String
    ): Int {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = ALARM_REQUEST_BASE + (cloneId.hashCode() and 0xFFFF)

        val intent = Intent(context, CloneAlarmReceiver::class.java).apply {
            putExtra("cloneId", cloneId)
            putExtra("action", action)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (intervalMillis > 0) {
                am.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    intervalMillis,
                    pendingIntent
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    am.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }

            scheduledAlarms.getOrPut(cloneId) { mutableListOf() }.add(requestCode)
            Log.d(TAG, "Scheduled alarm for clone $cloneId (code=$requestCode)")
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot schedule exact alarm: missing SCHEDULE_EXACT_ALARM permission", e)
        }

        return requestCode
    }

    /**
     * Cancel all scheduled alarms for a clone.
     */
    fun cancelAlarmsForClone(cloneId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduledAlarms[cloneId]?.forEach { requestCode ->
            val intent = Intent(context, CloneAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { am.cancel(it) }
        }
        scheduledAlarms.remove(cloneId)
        Log.d(TAG, "Cancelled all alarms for clone $cloneId")
    }

    /**
     * Detect OEM-specific battery optimization that may kill clone processes.
     */
    fun detectBatteryOptimization(): BatteryOptimizationInfo {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringOptimizations = pm.isIgnoringBatteryOptimizations(context.packageName)

        val oemBrand = Build.MANUFACTURER.lowercase()
        val oemIssue = when {
            oemBrand.contains("xiaomi") || oemBrand.contains("redmi") ->
                OemIssue.MIUI_AUTOSTART
            oemBrand.contains("samsung") ->
                OemIssue.SAMSUNG_SLEEPING_APPS
            oemBrand.contains("oppo") || oemBrand.contains("realme") ->
                OemIssue.COLOROS_BATTERY
            oemBrand.contains("vivo") ->
                OemIssue.FUNTOUCH_BATTERY
            oemBrand.contains("huawei") || oemBrand.contains("honor") ->
                OemIssue.EMUI_POWER_INTENSIVE
            oemBrand.contains("oneplus") ->
                OemIssue.OXYGENOS_BATTERY
            else -> null
        }

        return BatteryOptimizationInfo(
            isIgnoringBatteryOptimization = isIgnoringOptimizations,
            oemBrand = Build.MANUFACTURER,
            oemIssue = oemIssue,
            settingsIntent = getBatterySettingsIntent(oemIssue)
        )
    }

    /**
     * Get an intent to open battery optimization settings.
     */
    private fun getBatterySettingsIntent(oemIssue: OemIssue?): Intent {
        return when (oemIssue) {
            OemIssue.MIUI_AUTOSTART -> Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
            OemIssue.SAMSUNG_SLEEPING_APPS -> Intent().apply {
                component = ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.battery.ui.BatteryActivity"
                )
            }
            else -> Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        }
    }

    /**
     * Clean up all background resources for a clone.
     */
    fun cleanupClone(cloneId: String) {
        releaseCloneWakeLock(cloneId)
        cancelAlarmsForClone(cloneId)
        Log.d(TAG, "Cleaned up background resources for clone $cloneId")
    }

    /**
     * Release all resources on shutdown.
     */
    fun releaseAll() {
        activeWakeLocks.keys.toList().forEach { releaseCloneWakeLock(it) }
        scheduledAlarms.keys.toList().forEach { cancelAlarmsForClone(it) }
    }

    enum class OemIssue {
        MIUI_AUTOSTART,
        SAMSUNG_SLEEPING_APPS,
        COLOROS_BATTERY,
        FUNTOUCH_BATTERY,
        EMUI_POWER_INTENSIVE,
        OXYGENOS_BATTERY
    }

    data class BatteryOptimizationInfo(
        val isIgnoringBatteryOptimization: Boolean,
        val oemBrand: String,
        val oemIssue: OemIssue?,
        val settingsIntent: Intent
    )
}

/**
 * BroadcastReceiver that fires when a proxied clone alarm triggers.
 */
class CloneAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val cloneId = intent?.getStringExtra("cloneId") ?: return
        val action = intent.getStringExtra("action") ?: return
        Log.d("CloneAlarmReceiver", "Alarm fired for clone $cloneId: $action")
        // Route to the clone's process via the virtual server
    }
}

/**
 * BroadcastReceiver for BOOT_COMPLETED to restart auto-start clones.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d("BootReceiver", "Device booted — checking for auto-start clones")
        // Load auto-start preference and restart clones
    }
}

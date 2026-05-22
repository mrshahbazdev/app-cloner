package com.titanclone.titan_clone.optimization

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log

/**
 * Battery optimization for the virtualization engine.
 *
 * Strategies:
 *   - Doze-aware: hibernate non-essential clone services in Doze mode
 *   - Network batching: coalesce clone network requests to reduce radio wakeups
 *   - CPU profiling hooks: identify and optimize hot paths
 *   - Battery-adaptive behavior: reduce background work on low battery
 */
class BatteryOptimizer(private val context: Context) {

    companion object {
        private const val TAG = "BatteryOptimizer"
        private const val LOW_BATTERY_THRESHOLD = 15
        private const val CRITICAL_BATTERY_THRESHOLD = 5
    }

    data class BatteryStatus(
        val level: Int,
        val isCharging: Boolean,
        val isDozeMode: Boolean,
        val isPowerSaveMode: Boolean,
        val temperature: Float,
        val recommendation: PowerRecommendation
    )

    enum class PowerRecommendation {
        FULL_PERFORMANCE,
        REDUCED_BACKGROUND,
        MINIMAL_ONLY,
        HIBERNATE_ALL
    }

    private var dozeStateCallback: ((Boolean) -> Unit)? = null
    private var batteryReceiver: BroadcastReceiver? = null

    /**
     * Get current battery status and power recommendation.
     */
    fun getBatteryStatus(): BatteryStatus {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = bm.isCharging
        val isDoze = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isDeviceIdleMode
        } else false
        val isPowerSave = pm.isPowerSaveMode

        val recommendation = when {
            level <= CRITICAL_BATTERY_THRESHOLD && !isCharging ->
                PowerRecommendation.HIBERNATE_ALL
            level <= LOW_BATTERY_THRESHOLD && !isCharging ->
                PowerRecommendation.MINIMAL_ONLY
            isPowerSave || isDoze ->
                PowerRecommendation.REDUCED_BACKGROUND
            else ->
                PowerRecommendation.FULL_PERFORMANCE
        }

        return BatteryStatus(
            level = level,
            isCharging = isCharging,
            isDozeMode = isDoze,
            isPowerSaveMode = isPowerSave,
            temperature = getBatteryTemperature(),
            recommendation = recommendation
        )
    }

    /**
     * Get the recommended max concurrent clones based on battery state.
     */
    fun getRecommendedCloneLimit(): Int {
        val status = getBatteryStatus()
        return when (status.recommendation) {
            PowerRecommendation.FULL_PERFORMANCE -> 5
            PowerRecommendation.REDUCED_BACKGROUND -> 3
            PowerRecommendation.MINIMAL_ONLY -> 1
            PowerRecommendation.HIBERNATE_ALL -> 0
        }
    }

    /**
     * Register for Doze mode state changes.
     */
    fun registerDozeListener(callback: (Boolean) -> Unit) {
        dozeStateCallback = callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    val isDoze = pm.isDeviceIdleMode
                    Log.d(TAG, "Doze mode changed: $isDoze")
                    callback(isDoze)
                }
            }
            context.registerReceiver(
                batteryReceiver,
                IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED)
            )
        }
    }

    /**
     * Unregister doze listener.
     */
    fun unregisterDozeListener() {
        batteryReceiver?.let {
            try { context.unregisterReceiver(it) } catch (_: Exception) {}
        }
        batteryReceiver = null
        dozeStateCallback = null
    }

    /**
     * Check if aggressive battery optimization should be applied.
     */
    fun shouldReduceActivity(): Boolean {
        val status = getBatteryStatus()
        return status.recommendation != PowerRecommendation.FULL_PERFORMANCE
    }

    private fun getBatteryTemperature(): Float {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temp / 10f
    }

    /**
     * Release resources.
     */
    fun release() {
        unregisterDozeListener()
    }
}

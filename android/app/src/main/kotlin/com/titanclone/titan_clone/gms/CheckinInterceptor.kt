package com.titanclone.titan_clone.gms

import android.content.Context
import android.os.Build
import android.util.Log
import com.titanclone.titan_clone.profile.VirtualProfileManager
import java.io.File

/**
 * Intercepts device checkin requests to Google servers so each clone
 * registers as a unique, independent device.
 *
 * When a clone's GMS performs a device checkin (checkin.googleapis.com),
 * this interceptor injects the clone's virtual device profile into the
 * checkin payload — making Google see a completely different device.
 *
 * Key fields spoofed in the checkin:
 *   - Build.MODEL, Build.MANUFACTURER, Build.BRAND
 *   - Android ID, GSF ID
 *   - IMEI / MEID
 *   - Screen density, width, height
 *   - Locale, timezone
 *   - GL extensions, CPU ABI
 */
class CheckinInterceptor(
    private val context: Context,
    private val profileManager: VirtualProfileManager
) {

    companion object {
        private const val TAG = "CheckinInterceptor"
        const val CHECKIN_URL = "https://android.clients.google.com/checkin"
        const val CHECKIN_HOST = "android.clients.google.com"
    }

    data class CheckinPayload(
        val androidId: String,
        val gsfId: String,
        val deviceModel: String,
        val deviceBrand: String,
        val deviceManufacturer: String,
        val buildProduct: String,
        val buildFingerprint: String,
        val sdkVersion: Int,
        val screenDensity: Int,
        val screenWidth: Int,
        val screenHeight: Int,
        val locale: String,
        val timezone: String,
        val imei: String,
        val macAddress: String,
        val serialNumber: String,
        val glEsVersion: String
    )

    /**
     * Build a checkin payload from the clone's virtual profile.
     */
    fun buildCheckinPayload(cloneId: String, gmsConfig: GmsProxyConfig): CheckinPayload? {
        val profile = profileManager.loadProfile(cloneId, 0) ?: run {
            Log.e(TAG, "No profile found for clone $cloneId")
            return null
        }

        return CheckinPayload(
            androidId = profile.getString("androidId") ?: "",
            gsfId = gmsConfig.gsfId,
            deviceModel = profile.getString("model") ?: Build.MODEL,
            deviceBrand = profile.getString("brand") ?: Build.BRAND,
            deviceManufacturer = profile.getString("manufacturer") ?: Build.MANUFACTURER,
            buildProduct = profile.getString("product") ?: Build.PRODUCT,
            buildFingerprint = profile.getString("fingerprint") ?: Build.FINGERPRINT,
            sdkVersion = Build.VERSION.SDK_INT,
            screenDensity = context.resources.displayMetrics.densityDpi,
            screenWidth = profile.getInt("screenWidth", context.resources.displayMetrics.widthPixels),
            screenHeight = profile.getInt("screenHeight", context.resources.displayMetrics.heightPixels),
            locale = profile.getString("locale") ?: "en_US",
            timezone = profile.getString("timezone") ?: "America/New_York",
            imei = profile.getString("imei") ?: "",
            macAddress = profile.getString("macAddress") ?: "",
            serialNumber = profile.getString("serialNumber") ?: "",
            glEsVersion = "0x00030002"  // OpenGL ES 3.2
        )
    }

    /**
     * Store the device registration token returned by Google after checkin.
     */
    fun storeCheckinResult(cloneId: String, registrationId: String, securityToken: String) {
        try {
            val checkinFile = File(
                context.filesDir,
                "clones/$cloneId/gms/checkin_result.json"
            )
            checkinFile.parentFile?.mkdirs()
            val json = buildString {
                append("{")
                append("\"registrationId\":\"$registrationId\",")
                append("\"securityToken\":\"$securityToken\",")
                append("\"timestamp\":${System.currentTimeMillis()}")
                append("}")
            }
            checkinFile.writeText(json)
            Log.d(TAG, "Stored checkin result for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store checkin result for clone $cloneId", e)
        }
    }

    /**
     * Load the stored checkin registration for a clone.
     */
    fun getCheckinResult(cloneId: String): Pair<String, String>? {
        return try {
            val checkinFile = File(
                context.filesDir,
                "clones/$cloneId/gms/checkin_result.json"
            )
            if (!checkinFile.exists()) return null

            val json = checkinFile.readText()
            // Simple JSON parsing
            val regId = Regex("\"registrationId\":\"([^\"]+)\"").find(json)?.groupValues?.get(1)
            val token = Regex("\"securityToken\":\"([^\"]+)\"").find(json)?.groupValues?.get(1)
            if (regId != null && token != null) Pair(regId, token) else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load checkin result for clone $cloneId", e)
            null
        }
    }

    /**
     * Check if a clone has completed device checkin with Google.
     */
    fun hasCheckedIn(cloneId: String): Boolean {
        return getCheckinResult(cloneId) != null
    }

    /**
     * Clear checkin data for a clone (forces re-registration).
     */
    fun clearCheckinData(cloneId: String) {
        try {
            val checkinFile = File(
                context.filesDir,
                "clones/$cloneId/gms/checkin_result.json"
            )
            checkinFile.delete()
            Log.d(TAG, "Cleared checkin data for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear checkin data for clone $cloneId", e)
        }
    }
}

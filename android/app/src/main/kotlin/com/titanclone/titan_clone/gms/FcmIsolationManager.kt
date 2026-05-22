package com.titanclone.titan_clone.gms

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Manages per-clone FCM (Firebase Cloud Messaging) registration isolation.
 *
 * Each clone must register independently with FCM so push notifications
 * route to the correct clone process. Without isolation, all clones
 * would share the same FCM registration token, causing notification
 * delivery to the wrong clone.
 *
 * Architecture:
 *   Clone boots → GMS initializes → FCM registers with Google →
 *   Registration token stored per-clone → push arrives →
 *   virtual BroadcastReceiver in :x routes to correct :pN process
 */
class FcmIsolationManager(private val context: Context) {

    companion object {
        private const val TAG = "FcmIsolation"
        private const val FCM_DIR = "fcm"
    }

    data class FcmRegistration(
        val cloneId: String,
        val registrationToken: String,
        val senderId: String,
        val appPackage: String,
        val registeredAt: Long
    )

    private val registrations = mutableMapOf<String, MutableList<FcmRegistration>>()

    /**
     * Initialize FCM isolation for a new clone.
     */
    fun initializeForClone(cloneId: String) {
        try {
            getFcmDir(cloneId).mkdirs()
            registrations.getOrPut(cloneId) { mutableListOf() }
            Log.d(TAG, "Initialized FCM isolation for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FCM for $cloneId", e)
        }
    }

    /**
     * Store an FCM registration token for a clone.
     * Called when a cloned app's FirebaseInstanceIdService generates a token.
     */
    fun registerToken(
        cloneId: String,
        appPackage: String,
        senderId: String,
        token: String
    ) {
        val registration = FcmRegistration(
            cloneId = cloneId,
            registrationToken = token,
            senderId = senderId,
            appPackage = appPackage,
            registeredAt = System.currentTimeMillis()
        )

        val cloneRegs = registrations.getOrPut(cloneId) { mutableListOf() }
        cloneRegs.removeAll { it.appPackage == appPackage && it.senderId == senderId }
        cloneRegs.add(registration)

        persistRegistration(cloneId, registration)
        Log.d(TAG, "Registered FCM token for $appPackage in clone $cloneId")
    }

    /**
     * Get the FCM registration token for a specific app in a clone.
     */
    fun getToken(cloneId: String, appPackage: String): String? {
        return registrations[cloneId]?.find { it.appPackage == appPackage }?.registrationToken
    }

    /**
     * Find which clone a push notification should be routed to
     * based on the registration token.
     */
    fun findCloneByToken(registrationToken: String): String? {
        for ((cloneId, regs) in registrations) {
            if (regs.any { it.registrationToken == registrationToken }) {
                return cloneId
            }
        }
        return null
    }

    /**
     * Unregister FCM for a specific app in a clone.
     */
    fun unregisterToken(cloneId: String, appPackage: String) {
        registrations[cloneId]?.removeAll { it.appPackage == appPackage }
        try {
            val tokenFile = File(getFcmDir(cloneId), "${appPackage}.token")
            tokenFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister FCM token", e)
        }
    }

    /**
     * Get all FCM registrations for a clone.
     */
    fun getRegistrations(cloneId: String): List<FcmRegistration> {
        return registrations[cloneId]?.toList() ?: emptyList()
    }

    /**
     * Clean up all FCM data for a clone.
     */
    fun cleanupClone(cloneId: String) {
        try {
            getFcmDir(cloneId).deleteRecursively()
            registrations.remove(cloneId)
            Log.d(TAG, "Cleaned up FCM data for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup FCM for clone $cloneId", e)
        }
    }

    private fun getFcmDir(cloneId: String): File {
        return File(context.filesDir, "clones/$cloneId/$FCM_DIR")
    }

    private fun persistRegistration(cloneId: String, reg: FcmRegistration) {
        try {
            val tokenFile = File(getFcmDir(cloneId), "${reg.appPackage}.token")
            tokenFile.parentFile?.mkdirs()
            val json = buildString {
                append("{")
                append("\"registrationToken\":\"${reg.registrationToken}\",")
                append("\"senderId\":\"${reg.senderId}\",")
                append("\"appPackage\":\"${reg.appPackage}\",")
                append("\"registeredAt\":${reg.registeredAt}")
                append("}")
            }
            tokenFile.writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist FCM registration", e)
        }
    }
}

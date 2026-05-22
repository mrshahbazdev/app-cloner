package com.titanclone.titan_clone.gms

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.titanclone.titan_clone.profile.ProfileGenerator
import com.titanclone.titan_clone.profile.VirtualProfileManager
import java.io.File
import java.util.UUID

/**
 * Manages Play Store clone creation and lifecycle.
 *
 * A Play Store clone consists of:
 *   1. The Play Store APK (com.android.vending) inside a virtual sandbox
 *   2. Real GMS proxied with per-clone data isolation
 *   3. Unique device identity (from ProfileGenerator) so Google sees a
 *      different device for each clone
 *   4. Separate Google account session per clone
 *
 * The flow:
 *   User creates clone → engine copies APKs → assigns device preset →
 *   generates unique IDs → clone boots → GMS checkin → user signs in →
 *   Play Store functional
 */
class PlayStoreCloneManager(
    private val context: Context,
    private val gmsProxy: GmsServiceProxy,
    private val profileManager: VirtualProfileManager,
    private val profileGenerator: ProfileGenerator
) {

    companion object {
        private const val TAG = "PlayStoreCloneMgr"
        const val MAX_PLAY_STORE_CLONES = 12
    }

    data class PlayStoreClone(
        val cloneId: String,
        val displayName: String,
        val devicePreset: String,
        val gmsConfig: GmsProxyConfig,
        val accountEmail: String?,
        val setupComplete: Boolean,
        val createdAt: Long
    )

    private val activeClones = mutableMapOf<String, PlayStoreClone>()

    /**
     * Create a new Play Store clone with a unique device identity.
     *
     * @param devicePreset device profile preset name (e.g. "Google Pixel 8 Pro")
     * @return clone ID on success, null on failure
     */
    fun createPlayStoreClone(devicePreset: String? = null): String? {
        if (activeClones.size >= MAX_PLAY_STORE_CLONES) {
            Log.w(TAG, "Maximum Play Store clones ($MAX_PLAY_STORE_CLONES) reached")
            return null
        }

        val gmsState = gmsProxy.detectGmsState()
        if (!gmsState.available) {
            Log.e(TAG, "Cannot create Play Store clone: GMS not available on host")
            return null
        }

        val cloneId = "ps_${UUID.randomUUID().toString().take(8)}"
        val preset = devicePreset ?: "Google Pixel 8 Pro"

        return try {
            // Generate unique device identity
            val gsfId = generateGsfId()
            val advertisingId = UUID.randomUUID().toString()

            val gmsConfig = GmsProxyConfig(
                cloneId = cloneId,
                gsfId = gsfId,
                advertisingId = advertisingId,
                useRealGms = true,
                useMicroG = false
            )

            // Prepare GMS files in clone sandbox
            if (!gmsProxy.prepareGmsForClone(cloneId, gmsConfig)) {
                Log.e(TAG, "Failed to prepare GMS for clone $cloneId")
                return null
            }

            // Apply device preset via profile manager
            profileManager.applyPreset(cloneId, 0, preset)

            val clone = PlayStoreClone(
                cloneId = cloneId,
                displayName = "Play Store #${activeClones.size + 1}",
                devicePreset = preset,
                gmsConfig = gmsConfig,
                accountEmail = null,
                setupComplete = false,
                createdAt = System.currentTimeMillis()
            )
            activeClones[cloneId] = clone

            Log.d(TAG, "Created Play Store clone: $cloneId (preset: $preset)")
            cloneId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create Play Store clone", e)
            null
        }
    }

    /**
     * Mark a Play Store clone as having completed initial setup
     * (user signed in, Play Store opened successfully).
     */
    fun markSetupComplete(cloneId: String, accountEmail: String) {
        activeClones[cloneId]?.let { clone ->
            activeClones[cloneId] = clone.copy(
                accountEmail = accountEmail,
                setupComplete = true
            )
            Log.d(TAG, "Clone $cloneId setup complete with account: $accountEmail")
        }
    }

    /**
     * Delete a Play Store clone and clean up all its GMS data.
     */
    fun deletePlayStoreClone(cloneId: String): Boolean {
        return try {
            gmsProxy.cleanupCloneGms(cloneId)
            activeClones.remove(cloneId)
            Log.d(TAG, "Deleted Play Store clone: $cloneId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete clone $cloneId", e)
            false
        }
    }

    /**
     * Get all active Play Store clones.
     */
    fun getPlayStoreClones(): List<PlayStoreClone> {
        return activeClones.values.toList()
    }

    /**
     * Get a specific Play Store clone.
     */
    fun getPlayStoreClone(cloneId: String): PlayStoreClone? {
        return activeClones[cloneId]
    }

    /**
     * Check if the host device can support Play Store cloning.
     */
    fun canCreatePlayStoreClone(): PlayStoreClonePrecheck {
        val gmsState = gmsProxy.detectGmsState()
        return PlayStoreClonePrecheck(
            gmsAvailable = gmsState.available,
            playStoreAvailable = gmsState.playStoreVersion != null,
            gsfAvailable = gmsState.gsfAvailable,
            slotsRemaining = MAX_PLAY_STORE_CLONES - activeClones.size,
            canCreate = gmsState.available
                    && gmsState.playStoreVersion != null
                    && activeClones.size < MAX_PLAY_STORE_CLONES
        )
    }

    /**
     * Generate a unique GSF (Google Services Framework) ID.
     * In production this would be a proper hex-encoded 16-char ID
     * matching Google's format.
     */
    private fun generateGsfId(): String {
        val chars = "0123456789abcdef"
        return (1..16).map { chars.random() }.joinToString("")
    }

    data class PlayStoreClonePrecheck(
        val gmsAvailable: Boolean,
        val playStoreAvailable: Boolean,
        val gsfAvailable: Boolean,
        val slotsRemaining: Int,
        val canCreate: Boolean
    )
}

package com.titanclone.titan_clone.gms

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Manages MicroG (open-source GMS replacement) for non-Play Store clones.
 *
 * MicroG provides push notifications (FCM), location services, and limited
 * Google account support without the full weight of real GMS. It is
 * recommended for clones of apps like WhatsApp, Telegram, Instagram
 * that only need push notifications — NOT for Play Store clones.
 *
 * Architecture:
 *   MicroG APK bundled in assets → extracted on first use →
 *   injected into clone sandbox → provides GmsCore + GsfProxy →
 *   apps detect "Play Services" → push notifications work
 */
class MicroGManager(private val context: Context) {

    companion object {
        private const val TAG = "MicroGManager"
        private const val MICROG_CORE_ASSET = "microg-core.apk"
        private const val MICROG_GSF_ASSET = "microg-gsf.apk"
        private const val MICROG_STORE_ASSET = "microg-store.apk"
        private const val MICROG_DIR = "microg"

        const val MICROG_CORE_PACKAGE = "com.google.android.gms"
        const val MICROG_GSF_PACKAGE = "com.google.android.gsf"
    }

    data class MicroGState(
        val coreExtracted: Boolean,
        val gsfExtracted: Boolean,
        val storeExtracted: Boolean,
        val coreVersion: String?
    )

    private var initialized = false

    /**
     * Initialize MicroG by extracting APKs from bundled assets.
     */
    fun initialize(): Boolean {
        if (initialized) return true
        return try {
            val microGDir = getMicroGDir()
            microGDir.mkdirs()

            val coreExtracted = extractAsset(MICROG_CORE_ASSET, File(microGDir, MICROG_CORE_ASSET))
            val gsfExtracted = extractAsset(MICROG_GSF_ASSET, File(microGDir, MICROG_GSF_ASSET))
            extractAsset(MICROG_STORE_ASSET, File(microGDir, MICROG_STORE_ASSET))

            initialized = coreExtracted || gsfExtracted
            Log.d(TAG, "MicroG initialized: core=$coreExtracted, gsf=$gsfExtracted")
            initialized
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MicroG", e)
            false
        }
    }

    /**
     * Determine whether a cloned app should use MicroG or real GMS.
     *
     * Play Store and Google apps MUST use real GMS.
     * Other apps default to MicroG for lighter footprint and better isolation.
     */
    fun shouldUseMicroG(packageName: String): Boolean {
        return !GmsProxyConfig.isGmsPackage(packageName)
    }

    /**
     * Prepare MicroG for a specific clone by copying APKs into
     * the clone's sandbox.
     */
    fun prepareMicroGForClone(cloneId: String): Boolean {
        if (!initialized) initialize()
        return try {
            val cloneMicroGDir = getCloneMicroGDir(cloneId)
            cloneMicroGDir.mkdirs()

            val coreApk = File(getMicroGDir(), MICROG_CORE_ASSET)
            if (coreApk.exists()) {
                coreApk.copyTo(File(cloneMicroGDir, MICROG_CORE_ASSET), overwrite = true)
            }

            val gsfApk = File(getMicroGDir(), MICROG_GSF_ASSET)
            if (gsfApk.exists()) {
                gsfApk.copyTo(File(cloneMicroGDir, MICROG_GSF_ASSET), overwrite = true)
            }

            // Create MicroG data directories
            File(cloneMicroGDir, "data/databases").mkdirs()
            File(cloneMicroGDir, "data/shared_prefs").mkdirs()

            Log.d(TAG, "Prepared MicroG for clone $cloneId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare MicroG for clone $cloneId", e)
            false
        }
    }

    /**
     * Get the MicroG core APK path for injection into a clone.
     */
    fun getMicroGApkPath(): String? {
        val coreApk = File(getMicroGDir(), MICROG_CORE_ASSET)
        return if (coreApk.exists()) coreApk.absolutePath else null
    }

    /**
     * Get MicroG APK paths for a specific clone.
     */
    fun getCloneMicroGPaths(cloneId: String): List<String> {
        val cloneDir = getCloneMicroGDir(cloneId)
        return listOfNotNull(
            File(cloneDir, MICROG_CORE_ASSET).takeIf { it.exists() }?.absolutePath,
            File(cloneDir, MICROG_GSF_ASSET).takeIf { it.exists() }?.absolutePath
        )
    }

    /**
     * Get the current MicroG state.
     */
    fun getState(): MicroGState {
        val microGDir = getMicroGDir()
        return MicroGState(
            coreExtracted = File(microGDir, MICROG_CORE_ASSET).exists(),
            gsfExtracted = File(microGDir, MICROG_GSF_ASSET).exists(),
            storeExtracted = File(microGDir, MICROG_STORE_ASSET).exists(),
            coreVersion = null
        )
    }

    /**
     * Clean up MicroG files for a specific clone.
     */
    fun cleanupClone(cloneId: String) {
        try {
            getCloneMicroGDir(cloneId).deleteRecursively()
            Log.d(TAG, "Cleaned up MicroG for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup MicroG for clone $cloneId", e)
        }
    }

    private fun getMicroGDir(): File {
        return File(context.filesDir, MICROG_DIR)
    }

    private fun getCloneMicroGDir(cloneId: String): File {
        return File(context.filesDir, "clones/$cloneId/$MICROG_DIR")
    }

    private fun extractAsset(assetName: String, targetFile: File): Boolean {
        return try {
            if (targetFile.exists()) return true
            val inputStream = context.assets.open(assetName)
            targetFile.parentFile?.mkdirs()
            targetFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Asset not found: $assetName (will be added later)")
            false
        }
    }
}

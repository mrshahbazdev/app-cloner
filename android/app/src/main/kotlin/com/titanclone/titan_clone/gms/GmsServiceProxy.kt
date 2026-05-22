package com.titanclone.titan_clone.gms

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File

/**
 * Proxies the host device's real Google Mobile Services (GMS) into each
 * clone's virtual environment.
 *
 * For Play Store clones the real GMS package is required because MicroG
 * does not support the full Play Store installation flow. Each clone gets:
 *   - A copy of the GMS APK in its sandbox
 *   - An isolated data directory for GMS (per-clone registration, tokens, etc.)
 *   - Correct PackageManager metadata so the clone "sees" GMS
 */
class GmsServiceProxy(private val context: Context) {

    companion object {
        private const val TAG = "GmsServiceProxy"

        /** Minimum GMS version we consider usable. */
        private const val MIN_GMS_VERSION_CODE = 230_000_000L
    }

    data class GmsState(
        val available: Boolean,
        val gmsVersion: String?,
        val gmsVersionCode: Long,
        val playStoreVersion: String?,
        val playStoreVersionCode: Long,
        val gsfAvailable: Boolean
    )

    /**
     * Detect host device's GMS installation status.
     */
    fun detectGmsState(): GmsState {
        val pm = context.packageManager

        val gmsInfo = try {
            pm.getPackageInfo(GmsProxyConfig.GMS_PACKAGE, 0)
        } catch (_: PackageManager.NameNotFoundException) { null }

        val playStoreInfo = try {
            pm.getPackageInfo(GmsProxyConfig.PLAY_STORE_PACKAGE, 0)
        } catch (_: PackageManager.NameNotFoundException) { null }

        val gsfInfo = try {
            pm.getPackageInfo(GmsProxyConfig.GSF_PACKAGE, 0)
        } catch (_: PackageManager.NameNotFoundException) { null }

        val gmsVersionCode = if (gmsInfo != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                gmsInfo.longVersionCode
            else
                @Suppress("DEPRECATION") gmsInfo.versionCode.toLong()
        } else 0L

        val playStoreVersionCode = if (playStoreInfo != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                playStoreInfo.longVersionCode
            else
                @Suppress("DEPRECATION") playStoreInfo.versionCode.toLong()
        } else 0L

        return GmsState(
            available = gmsInfo != null,
            gmsVersion = gmsInfo?.versionName,
            gmsVersionCode = gmsVersionCode,
            playStoreVersion = playStoreInfo?.versionName,
            playStoreVersionCode = playStoreVersionCode,
            gsfAvailable = gsfInfo != null
        )
    }

    /**
     * Prepare GMS files for a specific clone by copying the APKs into
     * the clone's isolated sandbox directory.
     *
     * @return true if preparation succeeded
     */
    fun prepareGmsForClone(cloneId: String, config: GmsProxyConfig): Boolean {
        val state = detectGmsState()
        if (!state.available) {
            Log.w(TAG, "GMS not available on host device")
            return false
        }
        if (state.gmsVersionCode < MIN_GMS_VERSION_CODE) {
            Log.w(TAG, "GMS version too old: ${state.gmsVersion}")
            return false
        }

        return try {
            val cloneDir = getCloneGmsDir(cloneId)
            cloneDir.mkdirs()

            // Copy GMS APK
            if (config.useRealGms) {
                copyGmsApk(cloneId, GmsProxyConfig.GMS_PACKAGE)
                copyGmsApk(cloneId, GmsProxyConfig.GSF_PACKAGE)
                Log.d(TAG, "Real GMS prepared for clone $cloneId")
            }

            // Copy Play Store APK if this is a Play Store clone
            val pm = context.packageManager
            try {
                pm.getPackageInfo(GmsProxyConfig.PLAY_STORE_PACKAGE, 0)
                copyGmsApk(cloneId, GmsProxyConfig.PLAY_STORE_PACKAGE)
                Log.d(TAG, "Play Store APK prepared for clone $cloneId")
            } catch (_: PackageManager.NameNotFoundException) {
                Log.w(TAG, "Play Store not installed on host")
            }

            // Create isolated data directories
            createIsolatedDataDirs(cloneId)

            // Write per-clone GMS config
            writeCloneGmsConfig(cloneId, config)

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare GMS for clone $cloneId", e)
            false
        }
    }

    /**
     * Copy a GMS-related APK from the host into the clone's sandbox.
     */
    private fun copyGmsApk(cloneId: String, packageName: String) {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        val sourceApk = File(appInfo.sourceDir)
        val targetDir = File(getCloneGmsDir(cloneId), packageName)
        targetDir.mkdirs()

        val targetApk = File(targetDir, "base.apk")
        if (!targetApk.exists() || targetApk.length() != sourceApk.length()) {
            sourceApk.copyTo(targetApk, overwrite = true)
            Log.d(TAG, "Copied $packageName APK to clone $cloneId")
        }

        // Also copy split APKs if present
        appInfo.splitSourceDirs?.forEachIndexed { index, splitPath ->
            val splitFile = File(splitPath)
            val targetSplit = File(targetDir, "split_$index.apk")
            if (!targetSplit.exists() || targetSplit.length() != splitFile.length()) {
                splitFile.copyTo(targetSplit, overwrite = true)
            }
        }
    }

    /**
     * Create per-clone isolated data directories for GMS, GSF, and Play Store
     * so each clone has its own registration, tokens, and account data.
     */
    private fun createIsolatedDataDirs(cloneId: String) {
        val dataRoot = getCloneGmsDataDir(cloneId)
        File(dataRoot, "com.google.android.gms/databases").mkdirs()
        File(dataRoot, "com.google.android.gms/shared_prefs").mkdirs()
        File(dataRoot, "com.google.android.gms/cache").mkdirs()
        File(dataRoot, "com.google.android.gsf/databases").mkdirs()
        File(dataRoot, "com.google.android.gsf/shared_prefs").mkdirs()
        File(dataRoot, "com.android.vending/databases").mkdirs()
        File(dataRoot, "com.android.vending/shared_prefs").mkdirs()
        File(dataRoot, "com.android.vending/cache").mkdirs()
        File(dataRoot, "com.android.vending/files").mkdirs()
        Log.d(TAG, "Created isolated GMS data dirs for clone $cloneId")
    }

    /**
     * Persist the GMS proxy configuration for this clone.
     */
    private fun writeCloneGmsConfig(cloneId: String, config: GmsProxyConfig) {
        val configFile = File(getCloneGmsDir(cloneId), "gms_config.json")
        val json = buildString {
            append("{")
            append("\"cloneId\":\"${config.cloneId}\",")
            append("\"gsfId\":\"${config.gsfId}\",")
            append("\"advertisingId\":\"${config.advertisingId}\",")
            append("\"useRealGms\":${config.useRealGms},")
            append("\"useMicroG\":${config.useMicroG}")
            config.accountName?.let { append(",\"accountName\":\"$it\"") }
            config.playStoreSignature?.let { append(",\"playStoreSignature\":\"$it\"") }
            append("}")
        }
        configFile.writeText(json)
    }

    /**
     * Remove all GMS data for a clone (called on clone deletion).
     */
    fun cleanupCloneGms(cloneId: String) {
        try {
            getCloneGmsDir(cloneId).deleteRecursively()
            getCloneGmsDataDir(cloneId).deleteRecursively()
            Log.d(TAG, "Cleaned up GMS data for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup GMS for clone $cloneId", e)
        }
    }

    /**
     * Get the GMS APK installation directory for a clone.
     */
    fun getCloneGmsDir(cloneId: String): File {
        return File(context.filesDir, "clones/$cloneId/gms")
    }

    /**
     * Get the isolated GMS data directory for a clone.
     */
    fun getCloneGmsDataDir(cloneId: String): File {
        return File(context.filesDir, "clones/$cloneId/gms_data")
    }

    /**
     * Check if GMS is properly configured for a specific clone.
     */
    fun isGmsReadyForClone(cloneId: String): Boolean {
        val gmsDir = getCloneGmsDir(cloneId)
        val gmsApk = File(gmsDir, "${GmsProxyConfig.GMS_PACKAGE}/base.apk")
        val configFile = File(gmsDir, "gms_config.json")
        return gmsApk.exists() && configFile.exists()
    }
}

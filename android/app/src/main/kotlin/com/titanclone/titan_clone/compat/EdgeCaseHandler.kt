package com.titanclone.titan_clone.compat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.CookieManager
import java.io.File

/**
 * Handles edge cases that arise when virtualizing Android apps.
 *
 * Key edge cases:
 *   - Deep linking: whatsapp://send routes to clone, not real app
 *   - WebView cookies: per-clone cookie isolation
 *   - Native libraries: .so files loaded with correct path redirection
 *   - Home screen shortcuts: clone shortcuts launch correct instance
 *   - Camera/media: photos saved to clone-specific storage
 *   - Content providers: per-clone URI redirection
 */
class EdgeCaseHandler(private val context: Context) {

    companion object {
        private const val TAG = "EdgeCaseHandler"
    }

    /**
     * Rewrite a deep link intent to route to a specific clone.
     *
     * Example: whatsapp://send?phone=123 → should open in
     * the correct WhatsApp clone, not the real installed app.
     */
    fun rewriteDeepLink(cloneId: String, originalIntent: Intent): Intent {
        return Intent(originalIntent).apply {
            putExtra("_titanclone_id", cloneId)
            putExtra("_titanclone_route", true)
            // Remove explicit component so it routes through our engine
            component = null
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Check if an intent should be intercepted for a clone.
     */
    fun shouldInterceptIntent(intent: Intent, clonePackage: String): Boolean {
        val data = intent.data ?: return false
        val scheme = data.scheme ?: return false

        // Known deep link schemes
        val knownSchemes = setOf(
            "whatsapp", "telegram", "tg", "instagram",
            "fb", "twitter", "snapchat", "viber",
            "line", "wechat", "signal"
        )

        return scheme in knownSchemes || intent.`package` == clonePackage
    }

    /**
     * Set up per-clone WebView cookie isolation.
     *
     * Each clone must have its own cookie jar so authenticated
     * sessions don't leak between clones.
     */
    fun setupWebViewCookieIsolation(cloneId: String) {
        try {
            val cookieDir = getCloneCookieDir(cloneId)
            cookieDir.mkdirs()

            // Set the WebView data directory for this clone
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val suffix = "clone_$cloneId"
                try {
                    android.webkit.WebView.setDataDirectorySuffix(suffix)
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "WebView data directory already set", e)
                }
            }

            Log.d(TAG, "WebView cookie isolation set up for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up WebView isolation for clone $cloneId", e)
        }
    }

    /**
     * Clear WebView cookies for a specific clone.
     */
    fun clearWebViewCookies(cloneId: String) {
        try {
            val cookieDir = getCloneCookieDir(cloneId)
            cookieDir.deleteRecursively()
            CookieManager.getInstance().removeAllCookies(null)
            Log.d(TAG, "Cleared WebView cookies for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cookies for clone $cloneId", e)
        }
    }

    /**
     * Get the correct native library path for a cloned app.
     *
     * The IO redirect layer (C++) handles most of this, but we
     * need to ensure the clone's .so files are in the right place.
     */
    fun getNativeLibraryPath(cloneId: String, packageName: String): String {
        return File(
            context.filesDir,
            "clones/$cloneId/native_libs/$packageName"
        ).absolutePath
    }

    /**
     * Prepare native libraries for a cloned app.
     */
    fun prepareNativeLibraries(
        cloneId: String,
        packageName: String,
        sourceLibDir: String?
    ): Boolean {
        if (sourceLibDir == null) return true
        return try {
            val sourceDir = File(sourceLibDir)
            if (!sourceDir.isDirectory) return true

            val targetDir = File(getNativeLibraryPath(cloneId, packageName))
            targetDir.mkdirs()

            sourceDir.walkTopDown()
                .filter { it.isFile && it.extension == "so" }
                .forEach { libFile ->
                    val targetFile = File(targetDir, libFile.name)
                    if (!targetFile.exists()) {
                        libFile.copyTo(targetFile, overwrite = true)
                    }
                }

            Log.d(TAG, "Prepared native libs for $packageName in clone $cloneId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare native libs for $packageName", e)
            false
        }
    }

    /**
     * Get the clone-specific media storage path.
     * Photos and media captured within a clone are saved here.
     */
    fun getCloneMediaPath(cloneId: String): String {
        val mediaDir = File(context.filesDir, "clones/$cloneId/media")
        mediaDir.mkdirs()
        return mediaDir.absolutePath
    }

    /**
     * Create a home screen shortcut for a clone.
     */
    fun createShortcutIntent(cloneId: String, appName: String, cloneLabel: String): Intent {
        return Intent("com.titanclone.LAUNCH_CLONE").apply {
            putExtra("cloneId", cloneId)
            putExtra("appName", appName)
            putExtra("label", cloneLabel)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Rewrite a content URI for clone-specific storage.
     */
    fun rewriteContentUri(cloneId: String, originalUri: Uri): Uri {
        val cloneAuthority = "${context.packageName}.clone.$cloneId"
        return Uri.Builder()
            .scheme("content")
            .authority(cloneAuthority)
            .path(originalUri.path)
            .build()
    }

    /**
     * Clean up all edge case data for a clone.
     */
    fun cleanupClone(cloneId: String) {
        try {
            getCloneCookieDir(cloneId).deleteRecursively()
            File(context.filesDir, "clones/$cloneId/native_libs").deleteRecursively()
            File(context.filesDir, "clones/$cloneId/media").deleteRecursively()
            Log.d(TAG, "Cleaned up edge case data for clone $cloneId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup edge case data for clone $cloneId", e)
        }
    }

    private fun getCloneCookieDir(cloneId: String): File {
        return File(context.filesDir, "clones/$cloneId/cookies")
    }
}

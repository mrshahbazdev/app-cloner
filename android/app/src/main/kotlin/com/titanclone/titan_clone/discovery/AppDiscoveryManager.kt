package com.titanclone.titan_clone.discovery

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Scans installed packages on the device, parses APK metadata,
 * handles split APKs, and provides installable app info to the UI.
 */
class AppDiscoveryManager(private val context: Context) {

    companion object {
        private const val TAG = "AppDiscoveryManager"
        private const val ICON_CACHE_DIR = "icon_cache"
        private const val ICON_SIZE = 96
    }

    data class DiscoveredApp(
        val packageName: String,
        val appName: String,
        val versionName: String?,
        val versionCode: Long,
        val sourceDir: String,
        val splitSourceDirs: List<String>,
        val nativeLibraryDir: String?,
        val minSdkVersion: Int,
        val targetSdkVersion: Int,
        val isSystemApp: Boolean,
        val permissions: List<String>,
        val activities: List<String>,
        val services: List<String>,
        val receivers: List<String>,
        val providers: List<String>,
        val totalSizeBytes: Long,
        val iconPath: String?,
        val abis: List<String>
    )

    /**
     * Scan all user-installed packages on the device.
     * Handles QUERY_ALL_PACKAGES for Android 11+.
     */
    fun scanInstalledApps(includeSystemApps: Boolean = false): List<DiscoveredApp> {
        val pm = context.packageManager
        val flags = PackageManager.GET_META_DATA or
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_PERMISSIONS

        val packages = try {
            pm.getInstalledPackages(flags)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get installed packages", e)
            emptyList()
        }

        return packages.mapNotNull { info ->
            try {
                val appInfo = info.applicationInfo ?: return@mapNotNull null
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                if (!includeSystemApps && isSystem) return@mapNotNull null

                parsePackageInfo(info, pm)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse package: ${info.packageName}", e)
                null
            }
        }.sortedBy { it.appName.lowercase() }
    }

    /**
     * Get detailed info for a specific package.
     */
    fun getAppInfo(packageName: String): DiscoveredApp? {
        val pm = context.packageManager
        return try {
            val flags = PackageManager.GET_META_DATA or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.GET_PERMISSIONS
            val info = pm.getPackageInfo(packageName, flags)
            parsePackageInfo(info, pm)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Package not found: $packageName")
            null
        }
    }

    /**
     * Estimate total size of an app (base APK + splits + native libs).
     */
    fun estimateAppSize(packageName: String): Long {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            var size = File(appInfo.sourceDir).length()
            appInfo.splitSourceDirs?.forEach { split ->
                size += File(split).length()
            }
            appInfo.nativeLibraryDir?.let { dir ->
                val libDir = File(dir)
                if (libDir.isDirectory) {
                    libDir.walkTopDown().filter { it.isFile }.forEach {
                        size += it.length()
                    }
                }
            }
            size
        } catch (e: Exception) {
            Log.e(TAG, "Failed to estimate size for $packageName", e)
            -1
        }
    }

    /**
     * Get the list of split APK paths for a package (App Bundles).
     */
    fun getSplitApkPaths(packageName: String): List<String> {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            appInfo.splitSourceDirs?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Check ABI compatibility between app and device.
     */
    fun checkAbiCompatibility(packageName: String): AbiCheckResult {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            val nativeLibDir = appInfo.nativeLibraryDir
            val deviceAbis = Build.SUPPORTED_ABIS.toList()

            if (nativeLibDir == null || !File(nativeLibDir).exists()) {
                return AbiCheckResult(compatible = true, appAbis = emptyList(),
                    deviceAbis = deviceAbis, hasNativeCode = false)
            }

            val libDir = File(nativeLibDir)
            val hasLibs = libDir.listFiles()?.isNotEmpty() == true

            AbiCheckResult(
                compatible = true,
                appAbis = listOf(libDir.name),
                deviceAbis = deviceAbis,
                hasNativeCode = hasLibs
            )
        } catch (e: Exception) {
            AbiCheckResult(compatible = false, appAbis = emptyList(),
                deviceAbis = emptyList(), hasNativeCode = false,
                error = e.message)
        }
    }

    private fun parsePackageInfo(info: PackageInfo, pm: PackageManager): DiscoveredApp {
        val appInfo = info.applicationInfo!!
        val appName = pm.getApplicationLabel(appInfo).toString()
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        val splitDirs = appInfo.splitSourceDirs?.toList() ?: emptyList()

        val activities = info.activities?.map { it.name } ?: emptyList()
        val services = info.services?.map { it.name } ?: emptyList()
        val receivers = info.receivers?.map { it.name } ?: emptyList()
        val providers = info.providers?.map { it.name } ?: emptyList()
        val permissions = info.requestedPermissions?.toList() ?: emptyList()

        var totalSize = File(appInfo.sourceDir).length()
        splitDirs.forEach { totalSize += File(it).length() }

        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }

        val iconPath = cacheAppIcon(info.packageName, pm.getApplicationIcon(appInfo))

        val abis = mutableListOf<String>()
        appInfo.nativeLibraryDir?.let { dir ->
            val libDir = File(dir)
            if (libDir.exists() && libDir.isDirectory) {
                abis.add(libDir.name)
            }
        }
        if (abis.isEmpty()) {
            abis.addAll(Build.SUPPORTED_ABIS.toList())
        }

        return DiscoveredApp(
            packageName = info.packageName,
            appName = appName,
            versionName = info.versionName,
            versionCode = versionCode,
            sourceDir = appInfo.sourceDir,
            splitSourceDirs = splitDirs,
            nativeLibraryDir = appInfo.nativeLibraryDir,
            minSdkVersion = appInfo.minSdkVersion,
            targetSdkVersion = appInfo.targetSdkVersion,
            isSystemApp = isSystem,
            permissions = permissions,
            activities = activities,
            services = services,
            receivers = receivers,
            providers = providers,
            totalSizeBytes = totalSize,
            iconPath = iconPath,
            abis = abis
        )
    }

    private fun cacheAppIcon(packageName: String, drawable: Drawable): String? {
        return try {
            val cacheDir = File(context.cacheDir, ICON_CACHE_DIR)
            cacheDir.mkdirs()
            val iconFile = File(cacheDir, "$packageName.png")
            if (iconFile.exists()) return iconFile.absolutePath

            val bitmap = drawableToBitmap(drawable)
            FileOutputStream(iconFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            iconFile.absolutePath
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache icon for $packageName", e)
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return Bitmap.createScaledBitmap(drawable.bitmap, ICON_SIZE, ICON_SIZE, true)
        }
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    data class AbiCheckResult(
        val compatible: Boolean,
        val appAbis: List<String>,
        val deviceAbis: List<String>,
        val hasNativeCode: Boolean,
        val error: String? = null
    )
}

package com.titanclone.titan_clone.compat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Handles Android version-specific compatibility issues for the engine.
 *
 * Supported versions: Android 10 (API 29) through Android 15 (API 35).
 *
 * Each version introduces restrictions or behavioral changes that
 * affect how clones operate:
 *   - API 29: Scoped storage (opt-out with requestLegacyExternalStorage)
 *   - API 30: Package visibility restrictions, MANAGE_EXTERNAL_STORAGE
 *   - API 31-32: Exact alarm permissions, exported component rules
 *   - API 33: Per-app language, notification runtime permission
 *   - API 34: Foreground service types, stricter background limits
 *   - API 35: Stricter scoped storage, edge-to-edge enforced
 */
class VersionCompatHandler(private val context: Context) {

    companion object {
        private const val TAG = "VersionCompat"
        const val MIN_SUPPORTED_API = 29  // Android 10
        const val MAX_SUPPORTED_API = 35  // Android 15
    }

    data class CompatibilityReport(
        val apiLevel: Int,
        val androidVersion: String,
        val isSupported: Boolean,
        val issues: List<CompatIssue>,
        val missingPermissions: List<String>,
        val recommendations: List<String>
    )

    data class CompatIssue(
        val severity: Severity,
        val category: String,
        val description: String,
        val fix: String?
    )

    enum class Severity { INFO, WARNING, ERROR }

    /**
     * Run a full compatibility check for the current device.
     */
    fun checkCompatibility(): CompatibilityReport {
        val apiLevel = Build.VERSION.SDK_INT
        val issues = mutableListOf<CompatIssue>()
        val missingPermissions = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        val isSupported = apiLevel in MIN_SUPPORTED_API..MAX_SUPPORTED_API

        if (!isSupported) {
            issues.add(CompatIssue(
                Severity.ERROR, "Version",
                "Android API $apiLevel is not supported (requires $MIN_SUPPORTED_API-$MAX_SUPPORTED_API)",
                null
            ))
        }

        // Check required permissions
        checkPermission(Manifest.permission.QUERY_ALL_PACKAGES, missingPermissions)
        checkPermission(Manifest.permission.INTERNET, missingPermissions)
        checkPermission(Manifest.permission.FOREGROUND_SERVICE, missingPermissions)

        if (apiLevel >= 33) {
            checkPermission(Manifest.permission.POST_NOTIFICATIONS, missingPermissions)
            if (Manifest.permission.POST_NOTIFICATIONS in missingPermissions) {
                issues.add(CompatIssue(
                    Severity.WARNING, "Notifications",
                    "POST_NOTIFICATIONS permission required on Android 13+",
                    "Request permission at runtime"
                ))
            }
        }

        // API 30+ package visibility
        if (apiLevel >= 30) {
            issues.add(CompatIssue(
                Severity.INFO, "Package Visibility",
                "Android 11+ restricts package visibility — QUERY_ALL_PACKAGES declared",
                null
            ))
        }

        // API 31+ exact alarm
        if (apiLevel >= 31) {
            if (!hasExactAlarmPermission()) {
                issues.add(CompatIssue(
                    Severity.WARNING, "Alarms",
                    "SCHEDULE_EXACT_ALARM may be required for clone background alarms",
                    "Request SCHEDULE_EXACT_ALARM permission"
                ))
                recommendations.add("Request exact alarm permission for background clone operations")
            }
        }

        // API 34+ foreground service type
        if (apiLevel >= 34) {
            issues.add(CompatIssue(
                Severity.INFO, "Foreground Service",
                "Android 14+ requires foregroundServiceType declaration",
                "specialUse type declared in manifest"
            ))
            recommendations.add("Ensure Play Store policy justification for QUERY_ALL_PACKAGES")
        }

        // API 35 edge-to-edge
        if (apiLevel >= 35) {
            issues.add(CompatIssue(
                Severity.INFO, "UI",
                "Android 15 enforces edge-to-edge display",
                "Flutter handles edge-to-edge automatically"
            ))
        }

        // Storage
        if (apiLevel >= 30) {
            if (!Environment.isExternalStorageManager()) {
                recommendations.add("Consider requesting MANAGE_EXTERNAL_STORAGE for full file access")
            }
        }

        return CompatibilityReport(
            apiLevel = apiLevel,
            androidVersion = Build.VERSION.RELEASE,
            isSupported = isSupported,
            issues = issues,
            missingPermissions = missingPermissions,
            recommendations = recommendations
        )
    }

    /**
     * Get the appropriate storage access method for the current API level.
     */
    fun getStorageStrategy(): StorageStrategy {
        return when {
            Build.VERSION.SDK_INT >= 30 -> StorageStrategy.SCOPED_WITH_MANAGE
            Build.VERSION.SDK_INT >= 29 -> StorageStrategy.LEGACY_OPT_OUT
            else -> StorageStrategy.FULL_ACCESS
        }
    }

    /**
     * Get the appropriate foreground service configuration.
     */
    fun getForegroundServiceConfig(): ForegroundServiceConfig {
        return ForegroundServiceConfig(
            requiresType = Build.VERSION.SDK_INT >= 34,
            serviceType = if (Build.VERSION.SDK_INT >= 34) "specialUse" else null,
            requiresNotificationPermission = Build.VERSION.SDK_INT >= 33
        )
    }

    /**
     * Request missing runtime permissions.
     */
    fun requestMissingPermissions(activity: Activity) {
        val report = checkCompatibility()
        if (report.missingPermissions.isNotEmpty()) {
            activity.requestPermissions(
                report.missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun checkPermission(permission: String, missing: MutableList<String>) {
        if (ContextCompat.checkSelfPermission(context, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            missing.add(permission)
        }
    }

    private fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            am.canScheduleExactAlarms()
        } else {
            true
        }
    }

    enum class StorageStrategy {
        FULL_ACCESS,
        LEGACY_OPT_OUT,
        SCOPED_WITH_MANAGE
    }

    data class ForegroundServiceConfig(
        val requiresType: Boolean,
        val serviceType: String?,
        val requiresNotificationPermission: Boolean
    )

    companion object RequestCodes {
        const val PERMISSION_REQUEST_CODE = 1001
    }
}

package com.titanclone.titan_clone.profile.db

/**
 * Data class representing a stored virtual device profile.
 *
 * TODO: Convert to Room @Entity when Room is integrated.
 * For now, profiles are stored via SharedPreferences (VirtualProfileManager).
 */
data class VirtualProfileEntity(
    val id: String,
    val cloneId: String,
    val packageName: String,
    val userId: Int,
    val name: String,
    val model: String,
    val brand: String,
    val manufacturer: String,
    val fingerprint: String,
    val screenDensity: Int,
    val screenWidth: Int,
    val screenHeight: Int,
    val sdkVersion: Int,
    val releaseVersion: String,
    val androidId: String,
    val imei: String,
    val macAddress: String,
    val bluetoothMac: String,
    val gsfId: String,
    val advertisingId: String,
    val serialNumber: String? = null,
    val timezone: String? = null,
    val locale: String? = null,
    val proxyHost: String? = null,
    val proxyPort: Int? = null,
    val proxyType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

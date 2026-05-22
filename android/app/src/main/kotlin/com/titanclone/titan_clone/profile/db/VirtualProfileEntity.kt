package com.titanclone.titan_clone.profile.db

/**
 * Full virtual device profile for a clone — 40+ identity fields.
 *
 * Every clone gets a unique, consistent device identity so apps
 * (especially Google Play) see each clone as a separate physical device.
 *
 * TODO: Convert to Room @Entity when Room dependency is added.
 * Currently serialized via JSON in SharedPreferences.
 */
data class VirtualProfileEntity(
    // Primary key
    val cloneId: String,            // "com.whatsapp_user0"
    val packageName: String,
    val userId: Int,

    // Device Identifiers
    val androidId: String,          // 16-char hex (unique per clone)
    val gsfId: String,              // Google Services Framework ID (hex long)
    val advertisingId: String,      // AAID (UUID format)

    // Hardware Fingerprint
    val deviceModel: String,        // Build.MODEL — e.g. "Pixel 8 Pro"
    val manufacturer: String,       // Build.MANUFACTURER
    val brand: String,              // Build.BRAND
    val product: String,            // Build.PRODUCT — e.g. "husky"
    val device: String,             // Build.DEVICE
    val hardware: String,           // Build.HARDWARE
    val buildFingerprint: String,   // Full Build.FINGERPRINT
    val buildDisplay: String,       // Build.DISPLAY
    val buildId: String,            // Build.ID
    val buildType: String,          // Build.TYPE — "user"
    val buildTags: String,          // Build.TAGS — "release-keys"
    val serial: String,             // Build.SERIAL
    val bootloader: String,         // Build.BOOTLOADER
    val board: String,              // Build.BOARD

    // Network Identifiers
    val macAddress: String,         // WiFi MAC (locally administered, unicast)
    val bluetoothMac: String,       // BT MAC
    val imei: String,               // 15 digits with valid Luhn checksum
    val imsi: String,               // 15 digits
    val simSerial: String,          // 19-20 digits
    val phoneNumber: String,        // E.164 format
    val carrierName: String,        // e.g. "T-Mobile"
    val carrierCode: String,        // MCC+MNC e.g. "310260"
    val countryIso: String,         // e.g. "us"

    // Display Properties
    val screenDensity: Int,         // DPI
    val screenWidth: Int,           // Pixels
    val screenHeight: Int,          // Pixels

    // System Info
    val sdkVersion: Int,            // Build.VERSION.SDK_INT
    val releaseVersion: String,     // Build.VERSION.RELEASE — e.g. "14"
    val securityPatch: String,      // Build.VERSION.SECURITY_PATCH
    val codename: String,           // Build.VERSION.CODENAME — "REL"
    val incremental: String,        // Build.VERSION.INCREMENTAL
    val baseOs: String,             // Build.VERSION.BASE_OS
    val previewSdkInt: Int,         // Build.VERSION.PREVIEW_SDK_INT

    // Locale/Timezone
    val locale: String,             // e.g. "en_US"
    val timezone: String,           // e.g. "America/New_York"

    // Network Proxy (per-clone IP isolation)
    val proxyHost: String?,         // SOCKS5 proxy host
    val proxyPort: Int?,            // SOCKS5 proxy port
    val proxyUser: String?,         // Proxy auth username
    val proxyPass: String?,         // Proxy auth password
    val dnsServer: String?,         // Per-clone DNS server

    // Advanced Identifiers
    val mediaDeviceId: String,      // MediaDrm Widevine device ID
    val appSetId: String,           // Google App Set ID
    val webViewUserAgent: String,   // User-Agent string matching device profile

    // Metadata
    val profilePreset: String?,     // "random", "pixel8pro", "s24ultra", "custom"
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis()
)

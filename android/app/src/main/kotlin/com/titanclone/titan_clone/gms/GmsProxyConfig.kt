package com.titanclone.titan_clone.gms

/**
 * Configuration for GMS/Play Store proxying per clone.
 *
 * Each clone needs isolated Google services:
 * - Unique GSF ID (prevents cross-clone device correlation)
 * - Separate Google Account (AccountManager isolation)
 * - Independent Play Store registration
 * - Correct installer package name (com.android.vending)
 */
data class GmsProxyConfig(
    val cloneId: String,
    val gsfId: String,
    val advertisingId: String = "",
    val accountName: String? = null,
    val accountType: String? = "com.google",
    val useRealGms: Boolean = true,
    val useMicroG: Boolean = false,
    val playStoreSignature: String? = null,
    val checkinUrl: String? = null
) {
    companion object {
        const val PLAY_STORE_PACKAGE = "com.android.vending"
        const val GMS_PACKAGE = "com.google.android.gms"
        const val GSF_PACKAGE = "com.google.android.gsf"
        const val GSF_PROVIDER_AUTHORITY = "com.google.android.gsf.gservices"

        // Google's Play Store signing certificate SHA-256 (public, used for verification)
        const val PLAY_STORE_CERT_SHA256 =
            "38918A453D07199354F8B19AF05EC6562CED5788F1C2F0A1C44E57B1C582E04F"

        /** GMS-related packages that need special handling */
        val GMS_PACKAGES = setOf(
            PLAY_STORE_PACKAGE,
            GMS_PACKAGE,
            GSF_PACKAGE,
            "com.google.android.gsf.login",
            "com.google.android.apps.maps",
            "com.google.android.youtube",
            "com.google.android.gm",           // Gmail
            "com.google.android.apps.photos",  // Google Photos
            "com.google.android.calendar",     // Calendar
            "com.google.android.contacts",     // Contacts
        )

        /** Authorities that need per-clone redirection */
        val GSF_AUTHORITIES = setOf(
            GSF_PROVIDER_AUTHORITY,
            "com.google.android.gms.auth.accounts",
            "com.google.android.gms.phenotype",
            "com.google.settings",
        )

        fun isGmsPackage(packageName: String): Boolean {
            return packageName in GMS_PACKAGES
        }

        fun isGsfAuthority(authority: String): Boolean {
            return authority in GSF_AUTHORITIES
        }
    }
}

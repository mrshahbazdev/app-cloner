package com.titanclone.titan_clone.gms

data class GmsProxyConfig(
    val cloneId: String,
    val gsfId: String,
    val accountName: String?,
    val useRealGms: Boolean = true,
    val useMicroG: Boolean = false
) {
    companion object {
        const val PLAY_STORE_PACKAGE = "com.android.vending"
        const val GMS_PACKAGE = "com.google.android.gms"
        const val GSF_PACKAGE = "com.google.android.gsf"
        const val GSF_PROVIDER_AUTHORITY = "com.google.android.gsf.gservices"

        fun isGmsPackage(packageName: String): Boolean {
            return packageName in setOf(
                PLAY_STORE_PACKAGE,
                GMS_PACKAGE,
                GSF_PACKAGE,
                "com.google.android.gsf.login",
                "com.google.android.apps.maps",
                "com.google.android.youtube"
            )
        }
    }
}

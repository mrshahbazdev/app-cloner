package com.titanclone.titan_clone.profile.db

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject

/**
 * Profile database backed by SharedPreferences with JSON serialization.
 *
 * Implements VirtualProfileDao for full CRUD operations with
 * anti-correlation queries (check identifier uniqueness).
 *
 * TODO: Migrate to Room @Database when Room dependency is added.
 */
class ProfileDatabase private constructor(context: Context) {

    companion object {
        private const val TAG = "ProfileDatabase"
        private const val PREFS_NAME = "titanclone_profiles_v2"

        @Volatile
        private var instance: ProfileDatabase? = null

        fun getInstance(context: Context): ProfileDatabase {
            return instance ?: synchronized(this) {
                instance ?: ProfileDatabase(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val dao: VirtualProfileDao = ProfileDaoImpl()

    private inner class ProfileDaoImpl : VirtualProfileDao {

        override fun insert(profile: VirtualProfileEntity) {
            val json = serializeProfile(profile)
            prefs.edit().putString(profile.cloneId, json).apply()
            Log.d(TAG, "Inserted profile: ${profile.cloneId}")
        }

        override fun update(profile: VirtualProfileEntity) {
            val updated = profile.copy(lastModifiedAt = System.currentTimeMillis())
            val json = serializeProfile(updated)
            prefs.edit().putString(updated.cloneId, json).apply()
        }

        override fun delete(cloneId: String) {
            prefs.edit().remove(cloneId).apply()
            Log.d(TAG, "Deleted profile: $cloneId")
        }

        override fun getById(cloneId: String): VirtualProfileEntity? {
            val json = prefs.getString(cloneId, null) ?: return null
            return deserializeProfile(json)
        }

        override fun getAll(): List<VirtualProfileEntity> {
            return prefs.all.values.mapNotNull { value ->
                if (value is String) deserializeProfile(value) else null
            }
        }

        override fun getByPackageAndUser(packageName: String, userId: Int): VirtualProfileEntity? {
            val cloneId = "${packageName}_user${userId}"
            return getById(cloneId)
        }

        override fun existsWithAndroidId(androidId: String): Boolean {
            return getAll().any { it.androidId == androidId }
        }

        override fun existsWithImei(imei: String): Boolean {
            return getAll().any { it.imei == imei }
        }

        override fun existsWithMac(mac: String): Boolean {
            return getAll().any { it.macAddress == mac || it.bluetoothMac == mac }
        }

        override fun existsWithGsfId(gsfId: String): Boolean {
            return getAll().any { it.gsfId == gsfId }
        }

        override fun getByPackage(packageName: String): List<VirtualProfileEntity> {
            return getAll().filter { it.packageName == packageName }
        }
    }

    private fun serializeProfile(p: VirtualProfileEntity): String {
        return JSONObject().apply {
            put("cloneId", p.cloneId)
            put("packageName", p.packageName)
            put("userId", p.userId)
            put("androidId", p.androidId)
            put("gsfId", p.gsfId)
            put("advertisingId", p.advertisingId)
            put("deviceModel", p.deviceModel)
            put("manufacturer", p.manufacturer)
            put("brand", p.brand)
            put("product", p.product)
            put("device", p.device)
            put("hardware", p.hardware)
            put("buildFingerprint", p.buildFingerprint)
            put("buildDisplay", p.buildDisplay)
            put("buildId", p.buildId)
            put("buildType", p.buildType)
            put("buildTags", p.buildTags)
            put("serial", p.serial)
            put("bootloader", p.bootloader)
            put("board", p.board)
            put("macAddress", p.macAddress)
            put("bluetoothMac", p.bluetoothMac)
            put("imei", p.imei)
            put("imsi", p.imsi)
            put("simSerial", p.simSerial)
            put("phoneNumber", p.phoneNumber)
            put("carrierName", p.carrierName)
            put("carrierCode", p.carrierCode)
            put("countryIso", p.countryIso)
            put("screenDensity", p.screenDensity)
            put("screenWidth", p.screenWidth)
            put("screenHeight", p.screenHeight)
            put("sdkVersion", p.sdkVersion)
            put("releaseVersion", p.releaseVersion)
            put("securityPatch", p.securityPatch)
            put("codename", p.codename)
            put("incremental", p.incremental)
            put("baseOs", p.baseOs)
            put("previewSdkInt", p.previewSdkInt)
            put("locale", p.locale)
            put("timezone", p.timezone)
            put("proxyHost", p.proxyHost)
            put("proxyPort", p.proxyPort ?: -1)
            put("proxyUser", p.proxyUser)
            put("proxyPass", p.proxyPass)
            put("dnsServer", p.dnsServer)
            put("mediaDeviceId", p.mediaDeviceId)
            put("appSetId", p.appSetId)
            put("webViewUserAgent", p.webViewUserAgent)
            put("profilePreset", p.profilePreset)
            put("createdAt", p.createdAt)
            put("lastModifiedAt", p.lastModifiedAt)
        }.toString()
    }

    private fun deserializeProfile(json: String): VirtualProfileEntity? {
        return try {
            val j = JSONObject(json)
            VirtualProfileEntity(
                cloneId = j.getString("cloneId"),
                packageName = j.getString("packageName"),
                userId = j.getInt("userId"),
                androidId = j.getString("androidId"),
                gsfId = j.getString("gsfId"),
                advertisingId = j.getString("advertisingId"),
                deviceModel = j.getString("deviceModel"),
                manufacturer = j.getString("manufacturer"),
                brand = j.getString("brand"),
                product = j.getString("product"),
                device = j.getString("device"),
                hardware = j.getString("hardware"),
                buildFingerprint = j.getString("buildFingerprint"),
                buildDisplay = j.getString("buildDisplay"),
                buildId = j.getString("buildId"),
                buildType = j.getString("buildType"),
                buildTags = j.getString("buildTags"),
                serial = j.getString("serial"),
                bootloader = j.getString("bootloader"),
                board = j.getString("board"),
                macAddress = j.getString("macAddress"),
                bluetoothMac = j.getString("bluetoothMac"),
                imei = j.getString("imei"),
                imsi = j.getString("imsi"),
                simSerial = j.getString("simSerial"),
                phoneNumber = j.getString("phoneNumber"),
                carrierName = j.getString("carrierName"),
                carrierCode = j.getString("carrierCode"),
                countryIso = j.getString("countryIso"),
                screenDensity = j.getInt("screenDensity"),
                screenWidth = j.getInt("screenWidth"),
                screenHeight = j.getInt("screenHeight"),
                sdkVersion = j.getInt("sdkVersion"),
                releaseVersion = j.getString("releaseVersion"),
                securityPatch = j.getString("securityPatch"),
                codename = j.getString("codename"),
                incremental = j.getString("incremental"),
                baseOs = j.getString("baseOs"),
                previewSdkInt = j.getInt("previewSdkInt"),
                locale = j.getString("locale"),
                timezone = j.getString("timezone"),
                proxyHost = j.optString("proxyHost", null),
                proxyPort = j.optInt("proxyPort", -1).let { if (it == -1) null else it },
                proxyUser = j.optString("proxyUser", null),
                proxyPass = j.optString("proxyPass", null),
                dnsServer = j.optString("dnsServer", null),
                mediaDeviceId = j.getString("mediaDeviceId"),
                appSetId = j.getString("appSetId"),
                webViewUserAgent = j.getString("webViewUserAgent"),
                profilePreset = j.optString("profilePreset", null),
                createdAt = j.getLong("createdAt"),
                lastModifiedAt = j.getLong("lastModifiedAt")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize profile", e)
            null
        }
    }

    init {
        Log.d(TAG, "Profile database initialized (JSON/SharedPreferences mode)")
    }
}

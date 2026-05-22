package com.titanclone.titan_clone.bridge

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.BinaryMessenger
import com.titanclone.titan_clone.profile.VirtualProfileManager
import com.titanclone.titan_clone.profile.ProfileGenerator
import com.titanclone.titan_clone.profile.db.ProfileDatabase

class FlutterBridgePlugin : FlutterPlugin, CloneEngineApi {
    private lateinit var context: Context
    private lateinit var profileManager: VirtualProfileManager
    private lateinit var profileGenerator: ProfileGenerator
    private lateinit var profileDb: ProfileDatabase
    private lateinit var eventChannel: EventChannel

    private var eventSink: EventChannel.EventSink? = null
    private var flutterApi: CloneEventApi? = null
    private var engineInitialized = false
    private var maxConcurrentClones = 5
    private var memoryLimitPerClone = 256

    private val cloneStore = mutableMapOf<String, PigeonCloneInfo>()

    companion object {
        const val EVENT_CHANNEL = "com.titanclone/events"
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        context = binding.applicationContext
        profileDb = ProfileDatabase.getInstance(context)
        profileManager = VirtualProfileManager(context)
        profileGenerator = ProfileGenerator(profileDb.dao)

        CloneEngineApi.setUp(binding.binaryMessenger, this)
        flutterApi = CloneEventApi(binding.binaryMessenger)

        eventChannel = EventChannel(binding.binaryMessenger, EVENT_CHANNEL)
        eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                eventSink = events
            }

            override fun onCancel(arguments: Any?) {
                eventSink = null
            }
        })
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        CloneEngineApi.setUp(binding.binaryMessenger, null)
        flutterApi = null
        eventSink = null
    }

    override fun initializeEngine(callback: (Result<Boolean>) -> Unit) {
        try {
            engineInitialized = true
            flutterApi?.onEngineInitialized(true) {}
            sendEvent(mapOf(
                "eventType" to "engineInitialized",
                "cloneId" to "",
                "data" to mapOf("success" to true)
            ))
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun isEngineReady(): Boolean {
        return engineInitialized
    }

    override fun getEngineStatus(): PigeonEngineStatus {
        val runningCount = cloneStore.values.count { it.status == "running" }
        return PigeonEngineStatus(
            initialized = engineInitialized,
            runningCloneCount = runningCount.toLong(),
            totalCloneCount = cloneStore.size.toLong(),
            memoryUsageMb = (runningCount * 64).toLong()
        )
    }

    override fun getInstalledApps(callback: (Result<List<PigeonInstalledApp>>) -> Unit) {
        try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                .map { appInfo ->
                    val packageInfo = try {
                        pm.getPackageInfo(appInfo.packageName, 0)
                    } catch (_: PackageManager.NameNotFoundException) {
                        null
                    }

                    val category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        when (appInfo.category) {
                            ApplicationInfo.CATEGORY_SOCIAL -> "social"
                            ApplicationInfo.CATEGORY_GAME -> "games"
                            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "productivity"
                            ApplicationInfo.CATEGORY_VIDEO -> "media"
                            ApplicationInfo.CATEGORY_AUDIO -> "media"
                            ApplicationInfo.CATEGORY_IMAGE -> "media"
                            ApplicationInfo.CATEGORY_NEWS -> "news"
                            ApplicationInfo.CATEGORY_MAPS -> "tools"
                            else -> "other"
                        }
                    } else {
                        "other"
                    }

                    PigeonInstalledApp(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo)?.toString() ?: appInfo.packageName,
                        versionName = packageInfo?.versionName,
                        versionCode = packageInfo?.let {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                it.longVersionCode
                            } else {
                                @Suppress("DEPRECATION")
                                it.versionCode.toLong()
                            }
                        },
                        isSystemApp = false,
                        isSplitApk = appInfo.splitSourceDirs?.isNotEmpty() == true,
                        category = category
                    )
                }
            callback(Result.success(apps))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun createClone(
        packageName: String,
        userId: Long,
        profilePreset: String?,
        callback: (Result<PigeonCloneInfo>) -> Unit
    ) {
        try {
            val profile = profileGenerator.generateProfile(userId.toInt())
            profileManager.saveProfile(packageName, userId.toInt(), profile)

            val cloneId = "${packageName}_clone_${userId}"
            val now = System.currentTimeMillis()

            val cloneInfo = PigeonCloneInfo(
                id = cloneId,
                packageName = packageName,
                appName = getAppLabel(packageName),
                userId = userId,
                status = "installing",
                createdAtMs = now
            )

            cloneStore[cloneId] = cloneInfo

            flutterApi?.onInstallProgress(cloneId, 0) {}

            // Simulate install progress
            Thread {
                for (i in 1..10) {
                    Thread.sleep(100)
                    flutterApi?.onInstallProgress(cloneId, (i * 10).toLong()) {}
                    sendEvent(mapOf(
                        "eventType" to "install_progress",
                        "cloneId" to cloneId,
                        "data" to mapOf("percent" to i * 10)
                    ))
                }
                val readyClone = PigeonCloneInfo(
                    id = cloneId,
                    packageName = packageName,
                    appName = getAppLabel(packageName),
                    userId = userId,
                    status = "ready",
                    createdAtMs = now
                )
                cloneStore[cloneId] = readyClone
                flutterApi?.onCloneStatusChanged(cloneId, "ready") {}
                sendEvent(mapOf(
                    "eventType" to "ready",
                    "cloneId" to cloneId,
                    "message" to "Clone installed"
                ))
            }.start()

            callback(Result.success(cloneInfo))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun launchClone(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            val existing = cloneStore[cloneId]
            if (existing != null) {
                cloneStore[cloneId] = PigeonCloneInfo(
                    id = existing.id,
                    packageName = existing.packageName,
                    appName = existing.appName,
                    userId = existing.userId,
                    status = "running",
                    createdAtMs = existing.createdAtMs,
                    lastLaunchedMs = System.currentTimeMillis()
                )
            }
            flutterApi?.onCloneStatusChanged(cloneId, "running") {}
            sendEvent(mapOf(
                "eventType" to "running",
                "cloneId" to cloneId,
                "message" to "Clone launched"
            ))
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun stopClone(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            val existing = cloneStore[cloneId]
            if (existing != null) {
                cloneStore[cloneId] = PigeonCloneInfo(
                    id = existing.id,
                    packageName = existing.packageName,
                    appName = existing.appName,
                    userId = existing.userId,
                    status = "stopped",
                    createdAtMs = existing.createdAtMs,
                    lastLaunchedMs = existing.lastLaunchedMs
                )
            }
            flutterApi?.onCloneStatusChanged(cloneId, "stopped") {}
            sendEvent(mapOf(
                "eventType" to "stopped",
                "cloneId" to cloneId,
                "message" to "Clone stopped"
            ))
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun deleteClone(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            cloneStore.remove(cloneId)
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun getClones(callback: (Result<List<PigeonCloneInfo>>) -> Unit) {
        try {
            callback(Result.success(cloneStore.values.toList()))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun getCloneStatus(cloneId: String, callback: (Result<String>) -> Unit) {
        try {
            val clone = cloneStore[cloneId]
            callback(Result.success(clone?.status ?: "stopped"))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun getCloneProfile(cloneId: String, callback: (Result<PigeonDeviceProfile?>) -> Unit) {
        try {
            val profileData = profileManager.getProfile(cloneId)
            if (profileData != null) {
                val profile = PigeonDeviceProfile(
                    id = profileData["id"]?.toString() ?: cloneId,
                    name = profileData["name"]?.toString() ?: "Unknown",
                    model = profileData["model"]?.toString() ?: "",
                    brand = profileData["brand"]?.toString() ?: "",
                    manufacturer = profileData["manufacturer"]?.toString() ?: "",
                    fingerprint = profileData["fingerprint"]?.toString() ?: "",
                    screenDensity = (profileData["screenDensity"] as? Number)?.toLong() ?: 420,
                    screenWidth = (profileData["screenWidth"] as? Number)?.toLong() ?: 1080,
                    screenHeight = (profileData["screenHeight"] as? Number)?.toLong() ?: 2400,
                    sdkVersion = (profileData["sdkVersion"] as? Number)?.toLong() ?: 34,
                    releaseVersion = profileData["releaseVersion"]?.toString() ?: "14",
                    androidId = profileData["androidId"]?.toString() ?: "",
                    imei = profileData["imei"]?.toString() ?: "",
                    macAddress = profileData["macAddress"]?.toString() ?: "",
                    bluetoothMac = profileData["bluetoothMac"]?.toString() ?: "",
                    gsfId = profileData["gsfId"]?.toString() ?: "",
                    advertisingId = profileData["advertisingId"]?.toString() ?: "",
                    serialNumber = profileData["serialNumber"]?.toString(),
                    timezone = profileData["timezone"]?.toString(),
                    locale = profileData["locale"]?.toString(),
                    proxyHost = profileData["proxyHost"]?.toString(),
                    proxyPort = (profileData["proxyPort"] as? Number)?.toLong(),
                    proxyType = profileData["proxyType"]?.toString()
                )
                callback(Result.success(profile))
            } else {
                callback(Result.success(null))
            }
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun updateProfile(
        cloneId: String,
        profile: PigeonDeviceProfile,
        callback: (Result<Boolean>) -> Unit
    ) {
        try {
            val profileMap = mapOf(
                "id" to profile.id,
                "name" to profile.name,
                "model" to profile.model,
                "brand" to profile.brand,
                "manufacturer" to profile.manufacturer,
                "fingerprint" to profile.fingerprint,
                "screenDensity" to profile.screenDensity,
                "screenWidth" to profile.screenWidth,
                "screenHeight" to profile.screenHeight,
                "sdkVersion" to profile.sdkVersion,
                "releaseVersion" to profile.releaseVersion,
                "androidId" to profile.androidId,
                "imei" to profile.imei,
                "macAddress" to profile.macAddress,
                "bluetoothMac" to profile.bluetoothMac,
                "gsfId" to profile.gsfId,
                "advertisingId" to profile.advertisingId
            )
            profileManager.updateProfile(cloneId, profileMap)
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun resetCloneProfile(
        cloneId: String,
        callback: (Result<PigeonDeviceProfile>) -> Unit
    ) {
        try {
            val parts = cloneId.split("_clone_")
            val userId = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val newProfile = profileGenerator.generateProfile(userId)
            val packageName = parts.getOrNull(0) ?: ""
            profileManager.saveProfile(packageName, userId, newProfile)

            val profile = PigeonDeviceProfile(
                id = cloneId,
                name = newProfile["name"]?.toString() ?: "Random Device",
                model = newProfile["model"]?.toString() ?: "",
                brand = newProfile["brand"]?.toString() ?: "",
                manufacturer = newProfile["manufacturer"]?.toString() ?: "",
                fingerprint = newProfile["fingerprint"]?.toString() ?: "",
                screenDensity = (newProfile["screenDensity"] as? Number)?.toLong() ?: 420,
                screenWidth = (newProfile["screenWidth"] as? Number)?.toLong() ?: 1080,
                screenHeight = (newProfile["screenHeight"] as? Number)?.toLong() ?: 2400,
                sdkVersion = (newProfile["sdkVersion"] as? Number)?.toLong() ?: 34,
                releaseVersion = newProfile["releaseVersion"]?.toString() ?: "14",
                androidId = newProfile["androidId"]?.toString() ?: "",
                imei = newProfile["imei"]?.toString() ?: "",
                macAddress = newProfile["macAddress"]?.toString() ?: "",
                bluetoothMac = newProfile["bluetoothMac"]?.toString() ?: "",
                gsfId = newProfile["gsfId"]?.toString() ?: "",
                advertisingId = newProfile["advertisingId"]?.toString() ?: ""
            )
            callback(Result.success(profile))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun getCloneStorageInfo(
        cloneId: String,
        callback: (Result<PigeonStorageInfo>) -> Unit
    ) {
        try {
            val storageInfo = PigeonStorageInfo(
                cloneId = cloneId,
                totalSizeBytes = 0,
                dataSizeBytes = 0,
                cacheSizeBytes = 0
            )
            callback(Result.success(storageInfo))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun clearCloneCache(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun clearCloneData(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun setMaxConcurrentClones(maxClones: Long): Boolean {
        maxConcurrentClones = maxClones.toInt()
        return true
    }

    override fun setMemoryLimitPerClone(limitMb: Long): Boolean {
        memoryLimitPerClone = limitMb.toInt()
        return true
    }

    private fun getAppLabel(packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun sendEvent(event: Map<String, Any?>) {
        eventSink?.success(event)
    }
}

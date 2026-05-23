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
import com.titanclone.titan_clone.gms.GmsServiceProxy
import com.titanclone.titan_clone.gms.PlayStoreCloneManager
import com.titanclone.titan_clone.gms.CheckinInterceptor
import com.titanclone.titan_clone.gms.AccountIsolationManager
import com.titanclone.titan_clone.gms.FcmIsolationManager
import com.titanclone.titan_clone.gms.MicroGManager
import com.titanclone.titan_clone.compat.VersionCompatHandler
import com.titanclone.titan_clone.service.BackgroundProcessManager
import com.titanclone.titan_clone.service.CloneForegroundService
import com.titanclone.titan_clone.notification.CloneNotificationManager
import com.titanclone.titan_clone.optimization.MemoryOptimizer
import com.titanclone.titan_clone.optimization.StartupOptimizer
import com.titanclone.titan_clone.optimization.BatteryOptimizer
import com.titanclone.titan_clone.gms.GmsProxyConfig
import com.titanclone.titan_clone.security.CodeProtection
import com.titanclone.titan_clone.security.DataSecurity
import com.titanclone.engine.core.VirtualCore
import com.titanclone.engine.stub.StubActivity

class FlutterBridgePlugin : FlutterPlugin, CloneEngineApi {
    private lateinit var context: Context
    private lateinit var profileManager: VirtualProfileManager
    private lateinit var profileGenerator: ProfileGenerator
    private lateinit var profileDb: ProfileDatabase
    private lateinit var eventChannel: EventChannel
    private lateinit var gmsProxy: GmsServiceProxy
    private lateinit var playStoreManager: PlayStoreCloneManager
    private lateinit var checkinInterceptor: CheckinInterceptor
    private lateinit var accountIsolation: AccountIsolationManager
    private lateinit var fcmIsolation: FcmIsolationManager
    private lateinit var microGManager: MicroGManager
    private lateinit var versionCompat: VersionCompatHandler
    private lateinit var backgroundManager: BackgroundProcessManager
    private lateinit var notificationManager: CloneNotificationManager
    private lateinit var memoryOptimizer: MemoryOptimizer
    private lateinit var startupOptimizer: StartupOptimizer
    private lateinit var batteryOptimizer: BatteryOptimizer
    private lateinit var codeProtection: CodeProtection
    private lateinit var dataSecurity: DataSecurity

    private var eventSink: EventChannel.EventSink? = null
    private var flutterApi: CloneEventApi? = null
    private var engineInitialized = false
    private var maxConcurrentClones = 5
    private var memoryLimitPerClone = 256

    private val cloneStore = mutableMapOf<String, CloneInfo>()
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    companion object {
        const val EVENT_CHANNEL = "com.titanclone/events"
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        context = binding.applicationContext
        profileDb = ProfileDatabase.getInstance(context)
        profileManager = VirtualProfileManager(context)
        profileGenerator = ProfileGenerator(profileDb.dao)

        gmsProxy = GmsServiceProxy(context)
        microGManager = MicroGManager(context)
        playStoreManager = PlayStoreCloneManager(context, gmsProxy, profileManager, profileGenerator)
        checkinInterceptor = CheckinInterceptor(context, profileManager)
        accountIsolation = AccountIsolationManager(context)
        fcmIsolation = FcmIsolationManager(context)
        versionCompat = VersionCompatHandler(context)
        backgroundManager = BackgroundProcessManager(context)
        notificationManager = CloneNotificationManager(context)
        notificationManager.createForegroundChannel()
        memoryOptimizer = MemoryOptimizer(context)
        startupOptimizer = StartupOptimizer(context)
        batteryOptimizer = BatteryOptimizer(context)
        codeProtection = CodeProtection(context)
        dataSecurity = DataSecurity(context)
        context.registerComponentCallbacks(memoryOptimizer)
        startupOptimizer.markEngineInitStart()

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
            restoreClonesFromProfiles()
            engineInitialized = true
            mainHandler.post {
                flutterApi?.onEngineInitialized(true) {}
                sendEvent(mapOf(
                    "eventType" to "engineInitialized",
                    "cloneId" to "",
                    "message" to "Virtual Engine is ready"
                ))
            }
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun isEngineReady(): Boolean {
        return engineInitialized
    }

    override fun getEngineStatus(): EngineStatus {
        val runningCount = cloneStore.values.count { it.status == "running" }
        return EngineStatus(
            initialized = engineInitialized,
            runningCloneCount = runningCount.toLong(),
            totalCloneCount = cloneStore.size.toLong(),
            memoryUsageMb = (runningCount * 64).toLong()
        )
    }

    override fun getInstalledApps(callback: (Result<List<InstalledApp>>) -> Unit) {
        try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM == 0) || it.packageName == "com.android.vending" }
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

                    InstalledApp(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo)?.toString() ?: appInfo.packageName,
                        iconPath = getIconPathForApp(appInfo.packageName),
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
        callback: (Result<CloneInfo>) -> Unit
    ) {
        try {
            val profile = profileManager.getOrCreateProfile(packageName, userId.toInt())
            val cloneId = "${packageName}_clone_${userId}"
            val now = System.currentTimeMillis()

            val deviceProfile = DeviceProfile(
                id = cloneId,
                name = profile.profilePreset ?: "Unknown Preset",
                model = profile.deviceModel,
                brand = profile.brand,
                manufacturer = profile.manufacturer,
                fingerprint = profile.buildFingerprint,
                screenDensity = profile.screenDensity.toLong(),
                screenWidth = profile.screenWidth.toLong(),
                screenHeight = profile.screenHeight.toLong(),
                sdkVersion = profile.sdkVersion.toLong(),
                releaseVersion = profile.releaseVersion,
                androidId = profile.androidId,
                imei = profile.imei,
                macAddress = profile.macAddress,
                bluetoothMac = profile.bluetoothMac,
                gsfId = profile.gsfId,
                advertisingId = profile.advertisingId,
                serialNumber = profile.serial,
                timezone = profile.timezone,
                locale = profile.locale,
                proxyHost = profile.proxyHost,
                proxyPort = profile.proxyPort?.toLong(),
                proxyType = "SOCKS5"
            )
            val profileJsonString = serializeDeviceProfileToJson(deviceProfile)
            val appIconPath = getIconPathForApp(packageName)

            val cloneInfo = CloneInfo(
                id = cloneId,
                packageName = packageName,
                appName = getAppLabel(packageName),
                userId = userId,
                status = "installing",
                createdAtMs = now,
                profileJson = profileJsonString,
                appIconPath = appIconPath
            )

            cloneStore[cloneId] = cloneInfo

            mainHandler.post {
                flutterApi?.onInstallProgress(cloneId, 0) {}
            }

            // Run real installer in background thread
            Thread {
                try {
                    // Update progress: 10%
                    mainHandler.post {
                        flutterApi?.onInstallProgress(cloneId, 10) {}
                        sendEvent(mapOf(
                            "eventType" to "install_progress",
                            "cloneId" to cloneId,
                            "data" to mapOf("percent" to 10L)
                        ))
                    }

                    // Perform the real virtual package installation!
                    val vpInfo = VirtualCore.get().packageManager.getPackageInfo(packageName, userId.toInt())
                    if (vpInfo == null) {
                        val success = VirtualCore.get().installPackageAsUser(packageName, userId.toInt())
                        if (!success) {
                            throw Exception("Virtual Package Installer failed to clone package: $packageName")
                        }
                    }

                    // Update progress: 90%
                    mainHandler.post {
                        flutterApi?.onInstallProgress(cloneId, 90) {}
                        sendEvent(mapOf(
                            "eventType" to "install_progress",
                            "cloneId" to cloneId,
                            "data" to mapOf("percent" to 90L)
                        ))
                    }

                    Thread.sleep(200) // Small delay for visual smooth transition

                    val readyClone = CloneInfo(
                        id = cloneId,
                        packageName = packageName,
                        appName = getAppLabel(packageName),
                        userId = userId,
                        status = "ready",
                        createdAtMs = now,
                        profileJson = profileJsonString,
                        appIconPath = appIconPath
                    )
                    cloneStore[cloneId] = readyClone
                    mainHandler.post {
                        flutterApi?.onInstallProgress(cloneId, 100) {}
                        flutterApi?.onCloneStatusChanged(cloneId, "ready") {}
                        sendEvent(mapOf(
                            "eventType" to "ready",
                            "cloneId" to cloneId,
                            "message" to "Clone installed"
                        ))
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FlutterBridgePlugin", "Failed to install clone $cloneId", e)
                    val errorClone = CloneInfo(
                        id = cloneId,
                        packageName = packageName,
                        appName = getAppLabel(packageName),
                        userId = userId,
                        status = "failed",
                        createdAtMs = now,
                        profileJson = profileJsonString,
                        appIconPath = appIconPath
                    )
                    cloneStore[cloneId] = errorClone
                    mainHandler.post {
                        flutterApi?.onCloneStatusChanged(cloneId, "failed") {}
                        sendEvent(mapOf(
                            "eventType" to "failed",
                            "cloneId" to cloneId,
                            "message" to "Installation failed: ${e.message}"
                        ))
                    }
                }
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
                cloneStore[cloneId] = CloneInfo(
                    id = existing.id,
                    packageName = existing.packageName,
                    appName = existing.appName,
                    userId = existing.userId,
                    status = "running",
                    createdAtMs = existing.createdAtMs,
                    lastLaunchedMs = System.currentTimeMillis(),
                    profileJson = existing.profileJson,
                    appIconPath = existing.appIconPath
                )

                val packageName = existing.packageName
                val userId = existing.userId.toInt()

                // 1. Ensure package is installed in virtual space (if not already installed)
                if (!VirtualCore.get().packageManager.isPackageInstalled(packageName, userId)) {
                    val installed = VirtualCore.get().installPackageAsUser(packageName, userId)
                    if (!installed) {
                        callback(Result.failure(Exception("Failed to install package $packageName in virtual environment")))
                        return
                    }
                }

                // 2. Launch in virtual process slot
                val success = VirtualCore.get().launchApp(packageName, userId)
                if (!success) {
                    callback(Result.failure(Exception("Failed to launch cloned app: process slots full or initialization failed")))
                    return
                }

                // 3. Get process index and launch StubActivity wrapped intent
                val cloneIdStr = "${packageName}_user${userId}"
                val record = VirtualCore.get().processManager.getProcessRecord(cloneIdStr)
                if (record == null) {
                    callback(Result.failure(Exception("Failed to retrieve process slot for $cloneIdStr")))
                    return
                }

                // Get launch intent for cloned app's original package
                val pm = context.packageManager
                val realIntent = pm.getLaunchIntentForPackage(packageName)
                if (realIntent != null) {
                    val stubClass = StubActivity.getStubActivityClass(record.processIndex)
                    val stubIntent = StubActivity.wrap(realIntent, userId, stubClass)
                    stubIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(stubIntent)
                } else {
                    android.util.Log.e("FlutterBridgePlugin", "Launch intent not found for original package: $packageName")
                    callback(Result.failure(Exception("Launch intent not found for $packageName")))
                    return
                }
            }
            mainHandler.post {
                flutterApi?.onCloneStatusChanged(cloneId, "running") {}
                sendEvent(mapOf(
                    "eventType" to "running",
                    "cloneId" to cloneId,
                    "message" to "Clone launched"
                ))
            }
            mainHandler.post { callback(Result.success(true)) }
        } catch (e: Exception) {
            mainHandler.post { callback(Result.failure(e)) }
        }
    }

    override fun stopClone(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            val existing = cloneStore[cloneId]
            if (existing != null) {
                cloneStore[cloneId] = CloneInfo(
                    id = existing.id,
                    packageName = existing.packageName,
                    appName = existing.appName,
                    userId = existing.userId,
                    status = "stopped",
                    createdAtMs = existing.createdAtMs,
                    lastLaunchedMs = existing.lastLaunchedMs,
                    profileJson = existing.profileJson,
                    appIconPath = existing.appIconPath
                )
            }
            mainHandler.post {
                flutterApi?.onCloneStatusChanged(cloneId, "stopped") {}
                sendEvent(mapOf(
                    "eventType" to "stopped",
                    "cloneId" to cloneId,
                    "message" to "Clone stopped"
                ))
            }
            mainHandler.post { callback(Result.success(true)) }
        } catch (e: Exception) {
            mainHandler.post { callback(Result.failure(e)) }
        }
    }


    override fun deleteClone(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            cloneStore.remove(cloneId)
            profileManager.deleteProfile(normalizeCloneId(cloneId))
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun getClones(callback: (Result<List<CloneInfo>>) -> Unit) {
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

    override fun getCloneProfile(cloneId: String, callback: (Result<DeviceProfile?>) -> Unit) {
        try {
            val normalizedId = normalizeCloneId(cloneId)
            val profileData = profileManager.getProfile(normalizedId)
            if (profileData != null) {
                val profile = DeviceProfile(
                    id = cloneId,
                    name = profileData.profilePreset ?: "Unknown",
                    model = profileData.deviceModel,
                    brand = profileData.brand,
                    manufacturer = profileData.manufacturer,
                    fingerprint = profileData.buildFingerprint,
                    screenDensity = profileData.screenDensity.toLong(),
                    screenWidth = profileData.screenWidth.toLong(),
                    screenHeight = profileData.screenHeight.toLong(),
                    sdkVersion = profileData.sdkVersion.toLong(),
                    releaseVersion = profileData.releaseVersion,
                    androidId = profileData.androidId,
                    imei = profileData.imei,
                    macAddress = profileData.macAddress,
                    bluetoothMac = profileData.bluetoothMac,
                    gsfId = profileData.gsfId,
                    advertisingId = profileData.advertisingId,
                    serialNumber = profileData.serial,
                    timezone = profileData.timezone,
                    locale = profileData.locale,
                    proxyHost = profileData.proxyHost,
                    proxyPort = profileData.proxyPort?.toLong(),
                    proxyType = "SOCKS5"
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
        profile: DeviceProfile,
        callback: (Result<Boolean>) -> Unit
    ) {
        try {
            val normalizedId = normalizeCloneId(cloneId)
            val existing = profileManager.getProfile(normalizedId)
            if (existing != null) {
                val updated = existing.copy(
                    deviceModel = profile.model,
                    brand = profile.brand,
                    manufacturer = profile.manufacturer,
                    buildFingerprint = profile.fingerprint,
                    screenDensity = profile.screenDensity.toInt(),
                    screenWidth = profile.screenWidth.toInt(),
                    screenHeight = profile.screenHeight.toInt(),
                    sdkVersion = profile.sdkVersion.toInt(),
                    releaseVersion = profile.releaseVersion,
                    androidId = profile.androidId,
                    imei = profile.imei,
                    macAddress = profile.macAddress,
                    bluetoothMac = profile.bluetoothMac,
                    gsfId = profile.gsfId,
                    advertisingId = profile.advertisingId,
                    serial = profile.serialNumber ?: existing.serial,
                    timezone = profile.timezone ?: existing.timezone,
                    locale = profile.locale ?: existing.locale,
                    proxyHost = profile.proxyHost,
                    proxyPort = profile.proxyPort?.toInt()
                )
                profileManager.updateProfile(updated)
                
                val updatedDeviceProfile = profile.copy(id = cloneId)
                val existingClone = cloneStore[cloneId]
                if (existingClone != null) {
                    cloneStore[cloneId] = CloneInfo(
                        id = existingClone.id,
                        packageName = existingClone.packageName,
                        appName = existingClone.appName,
                        userId = existingClone.userId,
                        status = existingClone.status,
                        createdAtMs = existingClone.createdAtMs,
                        profileJson = serializeDeviceProfileToJson(updatedDeviceProfile),
                        appIconPath = existingClone.appIconPath,
                        memoryUsageMb = existingClone.memoryUsageMb,
                        lastLaunchedMs = existingClone.lastLaunchedMs,
                        storageSizeBytes = existingClone.storageSizeBytes
                    )
                }
                
                callback(Result.success(true))
            } else {
                callback(Result.success(false))
            }
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }


    override fun resetCloneProfile(
        cloneId: String,
        callback: (Result<DeviceProfile>) -> Unit
    ) {
        try {
            val normalizedId = normalizeCloneId(cloneId)
            val parts = normalizedId.split("_user")
            val userId = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val packageName = parts.getOrNull(0) ?: ""
            val randomPresetIndex = (0 until profileGenerator.getPresetCount()).random()
            val newProfile = profileGenerator.generateProfileWithPreset(packageName, userId, randomPresetIndex.toString())
            profileManager.updateProfile(newProfile)

            val profile = DeviceProfile(
                id = cloneId,
                name = newProfile.profilePreset ?: "Random Device",
                model = newProfile.deviceModel,
                brand = newProfile.brand,
                manufacturer = newProfile.manufacturer,
                fingerprint = newProfile.buildFingerprint,
                screenDensity = newProfile.screenDensity.toLong(),
                screenWidth = newProfile.screenWidth.toLong(),
                screenHeight = newProfile.screenHeight.toLong(),
                sdkVersion = newProfile.sdkVersion.toLong(),
                releaseVersion = newProfile.releaseVersion,
                androidId = newProfile.androidId,
                imei = newProfile.imei,
                macAddress = newProfile.macAddress,
                bluetoothMac = newProfile.bluetoothMac,
                gsfId = newProfile.gsfId,
                advertisingId = newProfile.advertisingId,
                serialNumber = newProfile.serial,
                timezone = newProfile.timezone,
                locale = newProfile.locale,
                proxyHost = newProfile.proxyHost,
                proxyPort = newProfile.proxyPort?.toLong(),
                proxyType = "SOCKS5"
            )
            
            val existingClone = cloneStore[cloneId]
            if (existingClone != null) {
                cloneStore[cloneId] = CloneInfo(
                    id = existingClone.id,
                    packageName = existingClone.packageName,
                    appName = existingClone.appName,
                    userId = existingClone.userId,
                    status = existingClone.status,
                    createdAtMs = existingClone.createdAtMs,
                    profileJson = serializeDeviceProfileToJson(profile),
                    appIconPath = existingClone.appIconPath,
                    memoryUsageMb = existingClone.memoryUsageMb,
                    lastLaunchedMs = existingClone.lastLaunchedMs,
                    storageSizeBytes = existingClone.storageSizeBytes
                )
            }
            
            callback(Result.success(profile))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    private fun getFolderSize(file: java.io.File): Long {
        var size = 0L
        if (file.isDirectory) {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    size += getFolderSize(child)
                }
            }
        } else {
            size = file.length()
        }
        return size
    }

    override fun getCloneStorageInfo(
        cloneId: String,
        callback: (Result<StorageInfo>) -> Unit
    ) {
        try {
            val cloneInfo = cloneStore[cloneId]
            if (cloneInfo == null) {
                callback(Result.failure(Exception("Clone not found")))
                return
            }
            val packageName = cloneInfo.packageName
            val userId = cloneInfo.userId.toInt()

            var total = 0L
            var data = 0L
            var cache = 0L

            val vpInfo = VirtualCore.get().packageManager.getPackageInfo(packageName, userId)
            if (vpInfo != null) {
                val dataDir = VirtualCore.get().storage.getCloneDataDir(packageName, userId)
                if (dataDir.exists()) {
                    data = getFolderSize(dataDir)
                    val cacheDir = java.io.File(dataDir, "cache")
                    if (cacheDir.exists()) {
                        cache = getFolderSize(cacheDir)
                    }
                }
                total = vpInfo.totalSizeBytes + data
            }

            val storageInfo = StorageInfo(
                cloneId = cloneId,
                totalSizeBytes = total,
                dataSizeBytes = data,
                cacheSizeBytes = cache
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

    override fun getGmsState(): GmsState {
        val gmsState = gmsProxy.detectGmsState()
        val precheck = playStoreManager.canCreatePlayStoreClone()
        return GmsState(
            gmsAvailable = gmsState.available,
            gmsVersion = gmsState.gmsVersion,
            playStoreVersion = gmsState.playStoreVersion,
            gsfAvailable = gmsState.gsfAvailable,
            maxPlayStoreClones = PlayStoreCloneManager.MAX_PLAY_STORE_CLONES.toLong(),
            activePlayStoreClones = playStoreManager.getPlayStoreClones().size.toLong()
        )
    }

    override fun createPlayStoreClone(devicePreset: String?, callback: (Result<String?>) -> Unit) {
        try {
            val cloneId = playStoreManager.createPlayStoreClone(devicePreset)
            if (cloneId != null) {
                val profileData = profileManager.getProfile(cloneId)
                val profileJsonString = if (profileData != null) {
                    val deviceProfile = DeviceProfile(
                        id = cloneId,
                        name = profileData.profilePreset ?: devicePreset ?: "Google Pixel 8 Pro",
                        model = profileData.deviceModel,
                        brand = profileData.brand,
                        manufacturer = profileData.manufacturer,
                        fingerprint = profileData.buildFingerprint,
                        screenDensity = profileData.screenDensity.toLong(),
                        screenWidth = profileData.screenWidth.toLong(),
                        screenHeight = profileData.screenHeight.toLong(),
                        sdkVersion = profileData.sdkVersion.toLong(),
                        releaseVersion = profileData.releaseVersion,
                        androidId = profileData.androidId,
                        imei = profileData.imei,
                        macAddress = profileData.macAddress,
                        bluetoothMac = profileData.bluetoothMac,
                        gsfId = profileData.gsfId,
                        advertisingId = profileData.advertisingId,
                        serialNumber = profileData.serial,
                        timezone = profileData.timezone,
                        locale = profileData.locale,
                        proxyHost = profileData.proxyHost,
                        proxyPort = profileData.proxyPort?.toLong(),
                        proxyType = "SOCKS5"
                    )
                    serializeDeviceProfileToJson(deviceProfile)
                } else null

                val cloneInfo = CloneInfo(
                    id = cloneId,
                    packageName = "com.android.vending",
                    appName = "Google Play Store",
                    userId = 0L,
                    status = "ready",
                    createdAtMs = System.currentTimeMillis(),
                    profileJson = profileJsonString,
                    appIconPath = getIconPathForApp("com.android.vending")
                )
                cloneStore[cloneId] = cloneInfo
            }
            callback(Result.success(cloneId))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun deletePlayStoreClone(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            cloneStore.remove(cloneId)
            val result = playStoreManager.deletePlayStoreClone(cloneId)
            profileManager.deleteProfile(cloneId)
            callback(Result.success(result))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun checkCompatibility(): CompatReport {
        val report = versionCompat.checkCompatibility()
        return CompatReport(
            apiLevel = report.apiLevel.toLong(),
            androidVersion = report.androidVersion,
            isSupported = report.isSupported,
            issues = report.issues.map { "${it.severity}: ${it.description}" },
            missingPermissions = report.missingPermissions,
            recommendations = report.recommendations
        )
    }

    override fun getBatteryOptimizationInfo(): BatteryInfo {
        val info = backgroundManager.detectBatteryOptimization()
        return BatteryInfo(
            isIgnoringOptimization = info.isIgnoringBatteryOptimization,
            oemBrand = info.oemBrand,
            oemIssue = info.oemIssue?.name
        )
    }

    override fun startForegroundService(runningCount: Long) {
        CloneForegroundService.start(context, runningCount.toInt())
    }

    override fun stopForegroundService() {
        CloneForegroundService.stop(context)
    }

    override fun getMemorySnapshot(): MemorySnapshot {
        val snapshot = memoryOptimizer.getMemorySnapshot()
        return MemorySnapshot(
            totalDeviceRamMb = snapshot.totalDeviceRamMb,
            availableRamMb = snapshot.availableRamMb,
            engineNativeHeapMb = snapshot.engineNativeHeapMb,
            engineJavaHeapMb = snapshot.engineJavaHeapMb,
            cloneProcessCount = snapshot.cloneProcessCount.toLong(),
            estimatedCloneOverheadMb = snapshot.estimatedCloneOverheadMb,
            isLowMemory = snapshot.isLowMemory,
            recommendedMaxClones = memoryOptimizer.getRecommendedMaxClones().toLong()
        )
    }

    override fun performSecurityCheck(): SecurityStatus {
        val status = codeProtection.performSecurityCheck()
        return SecurityStatus(
            signatureValid = status.signatureValid,
            debuggerAttached = status.debuggerAttached,
            deviceRooted = status.deviceRooted,
            isEmulator = status.isEmulator,
            nativeLibsIntact = status.nativeLibsIntact,
            overallSecure = status.overallSecure
        )
    }

    override fun getPerformanceMetrics(): PerformanceMetrics {
        val timings = startupOptimizer.getAverageTimings()
        val battery = batteryOptimizer.getBatteryStatus()
        return PerformanceMetrics(
            avgColdLaunchMs = (timings["avgColdLaunchMs"] ?: 0L),
            avgWarmLaunchMs = (timings["avgWarmLaunchMs"] ?: 0L),
            avgProfileLoadMs = (timings["avgProfileLoadMs"] ?: 0L),
            totalLaunches = (timings["totalLaunches"] ?: 0L),
            batteryLevel = battery.level.toLong(),
            isCharging = battery.isCharging,
            powerRecommendation = battery.recommendation.name
        )
    }

    override fun canLaunchClone(): Boolean {
        return memoryOptimizer.canLaunchClone()
    }

    override fun requestGc() {
        memoryOptimizer.requestGc()
    }

    override fun encryptCloneData(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            val result = dataSecurity.encryptCloneData(cloneId)
            callback(Result.success(result))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun secureDeleteCloneData(cloneId: String, callback: (Result<Boolean>) -> Unit) {
        try {
            val result = dataSecurity.secureDeleteCloneData(cloneId)
            callback(Result.success(result))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
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
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            eventSink?.success(event)
        } else {
            mainHandler.post {
                eventSink?.success(event)
            }
        }
    }

    private fun normalizeCloneId(cloneId: String): String {
        if (cloneId.contains("_clone_")) {
            return cloneId.replace("_clone_", "_user")
        }
        return cloneId
    }

    private fun getIconPathForApp(packageName: String): String? {
        try {
            val cacheFile = java.io.File(context.cacheDir, "app_icon_${packageName}.png")
            if (cacheFile.exists()) {
                return cacheFile.absolutePath
            }
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val drawable = pm.getApplicationIcon(appInfo)
            
            var width = drawable.intrinsicWidth.let { if (it <= 0) 128 else it }
            var height = drawable.intrinsicHeight.let { if (it <= 0) 128 else it }
            
            // Limit icon size to prevent OutOfMemoryError for badly behaving apps
            if (width > 512) width = 512
            if (height > 512) height = 512
            
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            
            java.io.FileOutputStream(cacheFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
            return cacheFile.absolutePath
        } catch (t: Throwable) {
            android.util.Log.e("FlutterBridgePlugin", "Failed to extract app icon for $packageName", t)
            return null
        }
    }

    private fun serializeDeviceProfileToJson(p: DeviceProfile): String {
        return org.json.JSONObject().apply {
            put("id", p.id)
            put("name", p.name)
            put("model", p.model)
            put("brand", p.brand)
            put("manufacturer", p.manufacturer)
            put("fingerprint", p.fingerprint)
            put("screenDensity", p.screenDensity)
            put("screenWidth", p.screenWidth)
            put("screenHeight", p.screenHeight)
            put("sdkVersion", p.sdkVersion)
            put("releaseVersion", p.releaseVersion)
            put("androidId", p.androidId)
            put("imei", p.imei)
            put("macAddress", p.macAddress)
            put("bluetoothMac", p.bluetoothMac)
            put("gsfId", p.gsfId)
            put("advertisingId", p.advertisingId)
            put("serialNumber", p.serialNumber)
            put("timezone", p.timezone)
            put("locale", p.locale)
            put("proxyHost", p.proxyHost)
            put("proxyPort", p.proxyPort)
            put("proxyType", p.proxyType)
        }.toString()
    }

    private fun restoreClonesFromProfiles() {
        try {
            val profiles = profileManager.getAllProfiles()
            for (profile in profiles) {
                val cloneId = if (profile.cloneId.startsWith("ps_")) {
                    profile.cloneId
                } else {
                    if (profile.cloneId.contains("_user")) {
                        profile.cloneId.replace("_user", "_clone_")
                    } else {
                        "${profile.packageName}_clone_${profile.userId}"
                    }
                }

                val deviceProfile = DeviceProfile(
                    id = cloneId,
                    name = profile.profilePreset ?: "Unknown Preset",
                    model = profile.deviceModel,
                    brand = profile.brand,
                    manufacturer = profile.manufacturer,
                    fingerprint = profile.buildFingerprint,
                    screenDensity = profile.screenDensity.toLong(),
                    screenWidth = profile.screenWidth.toLong(),
                    screenHeight = profile.screenHeight.toLong(),
                    sdkVersion = profile.sdkVersion.toLong(),
                    releaseVersion = profile.releaseVersion,
                    androidId = profile.androidId,
                    imei = profile.imei,
                    macAddress = profile.macAddress,
                    bluetoothMac = profile.bluetoothMac,
                    gsfId = profile.gsfId,
                    advertisingId = profile.advertisingId,
                    serialNumber = profile.serial,
                    timezone = profile.timezone,
                    locale = profile.locale,
                    proxyHost = profile.proxyHost,
                    proxyPort = profile.proxyPort?.toLong(),
                    proxyType = "SOCKS5"
                )
                val profileJson = serializeDeviceProfileToJson(deviceProfile)

                if (profile.cloneId.startsWith("ps_")) {
                    // Restore Play Store Clone
                    val preset = profile.profilePreset ?: "Google Pixel 8 Pro"
                    val gmsConfig = GmsProxyConfig(
                        cloneId = cloneId,
                        gsfId = profile.gsfId,
                        advertisingId = profile.advertisingId,
                        useRealGms = true,
                        useMicroG = false
                    )
                    val psClone = PlayStoreCloneManager.PlayStoreClone(
                        cloneId = cloneId,
                        displayName = "Play Store",
                        devicePreset = preset,
                        gmsConfig = gmsConfig,
                        accountEmail = null,
                        setupComplete = true,
                        createdAt = profile.createdAt
                    )
                    playStoreManager.restoreClone(psClone)

                    val cloneInfo = CloneInfo(
                        id = cloneId,
                        packageName = "com.android.vending",
                        appName = "Google Play Store",
                        userId = 0L,
                        status = "ready",
                        createdAtMs = profile.createdAt,
                        profileJson = profileJson,
                        appIconPath = getIconPathForApp("com.android.vending")
                    )
                    cloneStore[cloneId] = cloneInfo
                } else {
                    // Restore standard clone

                    // MUST SYNC WITH VIRTUAL CORE HERE
                    val success = VirtualCore.get().installPackageAsUser(profile.packageName, profile.userId)
                    android.util.Log.i("FlutterBridgePlugin", "Restoring ${profile.packageName} into VirtualCore: $success")

                    val cloneInfo = CloneInfo(
                        id = cloneId,
                        packageName = profile.packageName,
                        appName = getAppLabel(profile.packageName),
                        userId = profile.userId.toLong(),
                        status = "ready",
                        createdAtMs = profile.createdAt,
                        profileJson = profileJson,
                        appIconPath = getIconPathForApp(profile.packageName)
                    )
                    cloneStore[cloneId] = cloneInfo
                }
            }
            android.util.Log.i("FlutterBridgePlugin", "Restored ${profiles.size} clones from database profiles.")
        } catch (e: Exception) {
            android.util.Log.e("FlutterBridgePlugin", "Failed to restore clones from profiles", e)
        }
    }
}

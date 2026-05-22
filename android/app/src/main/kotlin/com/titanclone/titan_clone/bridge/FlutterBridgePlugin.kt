package com.titanclone.titan_clone.bridge

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.titanclone.titan_clone.profile.VirtualProfileManager
import com.titanclone.titan_clone.profile.ProfileGenerator
import com.titanclone.titan_clone.profile.db.ProfileDatabase

class FlutterBridgePlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var methodChannel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private lateinit var context: Context
    private lateinit var profileManager: VirtualProfileManager
    private lateinit var profileGenerator: ProfileGenerator
    private lateinit var profileDb: ProfileDatabase

    private var eventSink: EventChannel.EventSink? = null
    private var engineInitialized = false

    companion object {
        const val METHOD_CHANNEL = "com.titanclone/bridge"
        const val EVENT_CHANNEL = "com.titanclone/events"
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        context = binding.applicationContext
        profileDb = ProfileDatabase.getInstance(context)
        profileManager = VirtualProfileManager(context)
        profileGenerator = ProfileGenerator(profileDb.dao)

        methodChannel = MethodChannel(binding.binaryMessenger, METHOD_CHANNEL)
        methodChannel.setMethodCallHandler(this)

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
        methodChannel.setMethodCallHandler(null)
        eventSink = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "initializeEngine" -> initializeEngine(result)
            "getInstalledApps" -> getInstalledApps(result)
            "createClone" -> {
                val packageName = call.argument<String>("packageName") ?: ""
                val userId = call.argument<Int>("userId") ?: 0
                createClone(packageName, userId, result)
            }
            "launchClone" -> {
                val cloneId = call.argument<String>("cloneId") ?: ""
                launchClone(cloneId, result)
            }
            "stopClone" -> {
                val cloneId = call.argument<String>("cloneId") ?: ""
                stopClone(cloneId, result)
            }
            "deleteClone" -> {
                val cloneId = call.argument<String>("cloneId") ?: ""
                deleteClone(cloneId, result)
            }
            "getClones" -> getClones(result)
            "getCloneStatus" -> {
                val cloneId = call.argument<String>("cloneId") ?: ""
                getCloneStatus(cloneId, result)
            }
            "getCloneProfile" -> {
                val cloneId = call.argument<String>("cloneId") ?: ""
                getCloneProfile(cloneId, result)
            }
            "updateProfile" -> {
                val cloneId = call.argument<String>("cloneId") ?: ""
                val profileData = call.argument<Map<String, Any>>("profile")
                updateProfile(cloneId, profileData, result)
            }
            "getDevicePresets" -> getDevicePresets(result)
            "setCloneProxy" -> {
                val cloneId = call.argument<String>("cloneId") ?: ""
                val host = call.argument<String>("host") ?: ""
                val port = call.argument<Int>("port") ?: 0
                val user = call.argument<String>("user")
                val pass = call.argument<String>("pass")
                setCloneProxy(cloneId, host, port, user, pass, result)
            }
            "getSystemProperties" -> {
                val cloneId = call.argument<String>("cloneId") ?: ""
                getSystemProperties(cloneId, result)
            }
            "validateProfile" -> {
                val cloneId = call.argument<String>("cloneId") ?: ""
                validateProfile(cloneId, result)
            }
            else -> result.notImplemented()
        }
    }

    private fun initializeEngine(result: Result) {
        try {
            // TODO: Initialize BlackBox/VirtualApp engine
            // VirtualCore.get().doCreate()
            engineInitialized = true
            sendEvent(mapOf("eventType" to "engineInitialized", "cloneId" to "", "data" to mapOf("success" to true)))
            result.success(true)
        } catch (e: Exception) {
            result.error("ENGINE_INIT_FAILED", e.message, null)
        }
    }

    private fun getInstalledApps(result: Result) {
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
                    mapOf(
                        "packageName" to appInfo.packageName,
                        "appName" to (pm.getApplicationLabel(appInfo)?.toString() ?: appInfo.packageName),
                        "versionName" to (packageInfo?.versionName ?: ""),
                        "versionCode" to (packageInfo?.let {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                it.longVersionCode.toInt()
                            } else {
                                @Suppress("DEPRECATION")
                                it.versionCode
                            }
                        } ?: 0),
                        "isSystemApp" to false
                    )
                }
            result.success(apps)
        } catch (e: Exception) {
            result.error("GET_APPS_FAILED", e.message, null)
        }
    }

    private fun createClone(packageName: String, userId: Int, result: Result) {
        try {
            // TODO: Integrate with BlackBox engine
            // val success = VirtualCore.get().installPackageAsUser(packageName, userId)
            val profile = profileManager.getOrCreateProfile(packageName, userId)

            val cloneInfo = mapOf(
                "id" to profile.cloneId,
                "packageName" to packageName,
                "appName" to getAppLabel(packageName),
                "userId" to userId,
                "status" to "installing",
                "profilePreset" to (profile.profilePreset ?: "random"),
                "deviceModel" to profile.deviceModel,
                "createdAt" to System.currentTimeMillis().toString()
            )

            sendEvent(mapOf(
                "eventType" to "installing",
                "cloneId" to cloneInfo["id"],
                "message" to "Installing clone..."
            ))

            result.success(cloneInfo)
        } catch (e: Exception) {
            result.error("CREATE_CLONE_FAILED", e.message, null)
        }
    }

    private fun launchClone(cloneId: String, result: Result) {
        try {
            // TODO: VirtualCore.get().launchApp(packageName, userId)
            sendEvent(mapOf(
                "eventType" to "running",
                "cloneId" to cloneId,
                "message" to "Clone launched"
            ))
            result.success(true)
        } catch (e: Exception) {
            result.error("LAUNCH_FAILED", e.message, null)
        }
    }

    private fun stopClone(cloneId: String, result: Result) {
        try {
            // TODO: VirtualCore.get().killApp(packageName, userId)
            sendEvent(mapOf(
                "eventType" to "stopped",
                "cloneId" to cloneId,
                "message" to "Clone stopped"
            ))
            result.success(true)
        } catch (e: Exception) {
            result.error("STOP_FAILED", e.message, null)
        }
    }

    private fun deleteClone(cloneId: String, result: Result) {
        try {
            // TODO: VirtualCore.get().uninstallPackageAsUser(packageName, userId)
            result.success(true)
        } catch (e: Exception) {
            result.error("DELETE_FAILED", e.message, null)
        }
    }

    private fun getClones(result: Result) {
        try {
            // TODO: Query engine for installed clones
            result.success(emptyList<Map<String, Any>>())
        } catch (e: Exception) {
            result.error("GET_CLONES_FAILED", e.message, null)
        }
    }

    private fun getCloneStatus(cloneId: String, result: Result) {
        try {
            // TODO: Query engine for clone status
            result.success("stopped")
        } catch (e: Exception) {
            result.error("GET_STATUS_FAILED", e.message, null)
        }
    }

    private fun getCloneProfile(cloneId: String, result: Result) {
        try {
            val profile = profileManager.getProfile(cloneId)
            if (profile != null) {
                result.success(mapOf(
                    "cloneId" to profile.cloneId,
                    "packageName" to profile.packageName,
                    "userId" to profile.userId,
                    "androidId" to profile.androidId,
                    "deviceModel" to profile.deviceModel,
                    "manufacturer" to profile.manufacturer,
                    "brand" to profile.brand,
                    "imei" to profile.imei,
                    "macAddress" to profile.macAddress,
                    "bluetoothMac" to profile.bluetoothMac,
                    "phoneNumber" to profile.phoneNumber,
                    "carrierName" to profile.carrierName,
                    "locale" to profile.locale,
                    "timezone" to profile.timezone,
                    "profilePreset" to profile.profilePreset,
                    "buildFingerprint" to profile.buildFingerprint,
                    "proxyHost" to profile.proxyHost,
                    "proxyPort" to profile.proxyPort
                ))
            } else {
                result.success(null)
            }
        } catch (e: Exception) {
            result.error("GET_PROFILE_FAILED", e.message, null)
        }
    }

    private fun updateProfile(cloneId: String, profileData: Map<String, Any>?, result: Result) {
        try {
            val existing = profileManager.getProfile(cloneId)
            if (existing != null && profileData != null) {
                val updated = existing.copy(
                    locale = profileData["locale"] as? String ?: existing.locale,
                    timezone = profileData["timezone"] as? String ?: existing.timezone
                )
                profileManager.updateProfile(updated)
            }
            result.success(true)
        } catch (e: Exception) {
            result.error("UPDATE_PROFILE_FAILED", e.message, null)
        }
    }

    private fun getDevicePresets(result: Result) {
        try {
            result.success(mapOf(
                "count" to profileGenerator.getPresetCount(),
                "presets" to listOf(
                    "Google Pixel 8 Pro", "Samsung Galaxy S24 Ultra",
                    "OnePlus 12", "Xiaomi 14 Pro", "Google Pixel 7",
                    "Samsung Galaxy A54", "Motorola Edge 40 Pro",
                    "Sony Xperia 1 V", "Nothing Phone (2)",
                    "OPPO Find X7 Ultra", "Realme GT 5 Pro", "Vivo X100 Pro"
                )
            ))
        } catch (e: Exception) {
            result.error("GET_PRESETS_FAILED", e.message, null)
        }
    }

    private fun setCloneProxy(
        cloneId: String, host: String, port: Int,
        user: String?, pass: String?, result: Result
    ) {
        try {
            profileManager.setProxyConfig(cloneId, host, port, user, pass)
            result.success(true)
        } catch (e: Exception) {
            result.error("SET_PROXY_FAILED", e.message, null)
        }
    }

    private fun getSystemProperties(cloneId: String, result: Result) {
        try {
            val props = profileManager.getSystemProperties(cloneId)
            result.success(props)
        } catch (e: Exception) {
            result.error("GET_PROPS_FAILED", e.message, null)
        }
    }

    private fun validateProfile(cloneId: String, result: Result) {
        try {
            val profile = profileManager.getProfile(cloneId)
            if (profile != null) {
                val errors = profileGenerator.validateProfile(profile)
                result.success(mapOf(
                    "valid" to errors.isEmpty(),
                    "errors" to errors
                ))
            } else {
                result.error("PROFILE_NOT_FOUND", "No profile for $cloneId", null)
            }
        } catch (e: Exception) {
            result.error("VALIDATE_FAILED", e.message, null)
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
        eventSink?.success(event)
    }
}

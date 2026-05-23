import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
  dartOut: 'lib/services/generated/clone_api.g.dart',
  kotlinOut:
      'android/app/src/main/kotlin/com/titanclone/titan_clone/bridge/CloneApi.g.kt',
  kotlinOptions: KotlinOptions(package: 'com.titanclone.titan_clone.bridge'),
))

/// Represents an installed app on the device.
class InstalledAppData {
  final String packageName;
  final String appName;
  final String? iconPath;
  final String? versionName;
  final int? versionCode;
  final bool isSystemApp;
  final int? installedSizeBytes;
  final bool isSplitApk;
  final String? category;

  InstalledAppData({
    required this.packageName,
    required this.appName,
    this.iconPath,
    this.versionName,
    this.versionCode,
    this.isSystemApp = false,
    this.installedSizeBytes,
    this.isSplitApk = false,
    this.category,
  });
}

/// Represents a clone instance.
class CloneInfoData {
  final String id;
  final String packageName;
  final String appName;
  final int userId;
  final String status;
  final int createdAtMs;
  final String? profileJson;
  final String? appIconPath;
  final int? memoryUsageMb;
  final int? lastLaunchedMs;
  final int? storageSizeBytes;

  CloneInfoData({
    required this.id,
    required this.packageName,
    required this.appName,
    required this.userId,
    required this.status,
    required this.createdAtMs,
    this.profileJson,
    this.appIconPath,
    this.memoryUsageMb,
    this.lastLaunchedMs,
    this.storageSizeBytes,
  });
}

/// Represents a virtual device profile.
class DeviceProfileData {
  final String id;
  final String name;
  final String model;
  final String brand;
  final String manufacturer;
  final String fingerprint;
  final int screenDensity;
  final int screenWidth;
  final int screenHeight;
  final int sdkVersion;
  final String releaseVersion;
  final String androidId;
  final String imei;
  final String macAddress;
  final String bluetoothMac;
  final String gsfId;
  final String advertisingId;
  final String? serialNumber;
  final String? timezone;
  final String? locale;
  final String? proxyHost;
  final int? proxyPort;
  final String? proxyType;

  DeviceProfileData({
    required this.id,
    required this.name,
    required this.model,
    required this.brand,
    required this.manufacturer,
    required this.fingerprint,
    required this.screenDensity,
    required this.screenWidth,
    required this.screenHeight,
    required this.sdkVersion,
    required this.releaseVersion,
    required this.androidId,
    required this.imei,
    required this.macAddress,
    required this.bluetoothMac,
    required this.gsfId,
    required this.advertisingId,
    this.serialNumber,
    this.timezone,
    this.locale,
    this.proxyHost,
    this.proxyPort,
    this.proxyType,
  });
}

/// Storage information for a clone.
class StorageInfoData {
  final String cloneId;
  final int totalSizeBytes;
  final int dataSizeBytes;
  final int cacheSizeBytes;

  StorageInfoData({
    required this.cloneId,
    required this.totalSizeBytes,
    required this.dataSizeBytes,
    required this.cacheSizeBytes,
  });
}

/// Engine status information.
class EngineStatusData {
  final bool initialized;
  final int runningCloneCount;
  final int totalCloneCount;
  final int memoryUsageMb;

  EngineStatusData({
    required this.initialized,
    required this.runningCloneCount,
    required this.totalCloneCount,
    required this.memoryUsageMb,
  });
}

/// GMS availability state.
class GmsStateData {
  final bool gmsAvailable;
  final String? gmsVersion;
  final String? playStoreVersion;
  final bool gsfAvailable;
  final int maxPlayStoreClones;
  final int activePlayStoreClones;

  GmsStateData({
    required this.gmsAvailable,
    this.gmsVersion,
    this.playStoreVersion,
    required this.gsfAvailable,
    required this.maxPlayStoreClones,
    required this.activePlayStoreClones,
  });
}

/// Device compatibility report.
class CompatReportData {
  final int apiLevel;
  final String androidVersion;
  final bool isSupported;
  final List<String> issues;
  final List<String> missingPermissions;
  final List<String> recommendations;

  CompatReportData({
    required this.apiLevel,
    required this.androidVersion,
    required this.isSupported,
    required this.issues,
    required this.missingPermissions,
    required this.recommendations,
  });
}

/// Battery optimization info.
class BatteryInfoData {
  final bool isIgnoringOptimization;
  final String oemBrand;
  final String? oemIssue;

  BatteryInfoData({
    required this.isIgnoringOptimization,
    required this.oemBrand,
    this.oemIssue,
  });
}

/// Memory snapshot from the optimization layer.
class MemorySnapshotData {
  final int totalDeviceRamMb;
  final int availableRamMb;
  final int engineNativeHeapMb;
  final int engineJavaHeapMb;
  final int cloneProcessCount;
  final int estimatedCloneOverheadMb;
  final bool isLowMemory;
  final int recommendedMaxClones;

  MemorySnapshotData({
    required this.totalDeviceRamMb,
    required this.availableRamMb,
    required this.engineNativeHeapMb,
    required this.engineJavaHeapMb,
    required this.cloneProcessCount,
    required this.estimatedCloneOverheadMb,
    required this.isLowMemory,
    required this.recommendedMaxClones,
  });
}

/// Security check result.
class SecurityStatusData {
  final bool signatureValid;
  final bool debuggerAttached;
  final bool deviceRooted;
  final bool isEmulator;
  final bool nativeLibsIntact;
  final bool overallSecure;

  SecurityStatusData({
    required this.signatureValid,
    required this.debuggerAttached,
    required this.deviceRooted,
    required this.isEmulator,
    required this.nativeLibsIntact,
    required this.overallSecure,
  });
}

/// Performance metrics.
class PerformanceMetricsData {
  final int avgColdLaunchMs;
  final int avgWarmLaunchMs;
  final int avgProfileLoadMs;
  final int totalLaunches;
  final int batteryLevel;
  final bool isCharging;
  final String powerRecommendation;

  PerformanceMetricsData({
    required this.avgColdLaunchMs,
    required this.avgWarmLaunchMs,
    required this.avgProfileLoadMs,
    required this.totalLaunches,
    required this.batteryLevel,
    required this.isCharging,
    required this.powerRecommendation,
  });
}

/// Host API — Dart calls into native Android code.
@HostApi()
abstract class CloneEngineApi {
  /// Initialize the virtualization engine.
  @async
  bool initializeEngine();

  /// Check if the engine is ready.
  bool isEngineReady();

  /// Get engine status information.
  EngineStatusData getEngineStatus();

  /// Get all installed apps available for cloning.
  @async
  List<InstalledAppData> getInstalledApps();

  /// Create a new clone of an app.
  @async
  CloneInfoData createClone(String packageName, int userId, String? profilePreset);

  /// Launch a clone.
  @async
  bool launchClone(String cloneId);

  /// Stop a running clone.
  @async
  bool stopClone(String cloneId);

  /// Delete a clone and its data.
  @async
  bool deleteClone(String cloneId);

  /// Get all created clones.
  @async
  List<CloneInfoData> getClones();

  /// Get the status of a specific clone.
  @async
  String getCloneStatus(String cloneId);

  /// Get virtual profile for a clone.
  @async
  DeviceProfileData? getCloneProfile(String cloneId);

  /// Update virtual profile for a clone.
  @async
  bool updateProfile(String cloneId, DeviceProfileData profile);

  /// Reset virtual profile to new random values.
  @async
  DeviceProfileData resetCloneProfile(String cloneId);

  /// Get storage info for a clone.
  @async
  StorageInfoData getCloneStorageInfo(String cloneId);

  /// Clear a clone's cache.
  @async
  bool clearCloneCache(String cloneId);

  /// Clear a clone's data (factory reset the clone).
  @async
  bool clearCloneData(String cloneId);

  /// Set the maximum number of concurrent running clones.
  bool setMaxConcurrentClones(int maxClones);

  /// Set memory limit per clone in MB.
  bool setMemoryLimitPerClone(int limitMb);

  /// Get GMS availability state.
  GmsStateData getGmsState();

  /// Create a Play Store clone with device preset.
  @async
  String? createPlayStoreClone(String? devicePreset);

  /// Delete a Play Store clone.
  @async
  bool deletePlayStoreClone(String cloneId);

  /// Check device compatibility.
  CompatReportData checkCompatibility();

  /// Get battery optimization info.
  BatteryInfoData getBatteryOptimizationInfo();

  /// Start foreground service.
  void startForegroundService(int runningCount);

  /// Stop foreground service.
  void stopForegroundService();

  /// Get memory snapshot.
  MemorySnapshotData getMemorySnapshot();

  /// Run security check.
  SecurityStatusData performSecurityCheck();

  /// Get performance metrics.
  PerformanceMetricsData getPerformanceMetrics();

  /// Check if it's safe to launch another clone.
  bool canLaunchClone();

  /// Request garbage collection.
  void requestGc();

  /// Encrypt clone data.
  @async
  bool encryptCloneData(String cloneId);

  /// Securely delete clone data.
  @async
  bool secureDeleteCloneData(String cloneId);
}

/// Flutter API — native Android calls into Dart.
@FlutterApi()
abstract class CloneEventApi {
  /// Clone status changed.
  void onCloneStatusChanged(String cloneId, String newStatus);

  /// Clone installation progress (0-100).
  void onInstallProgress(String cloneId, int progressPercent);

  /// Clone crashed or encountered an error.
  void onCloneError(String cloneId, String errorMessage);

  /// Engine initialization complete.
  void onEngineInitialized(bool success);

  /// Memory warning when usage is high.
  void onMemoryWarning(int totalUsedMb, int thresholdMb);

  /// GMS checkin completed for a clone.
  void onGmsCheckinComplete(String cloneId, bool success);

  /// Foreground service state changed.
  void onForegroundServiceStateChanged(bool running, int cloneCount);
}

import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
  dartOut: 'lib/services/generated/clone_api.g.dart',
  kotlinOut:
      'android/app/src/main/kotlin/com/titanclone/titan_clone/bridge/CloneApi.g.kt',
  kotlinOptions: KotlinOptions(package: 'com.titanclone.titan_clone.bridge'),
))

/// Represents an installed app on the device.
class PigeonInstalledApp {
  final String packageName;
  final String appName;
  final String? iconPath;
  final String? versionName;
  final int? versionCode;
  final bool isSystemApp;
  final int? installedSizeBytes;
  final bool isSplitApk;
  final String? category;

  PigeonInstalledApp({
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
class PigeonCloneInfo {
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

  PigeonCloneInfo({
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
class PigeonDeviceProfile {
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

  PigeonDeviceProfile({
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
class PigeonStorageInfo {
  final String cloneId;
  final int totalSizeBytes;
  final int dataSizeBytes;
  final int cacheSizeBytes;

  PigeonStorageInfo({
    required this.cloneId,
    required this.totalSizeBytes,
    required this.dataSizeBytes,
    required this.cacheSizeBytes,
  });
}

/// Engine status information.
class PigeonEngineStatus {
  final bool initialized;
  final int runningCloneCount;
  final int totalCloneCount;
  final int memoryUsageMb;

  PigeonEngineStatus({
    required this.initialized,
    required this.runningCloneCount,
    required this.totalCloneCount,
    required this.memoryUsageMb,
  });
}

/// GMS availability state.
class PigeonGmsState {
  final bool gmsAvailable;
  final String? gmsVersion;
  final String? playStoreVersion;
  final bool gsfAvailable;
  final int maxPlayStoreClones;
  final int activePlayStoreClones;

  PigeonGmsState({
    required this.gmsAvailable,
    this.gmsVersion,
    this.playStoreVersion,
    required this.gsfAvailable,
    required this.maxPlayStoreClones,
    required this.activePlayStoreClones,
  });
}

/// Device compatibility report.
class PigeonCompatReport {
  final int apiLevel;
  final String androidVersion;
  final bool isSupported;
  final List<String> issues;
  final List<String> missingPermissions;
  final List<String> recommendations;

  PigeonCompatReport({
    required this.apiLevel,
    required this.androidVersion,
    required this.isSupported,
    required this.issues,
    required this.missingPermissions,
    required this.recommendations,
  });
}

/// Battery optimization info.
class PigeonBatteryInfo {
  final bool isIgnoringOptimization;
  final String oemBrand;
  final String? oemIssue;

  PigeonBatteryInfo({
    required this.isIgnoringOptimization,
    required this.oemBrand,
    this.oemIssue,
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
  PigeonEngineStatus getEngineStatus();

  /// Get all installed apps available for cloning.
  @async
  List<PigeonInstalledApp> getInstalledApps();

  /// Create a new clone of an app.
  @async
  PigeonCloneInfo createClone(String packageName, int userId, String? profilePreset);

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
  List<PigeonCloneInfo> getClones();

  /// Get the status of a specific clone.
  @async
  String getCloneStatus(String cloneId);

  /// Get virtual profile for a clone.
  @async
  PigeonDeviceProfile? getCloneProfile(String cloneId);

  /// Update virtual profile for a clone.
  @async
  bool updateProfile(String cloneId, PigeonDeviceProfile profile);

  /// Reset virtual profile to new random values.
  @async
  PigeonDeviceProfile resetCloneProfile(String cloneId);

  /// Get storage info for a clone.
  @async
  PigeonStorageInfo getCloneStorageInfo(String cloneId);

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
  PigeonGmsState getGmsState();

  /// Create a Play Store clone with device preset.
  @async
  String? createPlayStoreClone(String? devicePreset);

  /// Delete a Play Store clone.
  @async
  bool deletePlayStoreClone(String cloneId);

  /// Check device compatibility.
  PigeonCompatReport checkCompatibility();

  /// Get battery optimization info.
  PigeonBatteryInfo getBatteryOptimizationInfo();

  /// Start foreground service.
  void startForegroundService(int runningCount);

  /// Stop foreground service.
  void stopForegroundService();
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

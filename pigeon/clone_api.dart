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

  PigeonInstalledApp({
    required this.packageName,
    required this.appName,
    this.iconPath,
    this.versionName,
    this.versionCode,
    this.isSystemApp = false,
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
    this.proxyHost,
    this.proxyPort,
    this.proxyType,
  });
}

/// Host API — Dart calls into native Android code.
@HostApi()
abstract class CloneEngineApi {
  @async
  bool initializeEngine();

  @async
  List<PigeonInstalledApp> getInstalledApps();

  @async
  PigeonCloneInfo createClone(String packageName, int userId);

  @async
  bool launchClone(String cloneId);

  @async
  bool stopClone(String cloneId);

  @async
  bool deleteClone(String cloneId);

  @async
  List<PigeonCloneInfo> getClones();

  @async
  String getCloneStatus(String cloneId);

  @async
  PigeonDeviceProfile? getCloneProfile(String cloneId);

  @async
  bool updateProfile(String cloneId, PigeonDeviceProfile profile);
}

/// Flutter API — native Android calls into Dart.
@FlutterApi()
abstract class CloneEventApi {
  void onCloneStatusChanged(String cloneId, String newStatus);

  void onCloneError(String cloneId, String errorMessage);

  void onEngineInitialized(bool success);
}

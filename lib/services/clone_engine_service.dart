import 'dart:async';

import 'package:flutter/services.dart';
import 'dart:convert';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/utils/logger.dart';
import '../models/clone_info.dart';
import '../models/clone_status.dart';
import '../models/device_profile.dart';
import 'generated/clone_api.g.dart' as pigeon;

final cloneEngineServiceProvider = Provider<CloneEngineService>((ref) {
  return CloneEngineService();
});

class CloneEngineService {
  final _api = pigeon.CloneEngineApi();

  bool _engineReady = false;
  bool get isEngineReady => _engineReady;

  Future<bool> initializeEngine() async {
    try {
      final result = await _api.initializeEngine();
      _engineReady = result;
      return _engineReady;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to initialize engine', error: e);
      return false;
    }
  }

  Future<EngineStatus> getEngineStatus() async {
    try {
      final result = await _api.getEngineStatus();
      return EngineStatus(
        initialized: result.initialized,
        runningCloneCount: result.runningCloneCount,
        totalCloneCount: result.totalCloneCount,
        memoryUsageMb: result.memoryUsageMb,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get engine status', error: e);
      return EngineStatus(
        initialized: _engineReady,
        runningCloneCount: 0,
        totalCloneCount: 0,
        memoryUsageMb: 0,
      );
    }
  }

  Future<List<InstalledApp>> getInstalledApps() async {
    try {
      final result = await _api.getInstalledApps();
      return result
          .map((e) => InstalledApp(
                packageName: e.packageName,
                appName: e.appName,
                iconPath: e.iconPath,
                versionName: e.versionName,
                versionCode: e.versionCode,
                isSystemApp: e.isSystemApp,
                installedSizeBytes: e.installedSizeBytes,
                isSplitApk: e.isSplitApk,
                category: e.category,
              ))
          .toList();
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get installed apps', error: e);
      return [];
    }
  }

  Future<CloneInfo?> createClone({
    required String packageName,
    required int userId,
    String? profilePreset,
  }) async {
    try {
      final result = await _api.createClone(packageName, userId, profilePreset);
      return _mapCloneInfo(result);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to create clone', error: e);
      return null;
    }
  }

  Future<bool> launchClone(String cloneId) async {
    try {
      return await _api.launchClone(cloneId);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to launch clone', error: e);
      return false;
    }
  }

  Future<bool> stopClone(String cloneId) async {
    try {
      return await _api.stopClone(cloneId);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to stop clone', error: e);
      return false;
    }
  }

  Future<bool> deleteClone(String cloneId) async {
    try {
      return await _api.deleteClone(cloneId);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to delete clone', error: e);
      return false;
    }
  }

  Future<List<CloneInfo>> getClones() async {
    try {
      final result = await _api.getClones();
      return result.map(_mapCloneInfo).toList();
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get clones', error: e);
      return [];
    }
  }

  Future<CloneStatus?> getCloneStatus(String cloneId) async {
    try {
      final result = await _api.getCloneStatus(cloneId);
      return CloneStatus.values.firstWhere(
        (s) => s.name == result,
        orElse: () => CloneStatus.error,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get clone status', error: e);
      return null;
    }
  }

  Future<DeviceProfile?> getCloneProfile(String cloneId) async {
    try {
      final result = await _api.getCloneProfile(cloneId);
      if (result == null) return null;
      return _mapDeviceProfile(result);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get clone profile', error: e);
      return null;
    }
  }

  Future<bool> updateCloneProfile(
    String cloneId,
    DeviceProfile profile,
  ) async {
    try {
      return await _api.updateProfile(cloneId, _toPigeonDeviceProfile(profile));
    } on PlatformException catch (e) {
      AppLogger.error('Failed to update clone profile', error: e);
      return false;
    }
  }

  Future<DeviceProfile?> resetCloneProfile(String cloneId) async {
    try {
      final result = await _api.resetCloneProfile(cloneId);
      return _mapDeviceProfile(result);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to reset clone profile', error: e);
      return null;
    }
  }

  Future<StorageInfo> getCloneStorageInfo(String cloneId) async {
    try {
      final result = await _api.getCloneStorageInfo(cloneId);
      return StorageInfo(
        cloneId: result.cloneId,
        totalSizeBytes: result.totalSizeBytes,
        dataSizeBytes: result.dataSizeBytes,
        cacheSizeBytes: result.cacheSizeBytes,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get storage info', error: e);
      return StorageInfo(
        cloneId: cloneId,
        totalSizeBytes: 0,
        dataSizeBytes: 0,
        cacheSizeBytes: 0,
      );
    }
  }

  Future<bool> clearCloneCache(String cloneId) async {
    try {
      return await _api.clearCloneCache(cloneId);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to clear cache', error: e);
      return false;
    }
  }

  Future<bool> clearCloneData(String cloneId) async {
    try {
      return await _api.clearCloneData(cloneId);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to clear data', error: e);
      return false;
    }
  }

  Future<bool> setMaxConcurrentClones(int maxClones) async {
    try {
      return await _api.setMaxConcurrentClones(maxClones);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to set max concurrent clones', error: e);
      return false;
    }
  }

  Future<bool> setMemoryLimitPerClone(int limitMb) async {
    try {
      return await _api.setMemoryLimitPerClone(limitMb);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to set memory limit', error: e);
      return false;
    }
  }

  Future<GmsState> getGmsState() async {
    try {
      final result = await _api.getGmsState();
      return GmsState(
        gmsAvailable: result.gmsAvailable,
        gmsVersion: result.gmsVersion,
        playStoreVersion: result.playStoreVersion,
        gsfAvailable: result.gsfAvailable,
        maxPlayStoreClones: result.maxPlayStoreClones,
        activePlayStoreClones: result.activePlayStoreClones,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get GMS state', error: e);
      return const GmsState(
        gmsAvailable: false,
        gmsVersion: null,
        playStoreVersion: null,
        gsfAvailable: false,
        maxPlayStoreClones: 12,
        activePlayStoreClones: 0,
      );
    }
  }

  Future<String?> createPlayStoreClone({String? devicePreset}) async {
    try {
      return await _api.createPlayStoreClone(devicePreset);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to create Play Store clone', error: e);
      return null;
    }
  }

  Future<bool> deletePlayStoreClone(String cloneId) async {
    try {
      return await _api.deletePlayStoreClone(cloneId);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to delete Play Store clone', error: e);
      return false;
    }
  }

  Future<CompatReport> checkCompatibility() async {
    try {
      final result = await _api.checkCompatibility();
      return CompatReport(
        apiLevel: result.apiLevel,
        androidVersion: result.androidVersion,
        isSupported: result.isSupported,
        issues: result.issues,
        missingPermissions: result.missingPermissions,
        recommendations: result.recommendations,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to check compatibility', error: e);
      return const CompatReport(
        apiLevel: 0,
        androidVersion: 'Unknown',
        isSupported: false,
        issues: [],
        missingPermissions: [],
        recommendations: [],
      );
    }
  }

  Future<BatteryInfo> getBatteryOptimizationInfo() async {
    try {
      final result = await _api.getBatteryOptimizationInfo();
      return BatteryInfo(
        isIgnoringOptimization: result.isIgnoringOptimization,
        oemBrand: result.oemBrand,
        oemIssue: result.oemIssue,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get battery info', error: e);
      return const BatteryInfo(
        isIgnoringOptimization: false,
        oemBrand: 'Unknown',
        oemIssue: null,
      );
    }
  }

  Future<void> startForegroundService(int runningCount) async {
    try {
      await _api.startForegroundService(runningCount);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to start foreground service', error: e);
    }
  }

  Future<void> stopForegroundService() async {
    try {
      await _api.stopForegroundService();
    } on PlatformException catch (e) {
      AppLogger.error('Failed to stop foreground service', error: e);
    }
  }

  Future<MemorySnapshot> getMemorySnapshot() async {
    try {
      final result = await _api.getMemorySnapshot();
      return MemorySnapshot(
        totalDeviceRamMb: result.totalDeviceRamMb,
        availableRamMb: result.availableRamMb,
        engineNativeHeapMb: result.engineNativeHeapMb,
        engineJavaHeapMb: result.engineJavaHeapMb,
        cloneProcessCount: result.cloneProcessCount,
        estimatedCloneOverheadMb: result.estimatedCloneOverheadMb,
        isLowMemory: result.isLowMemory,
        recommendedMaxClones: result.recommendedMaxClones,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get memory snapshot', error: e);
      return MemorySnapshot.empty();
    }
  }

  Future<SecurityStatus> performSecurityCheck() async {
    try {
      final result = await _api.performSecurityCheck();
      return SecurityStatus(
        signatureValid: result.signatureValid,
        debuggerAttached: result.debuggerAttached,
        deviceRooted: result.deviceRooted,
        isEmulator: result.isEmulator,
        nativeLibsIntact: result.nativeLibsIntact,
        overallSecure: result.overallSecure,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to perform security check', error: e);
      return SecurityStatus.unknown();
    }
  }

  Future<PerformanceMetrics> getPerformanceMetrics() async {
    try {
      final result = await _api.getPerformanceMetrics();
      return PerformanceMetrics(
        avgColdLaunchMs: result.avgColdLaunchMs,
        avgWarmLaunchMs: result.avgWarmLaunchMs,
        avgProfileLoadMs: result.avgProfileLoadMs,
        totalLaunches: result.totalLaunches,
        batteryLevel: result.batteryLevel,
        isCharging: result.isCharging,
        powerRecommendation: result.powerRecommendation,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get performance metrics', error: e);
      return PerformanceMetrics.empty();
    }
  }

  Future<bool> canLaunchClone() async {
    try {
      return await _api.canLaunchClone();
    } on PlatformException catch (e) {
      AppLogger.error('Failed to check launch capability', error: e);
      return true;
    }
  }

  Future<void> requestGc() async {
    try {
      await _api.requestGc();
    } on PlatformException catch (e) {
      AppLogger.error('Failed to request GC', error: e);
    }
  }

  Future<bool> encryptCloneData(String cloneId) async {
    try {
      return await _api.encryptCloneData(cloneId);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to encrypt clone data', error: e);
      return false;
    }
  }

  Future<bool> secureDeleteCloneData(String cloneId) async {
    try {
      return await _api.secureDeleteCloneData(cloneId);
    } on PlatformException catch (e) {
      AppLogger.error('Failed to secure delete clone data', error: e);
      return false;
    }
  }

  // Mapping Helpers
  CloneInfo _mapCloneInfo(pigeon.CloneInfo info) {
    DeviceProfile? decodedProfile;
    if (info.profileJson != null && info.profileJson!.isNotEmpty) {
      try {
        final decodedMap = jsonDecode(info.profileJson!) as Map<String, dynamic>;
        decodedProfile = DeviceProfile.fromJson(decodedMap);
      } catch (e) {
        AppLogger.error('Failed to decode clone profile JSON', error: e);
      }
    }

    return CloneInfo(
      id: info.id,
      packageName: info.packageName,
      appName: info.appName,
      userId: info.userId,
      status: CloneStatus.values.firstWhere(
        (s) => s.name == info.status,
        orElse: () => CloneStatus.error,
      ),
      createdAt: DateTime.fromMillisecondsSinceEpoch(info.createdAtMs),
      profile: decodedProfile,
      appIconPath: info.appIconPath,
      memoryUsageMb: info.memoryUsageMb,
      lastLaunched: info.lastLaunchedMs != null
          ? DateTime.fromMillisecondsSinceEpoch(info.lastLaunchedMs!)
          : null,
      storageSizeBytes: info.storageSizeBytes,
    );
  }

  DeviceProfile _mapDeviceProfile(pigeon.DeviceProfile profile) {
    return DeviceProfile(
      id: profile.id,
      name: profile.name,
      model: profile.model,
      brand: profile.brand,
      manufacturer: profile.manufacturer,
      fingerprint: profile.fingerprint,
      screenDensity: profile.screenDensity,
      screenWidth: profile.screenWidth,
      screenHeight: profile.screenHeight,
      abis: const ['arm64-v8a', 'armeabi-v7a'], // Default ABIs
      sdkVersion: profile.sdkVersion,
      releaseVersion: profile.releaseVersion,
      androidId: profile.androidId,
      imei: profile.imei,
      macAddress: profile.macAddress,
      bluetoothMac: profile.bluetoothMac,
      gsfId: profile.gsfId,
      advertisingId: profile.advertisingId,
      serialNumber: profile.serialNumber,
      timezone: profile.timezone,
      locale: profile.locale,
      proxyHost: profile.proxyHost,
      proxyPort: profile.proxyPort,
      proxyType: profile.proxyType,
    );
  }

  pigeon.DeviceProfile _toPigeonDeviceProfile(DeviceProfile p) {
    return pigeon.DeviceProfile(
      id: p.id,
      name: p.name,
      model: p.model,
      brand: p.brand,
      manufacturer: p.manufacturer,
      fingerprint: p.fingerprint,
      screenDensity: p.screenDensity,
      screenWidth: p.screenWidth,
      screenHeight: p.screenHeight,
      sdkVersion: p.sdkVersion,
      releaseVersion: p.releaseVersion,
      androidId: p.androidId,
      imei: p.imei,
      macAddress: p.macAddress,
      bluetoothMac: p.bluetoothMac,
      gsfId: p.gsfId,
      advertisingId: p.advertisingId,
      serialNumber: p.serialNumber,
      timezone: p.timezone,
      locale: p.locale,
      proxyHost: p.proxyHost,
      proxyPort: p.proxyPort,
      proxyType: p.proxyType,
    );
  }
}

class GmsState {
  final bool gmsAvailable;
  final String? gmsVersion;
  final String? playStoreVersion;
  final bool gsfAvailable;
  final int maxPlayStoreClones;
  final int activePlayStoreClones;

  const GmsState({
    required this.gmsAvailable,
    this.gmsVersion,
    this.playStoreVersion,
    required this.gsfAvailable,
    required this.maxPlayStoreClones,
    required this.activePlayStoreClones,
  });

  bool get canCreatePlayStoreClone =>
      gmsAvailable &&
      playStoreVersion != null &&
      activePlayStoreClones < maxPlayStoreClones;

  int get slotsRemaining => maxPlayStoreClones - activePlayStoreClones;
}

class CompatReport {
  final int apiLevel;
  final String androidVersion;
  final bool isSupported;
  final List<String> issues;
  final List<String> missingPermissions;
  final List<String> recommendations;

  const CompatReport({
    required this.apiLevel,
    required this.androidVersion,
    required this.isSupported,
    required this.issues,
    required this.missingPermissions,
    required this.recommendations,
  });
}

class BatteryInfo {
  final bool isIgnoringOptimization;
  final String oemBrand;
  final String? oemIssue;

  const BatteryInfo({
    required this.isIgnoringOptimization,
    required this.oemBrand,
    this.oemIssue,
  });
}

class EngineStatus {
  final bool initialized;
  final int runningCloneCount;
  final int totalCloneCount;
  final int memoryUsageMb;

  const EngineStatus({
    required this.initialized,
    required this.runningCloneCount,
    required this.totalCloneCount,
    required this.memoryUsageMb,
  });
}

class StorageInfo {
  final String cloneId;
  final int totalSizeBytes;
  final int dataSizeBytes;
  final int cacheSizeBytes;

  const StorageInfo({
    required this.cloneId,
    required this.totalSizeBytes,
    required this.dataSizeBytes,
    required this.cacheSizeBytes,
  });

  String get formattedTotal => _formatBytes(totalSizeBytes);
  String get formattedData => _formatBytes(dataSizeBytes);
  String get formattedCache => _formatBytes(cacheSizeBytes);

  static String _formatBytes(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    if (bytes < 1024 * 1024 * 1024) {
      return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
    }
    return '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(1)} GB';
  }
}

class MemorySnapshot {
  final int totalDeviceRamMb;
  final int availableRamMb;
  final int engineNativeHeapMb;
  final int engineJavaHeapMb;
  final int cloneProcessCount;
  final int estimatedCloneOverheadMb;
  final bool isLowMemory;
  final int recommendedMaxClones;

  const MemorySnapshot({
    required this.totalDeviceRamMb,
    required this.availableRamMb,
    required this.engineNativeHeapMb,
    required this.engineJavaHeapMb,
    required this.cloneProcessCount,
    required this.estimatedCloneOverheadMb,
    required this.isLowMemory,
    required this.recommendedMaxClones,
  });

  factory MemorySnapshot.empty() => const MemorySnapshot(
        totalDeviceRamMb: 0,
        availableRamMb: 0,
        engineNativeHeapMb: 0,
        engineJavaHeapMb: 0,
        cloneProcessCount: 0,
        estimatedCloneOverheadMb: 0,
        isLowMemory: false,
        recommendedMaxClones: 3,
      );

  double get usagePercent => totalDeviceRamMb > 0
      ? ((totalDeviceRamMb - availableRamMb) / totalDeviceRamMb * 100)
      : 0;
}

class SecurityStatus {
  final bool signatureValid;
  final bool debuggerAttached;
  final bool deviceRooted;
  final bool isEmulator;
  final bool nativeLibsIntact;
  final bool overallSecure;

  const SecurityStatus({
    required this.signatureValid,
    required this.debuggerAttached,
    required this.deviceRooted,
    required this.isEmulator,
    required this.nativeLibsIntact,
    required this.overallSecure,
  });

  factory SecurityStatus.unknown() => const SecurityStatus(
        signatureValid: false,
        debuggerAttached: false,
        deviceRooted: false,
        isEmulator: false,
        nativeLibsIntact: false,
        overallSecure: false,
      );

  int get issueCount {
    int count = 0;
    if (!signatureValid) count++;
    if (debuggerAttached) count++;
    if (deviceRooted) count++;
    if (!nativeLibsIntact) count++;
    return count;
  }
}

class PerformanceMetrics {
  final int avgColdLaunchMs;
  final int avgWarmLaunchMs;
  final int avgProfileLoadMs;
  final int totalLaunches;
  final int batteryLevel;
  final bool isCharging;
  final String powerRecommendation;

  const PerformanceMetrics({
    required this.avgColdLaunchMs,
    required this.avgWarmLaunchMs,
    required this.avgProfileLoadMs,
    required this.totalLaunches,
    required this.batteryLevel,
    required this.isCharging,
    required this.powerRecommendation,
  });

  factory PerformanceMetrics.empty() => const PerformanceMetrics(
        avgColdLaunchMs: 0,
        avgWarmLaunchMs: 0,
        avgProfileLoadMs: 0,
        totalLaunches: 0,
        batteryLevel: 0,
        isCharging: false,
        powerRecommendation: 'UNKNOWN',
      );
}

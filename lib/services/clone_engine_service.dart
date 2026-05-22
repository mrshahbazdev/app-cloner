import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/constants/app_constants.dart';
import '../core/utils/logger.dart';
import '../models/clone_info.dart';
import '../models/clone_status.dart';
import '../models/device_profile.dart';

final cloneEngineServiceProvider = Provider<CloneEngineService>((ref) {
  return CloneEngineService();
});

class CloneEngineService {
  static const _channel = MethodChannel(AppConstants.bridgeChannelName);

  bool _engineReady = false;
  bool get isEngineReady => _engineReady;

  Future<bool> initializeEngine() async {
    try {
      final result = await _channel.invokeMethod<bool>('initializeEngine');
      _engineReady = result ?? false;
      return _engineReady;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to initialize engine', error: e);
      return false;
    }
  }

  Future<EngineStatus> getEngineStatus() async {
    try {
      final result = await _channel.invokeMethod<Map>('getEngineStatus');
      if (result == null) {
        return EngineStatus(
          initialized: _engineReady,
          runningCloneCount: 0,
          totalCloneCount: 0,
          memoryUsageMb: 0,
        );
      }
      return EngineStatus(
        initialized: result['initialized'] as bool? ?? false,
        runningCloneCount: result['runningCloneCount'] as int? ?? 0,
        totalCloneCount: result['totalCloneCount'] as int? ?? 0,
        memoryUsageMb: result['memoryUsageMb'] as int? ?? 0,
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
      final result = await _channel.invokeMethod<List>('getInstalledApps');
      if (result == null) return [];
      return result
          .cast<Map>()
          .map((e) =>
              InstalledApp.fromJson(Map<String, dynamic>.from(e)))
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
      final result = await _channel.invokeMethod<Map>('createClone', {
        'packageName': packageName,
        'userId': userId,
        if (profilePreset != null) 'profilePreset': profilePreset,
      });
      if (result == null) return null;
      return CloneInfo.fromJson(Map<String, dynamic>.from(result));
    } on PlatformException catch (e) {
      AppLogger.error('Failed to create clone', error: e);
      return null;
    }
  }

  Future<bool> launchClone(String cloneId) async {
    try {
      final result = await _channel.invokeMethod<bool>('launchClone', {
        'cloneId': cloneId,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to launch clone', error: e);
      return false;
    }
  }

  Future<bool> stopClone(String cloneId) async {
    try {
      final result = await _channel.invokeMethod<bool>('stopClone', {
        'cloneId': cloneId,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to stop clone', error: e);
      return false;
    }
  }

  Future<bool> deleteClone(String cloneId) async {
    try {
      final result = await _channel.invokeMethod<bool>('deleteClone', {
        'cloneId': cloneId,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to delete clone', error: e);
      return false;
    }
  }

  Future<List<CloneInfo>> getClones() async {
    try {
      final result = await _channel.invokeMethod<List>('getClones');
      if (result == null) return [];
      return result
          .cast<Map>()
          .map((e) => CloneInfo.fromJson(Map<String, dynamic>.from(e)))
          .toList();
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get clones', error: e);
      return [];
    }
  }

  Future<CloneStatus?> getCloneStatus(String cloneId) async {
    try {
      final result = await _channel.invokeMethod<String>('getCloneStatus', {
        'cloneId': cloneId,
      });
      if (result == null) return null;
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
      final result = await _channel.invokeMethod<Map>('getCloneProfile', {
        'cloneId': cloneId,
      });
      if (result == null) return null;
      return DeviceProfile.fromJson(Map<String, dynamic>.from(result));
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
      final result = await _channel.invokeMethod<bool>('updateProfile', {
        'cloneId': cloneId,
        'profile': profile.toJson(),
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to update clone profile', error: e);
      return false;
    }
  }

  Future<DeviceProfile?> resetCloneProfile(String cloneId) async {
    try {
      final result =
          await _channel.invokeMethod<Map>('resetCloneProfile', {
        'cloneId': cloneId,
      });
      if (result == null) return null;
      return DeviceProfile.fromJson(Map<String, dynamic>.from(result));
    } on PlatformException catch (e) {
      AppLogger.error('Failed to reset clone profile', error: e);
      return null;
    }
  }

  Future<StorageInfo> getCloneStorageInfo(String cloneId) async {
    try {
      final result =
          await _channel.invokeMethod<Map>('getCloneStorageInfo', {
        'cloneId': cloneId,
      });
      if (result == null) {
        return StorageInfo(
            cloneId: cloneId,
            totalSizeBytes: 0,
            dataSizeBytes: 0,
            cacheSizeBytes: 0);
      }
      return StorageInfo(
        cloneId: result['cloneId'] as String? ?? cloneId,
        totalSizeBytes: result['totalSizeBytes'] as int? ?? 0,
        dataSizeBytes: result['dataSizeBytes'] as int? ?? 0,
        cacheSizeBytes: result['cacheSizeBytes'] as int? ?? 0,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get storage info', error: e);
      return StorageInfo(
          cloneId: cloneId,
          totalSizeBytes: 0,
          dataSizeBytes: 0,
          cacheSizeBytes: 0);
    }
  }

  Future<bool> clearCloneCache(String cloneId) async {
    try {
      final result =
          await _channel.invokeMethod<bool>('clearCloneCache', {
        'cloneId': cloneId,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to clear cache', error: e);
      return false;
    }
  }

  Future<bool> clearCloneData(String cloneId) async {
    try {
      final result =
          await _channel.invokeMethod<bool>('clearCloneData', {
        'cloneId': cloneId,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to clear data', error: e);
      return false;
    }
  }

  Future<bool> setMaxConcurrentClones(int maxClones) async {
    try {
      final result =
          await _channel.invokeMethod<bool>('setMaxConcurrentClones', {
        'maxClones': maxClones,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to set max concurrent clones', error: e);
      return false;
    }
  }

  Future<bool> setMemoryLimitPerClone(int limitMb) async {
    try {
      final result =
          await _channel.invokeMethod<bool>('setMemoryLimitPerClone', {
        'limitMb': limitMb,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to set memory limit', error: e);
      return false;
    }
  }

  Future<GmsState> getGmsState() async {
    try {
      final result = await _channel.invokeMethod<Map>('getGmsState');
      if (result == null) {
        return const GmsState(
          gmsAvailable: false,
          gmsVersion: null,
          playStoreVersion: null,
          gsfAvailable: false,
          maxPlayStoreClones: 12,
          activePlayStoreClones: 0,
        );
      }
      return GmsState(
        gmsAvailable: result['gmsAvailable'] as bool? ?? false,
        gmsVersion: result['gmsVersion'] as String?,
        playStoreVersion: result['playStoreVersion'] as String?,
        gsfAvailable: result['gsfAvailable'] as bool? ?? false,
        maxPlayStoreClones: result['maxPlayStoreClones'] as int? ?? 12,
        activePlayStoreClones: result['activePlayStoreClones'] as int? ?? 0,
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
      final result =
          await _channel.invokeMethod<String>('createPlayStoreClone', {
        if (devicePreset != null) 'devicePreset': devicePreset,
      });
      return result;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to create Play Store clone', error: e);
      return null;
    }
  }

  Future<bool> deletePlayStoreClone(String cloneId) async {
    try {
      final result =
          await _channel.invokeMethod<bool>('deletePlayStoreClone', {
        'cloneId': cloneId,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to delete Play Store clone', error: e);
      return false;
    }
  }

  Future<CompatReport> checkCompatibility() async {
    try {
      final result = await _channel.invokeMethod<Map>('checkCompatibility');
      if (result == null) {
        return CompatReport(
          apiLevel: 0,
          androidVersion: 'Unknown',
          isSupported: false,
          issues: [],
          missingPermissions: [],
          recommendations: [],
        );
      }
      return CompatReport(
        apiLevel: result['apiLevel'] as int? ?? 0,
        androidVersion: result['androidVersion'] as String? ?? 'Unknown',
        isSupported: result['isSupported'] as bool? ?? false,
        issues: (result['issues'] as List?)?.cast<String>() ?? [],
        missingPermissions:
            (result['missingPermissions'] as List?)?.cast<String>() ?? [],
        recommendations:
            (result['recommendations'] as List?)?.cast<String>() ?? [],
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to check compatibility', error: e);
      return CompatReport(
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
      final result =
          await _channel.invokeMethod<Map>('getBatteryOptimizationInfo');
      if (result == null) {
        return const BatteryInfo(
          isIgnoringOptimization: false,
          oemBrand: 'Unknown',
          oemIssue: null,
        );
      }
      return BatteryInfo(
        isIgnoringOptimization:
            result['isIgnoringOptimization'] as bool? ?? false,
        oemBrand: result['oemBrand'] as String? ?? 'Unknown',
        oemIssue: result['oemIssue'] as String?,
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
      await _channel.invokeMethod<void>('startForegroundService', {
        'runningCount': runningCount,
      });
    } on PlatformException catch (e) {
      AppLogger.error('Failed to start foreground service', error: e);
    }
  }

  Future<void> stopForegroundService() async {
    try {
      await _channel.invokeMethod<void>('stopForegroundService');
    } on PlatformException catch (e) {
      AppLogger.error('Failed to stop foreground service', error: e);
    }
  }

  Future<MemorySnapshot> getMemorySnapshot() async {
    try {
      final result = await _channel.invokeMethod<Map>('getMemorySnapshot');
      if (result == null) return MemorySnapshot.empty();
      return MemorySnapshot(
        totalDeviceRamMb: result['totalDeviceRamMb'] as int? ?? 0,
        availableRamMb: result['availableRamMb'] as int? ?? 0,
        engineNativeHeapMb: result['engineNativeHeapMb'] as int? ?? 0,
        engineJavaHeapMb: result['engineJavaHeapMb'] as int? ?? 0,
        cloneProcessCount: result['cloneProcessCount'] as int? ?? 0,
        estimatedCloneOverheadMb:
            result['estimatedCloneOverheadMb'] as int? ?? 0,
        isLowMemory: result['isLowMemory'] as bool? ?? false,
        recommendedMaxClones: result['recommendedMaxClones'] as int? ?? 3,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get memory snapshot', error: e);
      return MemorySnapshot.empty();
    }
  }

  Future<SecurityStatus> performSecurityCheck() async {
    try {
      final result =
          await _channel.invokeMethod<Map>('performSecurityCheck');
      if (result == null) return SecurityStatus.unknown();
      return SecurityStatus(
        signatureValid: result['signatureValid'] as bool? ?? false,
        debuggerAttached: result['debuggerAttached'] as bool? ?? false,
        deviceRooted: result['deviceRooted'] as bool? ?? false,
        isEmulator: result['isEmulator'] as bool? ?? false,
        nativeLibsIntact: result['nativeLibsIntact'] as bool? ?? false,
        overallSecure: result['overallSecure'] as bool? ?? false,
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to perform security check', error: e);
      return SecurityStatus.unknown();
    }
  }

  Future<PerformanceMetrics> getPerformanceMetrics() async {
    try {
      final result =
          await _channel.invokeMethod<Map>('getPerformanceMetrics');
      if (result == null) return PerformanceMetrics.empty();
      return PerformanceMetrics(
        avgColdLaunchMs: result['avgColdLaunchMs'] as int? ?? 0,
        avgWarmLaunchMs: result['avgWarmLaunchMs'] as int? ?? 0,
        avgProfileLoadMs: result['avgProfileLoadMs'] as int? ?? 0,
        totalLaunches: result['totalLaunches'] as int? ?? 0,
        batteryLevel: result['batteryLevel'] as int? ?? 0,
        isCharging: result['isCharging'] as bool? ?? false,
        powerRecommendation:
            result['powerRecommendation'] as String? ?? 'UNKNOWN',
      );
    } on PlatformException catch (e) {
      AppLogger.error('Failed to get performance metrics', error: e);
      return PerformanceMetrics.empty();
    }
  }

  Future<bool> canLaunchClone() async {
    try {
      final result = await _channel.invokeMethod<bool>('canLaunchClone');
      return result ?? true;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to check launch capability', error: e);
      return true;
    }
  }

  Future<void> requestGc() async {
    try {
      await _channel.invokeMethod<void>('requestGc');
    } on PlatformException catch (e) {
      AppLogger.error('Failed to request GC', error: e);
    }
  }

  Future<bool> encryptCloneData(String cloneId) async {
    try {
      final result = await _channel.invokeMethod<bool>('encryptCloneData', {
        'cloneId': cloneId,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to encrypt clone data', error: e);
      return false;
    }
  }

  Future<bool> secureDeleteCloneData(String cloneId) async {
    try {
      final result =
          await _channel.invokeMethod<bool>('secureDeleteCloneData', {
        'cloneId': cloneId,
      });
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to secure delete clone data', error: e);
      return false;
    }
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

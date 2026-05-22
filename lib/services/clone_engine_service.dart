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

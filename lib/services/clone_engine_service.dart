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

  Future<bool> initializeEngine() async {
    try {
      final result = await _channel.invokeMethod<bool>('initializeEngine');
      return result ?? false;
    } on PlatformException catch (e) {
      AppLogger.error('Failed to initialize engine', error: e);
      return false;
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
    DeviceProfile? profile,
  }) async {
    try {
      final result = await _channel.invokeMethod<Map>('createClone', {
        'packageName': packageName,
        'userId': userId,
        if (profile != null) 'profile': profile.toJson(),
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
}

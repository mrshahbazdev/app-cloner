import 'package:freezed_annotation/freezed_annotation.dart';

import 'clone_status.dart';
import 'device_profile.dart';

part 'clone_info.freezed.dart';
part 'clone_info.g.dart';

@freezed
abstract class CloneInfo with _$CloneInfo {
  const factory CloneInfo({
    required String id,
    required String packageName,
    required String appName,
    required int userId,
    required CloneStatus status,
    required DateTime createdAt,
    DeviceProfile? profile,
    String? appIconPath,
    int? memoryUsageMb,
    DateTime? lastLaunched,
  }) = _CloneInfo;

  factory CloneInfo.fromJson(Map<String, dynamic> json) =>
      _$CloneInfoFromJson(json);
}

@freezed
abstract class InstalledApp with _$InstalledApp {
  const factory InstalledApp({
    required String packageName,
    required String appName,
    String? iconPath,
    String? versionName,
    int? versionCode,
    @Default(false) bool isSystemApp,
  }) = _InstalledApp;

  factory InstalledApp.fromJson(Map<String, dynamic> json) =>
      _$InstalledAppFromJson(json);
}

@freezed
abstract class CloneEvent with _$CloneEvent {
  const factory CloneEvent({
    required String cloneId,
    required String eventType,
    String? message,
    Map<String, dynamic>? data,
  }) = _CloneEvent;

  factory CloneEvent.fromJson(Map<String, dynamic> json) =>
      _$CloneEventFromJson(json);
}

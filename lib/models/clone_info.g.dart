// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'clone_info.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_CloneInfo _$CloneInfoFromJson(Map<String, dynamic> json) => _CloneInfo(
  id: json['id'] as String,
  packageName: json['packageName'] as String,
  appName: json['appName'] as String,
  userId: (json['userId'] as num).toInt(),
  status: $enumDecode(_$CloneStatusEnumMap, json['status']),
  createdAt: DateTime.parse(json['createdAt'] as String),
  profile:
      json['profile'] == null
          ? null
          : DeviceProfile.fromJson(json['profile'] as Map<String, dynamic>),
  appIconPath: json['appIconPath'] as String?,
  memoryUsageMb: (json['memoryUsageMb'] as num?)?.toInt(),
  lastLaunched:
      json['lastLaunched'] == null
          ? null
          : DateTime.parse(json['lastLaunched'] as String),
);

Map<String, dynamic> _$CloneInfoToJson(_CloneInfo instance) =>
    <String, dynamic>{
      'id': instance.id,
      'packageName': instance.packageName,
      'appName': instance.appName,
      'userId': instance.userId,
      'status': _$CloneStatusEnumMap[instance.status]!,
      'createdAt': instance.createdAt.toIso8601String(),
      'profile': instance.profile,
      'appIconPath': instance.appIconPath,
      'memoryUsageMb': instance.memoryUsageMb,
      'lastLaunched': instance.lastLaunched?.toIso8601String(),
    };

const _$CloneStatusEnumMap = {
  CloneStatus.installing: 'installing',
  CloneStatus.ready: 'ready',
  CloneStatus.running: 'running',
  CloneStatus.stopped: 'stopped',
  CloneStatus.error: 'error',
};

_InstalledApp _$InstalledAppFromJson(Map<String, dynamic> json) =>
    _InstalledApp(
      packageName: json['packageName'] as String,
      appName: json['appName'] as String,
      iconPath: json['iconPath'] as String?,
      versionName: json['versionName'] as String?,
      versionCode: (json['versionCode'] as num?)?.toInt(),
      isSystemApp: json['isSystemApp'] as bool? ?? false,
    );

Map<String, dynamic> _$InstalledAppToJson(_InstalledApp instance) =>
    <String, dynamic>{
      'packageName': instance.packageName,
      'appName': instance.appName,
      'iconPath': instance.iconPath,
      'versionName': instance.versionName,
      'versionCode': instance.versionCode,
      'isSystemApp': instance.isSystemApp,
    };

_CloneEvent _$CloneEventFromJson(Map<String, dynamic> json) => _CloneEvent(
  cloneId: json['cloneId'] as String,
  eventType: json['eventType'] as String,
  message: json['message'] as String?,
  data: json['data'] as Map<String, dynamic>?,
);

Map<String, dynamic> _$CloneEventToJson(_CloneEvent instance) =>
    <String, dynamic>{
      'cloneId': instance.cloneId,
      'eventType': instance.eventType,
      'message': instance.message,
      'data': instance.data,
    };

// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'device_profile.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_DeviceProfile _$DeviceProfileFromJson(Map<String, dynamic> json) =>
    _DeviceProfile(
      id: json['id'] as String,
      name: json['name'] as String,
      model: json['model'] as String,
      brand: json['brand'] as String,
      manufacturer: json['manufacturer'] as String,
      fingerprint: json['fingerprint'] as String,
      screenDensity: (json['screenDensity'] as num).toInt(),
      screenWidth: (json['screenWidth'] as num).toInt(),
      screenHeight: (json['screenHeight'] as num).toInt(),
      abis: (json['abis'] as List<dynamic>).map((e) => e as String).toList(),
      sdkVersion: (json['sdkVersion'] as num).toInt(),
      releaseVersion: json['releaseVersion'] as String,
      androidId: json['androidId'] as String,
      imei: json['imei'] as String,
      macAddress: json['macAddress'] as String,
      bluetoothMac: json['bluetoothMac'] as String,
      gsfId: json['gsfId'] as String,
      advertisingId: json['advertisingId'] as String,
      serialNumber: json['serialNumber'] as String?,
      timezone: json['timezone'] as String?,
      locale: json['locale'] as String?,
      proxyHost: json['proxyHost'] as String?,
      proxyPort: (json['proxyPort'] as num?)?.toInt(),
      proxyType: json['proxyType'] as String?,
    );

Map<String, dynamic> _$DeviceProfileToJson(_DeviceProfile instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'model': instance.model,
      'brand': instance.brand,
      'manufacturer': instance.manufacturer,
      'fingerprint': instance.fingerprint,
      'screenDensity': instance.screenDensity,
      'screenWidth': instance.screenWidth,
      'screenHeight': instance.screenHeight,
      'abis': instance.abis,
      'sdkVersion': instance.sdkVersion,
      'releaseVersion': instance.releaseVersion,
      'androidId': instance.androidId,
      'imei': instance.imei,
      'macAddress': instance.macAddress,
      'bluetoothMac': instance.bluetoothMac,
      'gsfId': instance.gsfId,
      'advertisingId': instance.advertisingId,
      'serialNumber': instance.serialNumber,
      'timezone': instance.timezone,
      'locale': instance.locale,
      'proxyHost': instance.proxyHost,
      'proxyPort': instance.proxyPort,
      'proxyType': instance.proxyType,
    };

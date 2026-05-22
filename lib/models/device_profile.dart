import 'package:freezed_annotation/freezed_annotation.dart';

part 'device_profile.freezed.dart';
part 'device_profile.g.dart';

@freezed
abstract class DeviceProfile with _$DeviceProfile {
  const factory DeviceProfile({
    required String id,
    required String name,
    required String model,
    required String brand,
    required String manufacturer,
    required String fingerprint,
    required int screenDensity,
    required int screenWidth,
    required int screenHeight,
    required List<String> abis,
    required int sdkVersion,
    required String releaseVersion,
    required String androidId,
    required String imei,
    required String macAddress,
    required String bluetoothMac,
    required String gsfId,
    required String advertisingId,
    String? serialNumber,
    String? timezone,
    String? locale,
    String? proxyHost,
    int? proxyPort,
    String? proxyType,
  }) = _DeviceProfile;

  factory DeviceProfile.fromJson(Map<String, dynamic> json) =>
      _$DeviceProfileFromJson(json);
}

abstract final class DevicePresets {
  static const List<Map<String, dynamic>> presets = [
    {
      'name': 'Google Pixel 8 Pro',
      'model': 'husky',
      'brand': 'google',
      'manufacturer': 'Google',
      'fingerprint':
          'google/husky/husky:14/AP2A.240805.005/12025142:user/release-keys',
      'screenDensity': 420,
      'screenWidth': 1344,
      'screenHeight': 2992,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Samsung Galaxy S24 Ultra',
      'model': 'SM-S928B',
      'brand': 'samsung',
      'manufacturer': 'samsung',
      'fingerprint':
          'samsung/dm3q/dm3q:14/UP1A.231005.007/S928BXXS1AXB1:user/release-keys',
      'screenDensity': 480,
      'screenWidth': 1440,
      'screenHeight': 3120,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'OnePlus 12',
      'model': 'CPH2583',
      'brand': 'OnePlus',
      'manufacturer': 'OnePlus',
      'fingerprint':
          'OnePlus/CPH2583/OP5913L1:14/UKQ1.230924.001/1704180000:user/release-keys',
      'screenDensity': 480,
      'screenWidth': 1440,
      'screenHeight': 3168,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Xiaomi 14 Pro',
      'model': '23116PN5BC',
      'brand': 'Xiaomi',
      'manufacturer': 'Xiaomi',
      'fingerprint':
          'Xiaomi/missi/missi:14/UKQ1.231003.002/V816.0.3.0.UNACNXM:user/release-keys',
      'screenDensity': 480,
      'screenWidth': 1440,
      'screenHeight': 3200,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Google Pixel 7',
      'model': 'panther',
      'brand': 'google',
      'manufacturer': 'Google',
      'fingerprint':
          'google/panther/panther:14/AP2A.240805.005/12025142:user/release-keys',
      'screenDensity': 420,
      'screenWidth': 1080,
      'screenHeight': 2400,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Samsung Galaxy A54',
      'model': 'SM-A546B',
      'brand': 'samsung',
      'manufacturer': 'samsung',
      'fingerprint':
          'samsung/a54xnsxx/a54x:14/UP1A.231005.007/A546BXXS7CXA2:user/release-keys',
      'screenDensity': 393,
      'screenWidth': 1080,
      'screenHeight': 2340,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Sony Xperia 1 V',
      'model': 'XQ-DQ72',
      'brand': 'Sony',
      'manufacturer': 'Sony',
      'fingerprint':
          'Sony/XQ-DQ72/XQ-DQ72:14/67.2.A.2.45/067002A002004500:user/release-keys',
      'screenDensity': 480,
      'screenWidth': 1644,
      'screenHeight': 3840,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Nothing Phone (2)',
      'model': 'A065',
      'brand': 'Nothing',
      'manufacturer': 'Nothing',
      'fingerprint':
          'Nothing/Pong/Pong:14/UKQ1.230924.001/2401100141:user/release-keys',
      'screenDensity': 420,
      'screenWidth': 1080,
      'screenHeight': 2412,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Motorola Edge 40 Pro',
      'model': 'XT2301-4',
      'brand': 'motorola',
      'manufacturer': 'Motorola',
      'fingerprint':
          'motorola/eqs_g/eqs:14/U1TQS34.66-18-2-8/14af62:user/release-keys',
      'screenDensity': 393,
      'screenWidth': 1080,
      'screenHeight': 2400,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Oppo Find X7 Ultra',
      'model': 'PHZ110',
      'brand': 'OPPO',
      'manufacturer': 'OPPO',
      'fingerprint':
          'OPPO/PHZ110/OP5B11L1:14/UKQ1.230924.001/1704180000:user/release-keys',
      'screenDensity': 480,
      'screenWidth': 1440,
      'screenHeight': 3168,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Realme GT 5 Pro',
      'model': 'RMX3888',
      'brand': 'realme',
      'manufacturer': 'realme',
      'fingerprint':
          'realme/RMX3888/RE58C2L1:14/UKQ1.230924.001/1704180000:user/release-keys',
      'screenDensity': 480,
      'screenWidth': 1264,
      'screenHeight': 2780,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
    {
      'name': 'Vivo X100 Pro',
      'model': 'V2324A',
      'brand': 'vivo',
      'manufacturer': 'vivo',
      'fingerprint':
          'vivo/V2324A/V2324A:14/UKQ1.230924.001/1704180000:user/release-keys',
      'screenDensity': 480,
      'screenWidth': 1440,
      'screenHeight': 3200,
      'abis': ['arm64-v8a', 'armeabi-v7a'],
      'sdkVersion': 34,
      'releaseVersion': '14',
    },
  ];
}

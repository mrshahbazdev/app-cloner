// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'device_profile.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$DeviceProfile {

 String get id; String get name; String get model; String get brand; String get manufacturer; String get fingerprint; int get screenDensity; int get screenWidth; int get screenHeight; List<String> get abis; int get sdkVersion; String get releaseVersion; String get androidId; String get imei; String get macAddress; String get bluetoothMac; String get gsfId; String get advertisingId; String? get serialNumber; String? get timezone; String? get locale; String? get proxyHost; int? get proxyPort; String? get proxyType;
/// Create a copy of DeviceProfile
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$DeviceProfileCopyWith<DeviceProfile> get copyWith => _$DeviceProfileCopyWithImpl<DeviceProfile>(this as DeviceProfile, _$identity);

  /// Serializes this DeviceProfile to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is DeviceProfile&&(identical(other.id, id) || other.id == id)&&(identical(other.name, name) || other.name == name)&&(identical(other.model, model) || other.model == model)&&(identical(other.brand, brand) || other.brand == brand)&&(identical(other.manufacturer, manufacturer) || other.manufacturer == manufacturer)&&(identical(other.fingerprint, fingerprint) || other.fingerprint == fingerprint)&&(identical(other.screenDensity, screenDensity) || other.screenDensity == screenDensity)&&(identical(other.screenWidth, screenWidth) || other.screenWidth == screenWidth)&&(identical(other.screenHeight, screenHeight) || other.screenHeight == screenHeight)&&const DeepCollectionEquality().equals(other.abis, abis)&&(identical(other.sdkVersion, sdkVersion) || other.sdkVersion == sdkVersion)&&(identical(other.releaseVersion, releaseVersion) || other.releaseVersion == releaseVersion)&&(identical(other.androidId, androidId) || other.androidId == androidId)&&(identical(other.imei, imei) || other.imei == imei)&&(identical(other.macAddress, macAddress) || other.macAddress == macAddress)&&(identical(other.bluetoothMac, bluetoothMac) || other.bluetoothMac == bluetoothMac)&&(identical(other.gsfId, gsfId) || other.gsfId == gsfId)&&(identical(other.advertisingId, advertisingId) || other.advertisingId == advertisingId)&&(identical(other.serialNumber, serialNumber) || other.serialNumber == serialNumber)&&(identical(other.timezone, timezone) || other.timezone == timezone)&&(identical(other.locale, locale) || other.locale == locale)&&(identical(other.proxyHost, proxyHost) || other.proxyHost == proxyHost)&&(identical(other.proxyPort, proxyPort) || other.proxyPort == proxyPort)&&(identical(other.proxyType, proxyType) || other.proxyType == proxyType));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hashAll([runtimeType,id,name,model,brand,manufacturer,fingerprint,screenDensity,screenWidth,screenHeight,const DeepCollectionEquality().hash(abis),sdkVersion,releaseVersion,androidId,imei,macAddress,bluetoothMac,gsfId,advertisingId,serialNumber,timezone,locale,proxyHost,proxyPort,proxyType]);

@override
String toString() {
  return 'DeviceProfile(id: $id, name: $name, model: $model, brand: $brand, manufacturer: $manufacturer, fingerprint: $fingerprint, screenDensity: $screenDensity, screenWidth: $screenWidth, screenHeight: $screenHeight, abis: $abis, sdkVersion: $sdkVersion, releaseVersion: $releaseVersion, androidId: $androidId, imei: $imei, macAddress: $macAddress, bluetoothMac: $bluetoothMac, gsfId: $gsfId, advertisingId: $advertisingId, serialNumber: $serialNumber, timezone: $timezone, locale: $locale, proxyHost: $proxyHost, proxyPort: $proxyPort, proxyType: $proxyType)';
}


}

/// @nodoc
abstract mixin class $DeviceProfileCopyWith<$Res>  {
  factory $DeviceProfileCopyWith(DeviceProfile value, $Res Function(DeviceProfile) _then) = _$DeviceProfileCopyWithImpl;
@useResult
$Res call({
 String id, String name, String model, String brand, String manufacturer, String fingerprint, int screenDensity, int screenWidth, int screenHeight, List<String> abis, int sdkVersion, String releaseVersion, String androidId, String imei, String macAddress, String bluetoothMac, String gsfId, String advertisingId, String? serialNumber, String? timezone, String? locale, String? proxyHost, int? proxyPort, String? proxyType
});




}
/// @nodoc
class _$DeviceProfileCopyWithImpl<$Res>
    implements $DeviceProfileCopyWith<$Res> {
  _$DeviceProfileCopyWithImpl(this._self, this._then);

  final DeviceProfile _self;
  final $Res Function(DeviceProfile) _then;

/// Create a copy of DeviceProfile
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,Object? name = null,Object? model = null,Object? brand = null,Object? manufacturer = null,Object? fingerprint = null,Object? screenDensity = null,Object? screenWidth = null,Object? screenHeight = null,Object? abis = null,Object? sdkVersion = null,Object? releaseVersion = null,Object? androidId = null,Object? imei = null,Object? macAddress = null,Object? bluetoothMac = null,Object? gsfId = null,Object? advertisingId = null,Object? serialNumber = freezed,Object? timezone = freezed,Object? locale = freezed,Object? proxyHost = freezed,Object? proxyPort = freezed,Object? proxyType = freezed,}) {
  return _then(_self.copyWith(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,name: null == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String,model: null == model ? _self.model : model // ignore: cast_nullable_to_non_nullable
as String,brand: null == brand ? _self.brand : brand // ignore: cast_nullable_to_non_nullable
as String,manufacturer: null == manufacturer ? _self.manufacturer : manufacturer // ignore: cast_nullable_to_non_nullable
as String,fingerprint: null == fingerprint ? _self.fingerprint : fingerprint // ignore: cast_nullable_to_non_nullable
as String,screenDensity: null == screenDensity ? _self.screenDensity : screenDensity // ignore: cast_nullable_to_non_nullable
as int,screenWidth: null == screenWidth ? _self.screenWidth : screenWidth // ignore: cast_nullable_to_non_nullable
as int,screenHeight: null == screenHeight ? _self.screenHeight : screenHeight // ignore: cast_nullable_to_non_nullable
as int,abis: null == abis ? _self.abis : abis // ignore: cast_nullable_to_non_nullable
as List<String>,sdkVersion: null == sdkVersion ? _self.sdkVersion : sdkVersion // ignore: cast_nullable_to_non_nullable
as int,releaseVersion: null == releaseVersion ? _self.releaseVersion : releaseVersion // ignore: cast_nullable_to_non_nullable
as String,androidId: null == androidId ? _self.androidId : androidId // ignore: cast_nullable_to_non_nullable
as String,imei: null == imei ? _self.imei : imei // ignore: cast_nullable_to_non_nullable
as String,macAddress: null == macAddress ? _self.macAddress : macAddress // ignore: cast_nullable_to_non_nullable
as String,bluetoothMac: null == bluetoothMac ? _self.bluetoothMac : bluetoothMac // ignore: cast_nullable_to_non_nullable
as String,gsfId: null == gsfId ? _self.gsfId : gsfId // ignore: cast_nullable_to_non_nullable
as String,advertisingId: null == advertisingId ? _self.advertisingId : advertisingId // ignore: cast_nullable_to_non_nullable
as String,serialNumber: freezed == serialNumber ? _self.serialNumber : serialNumber // ignore: cast_nullable_to_non_nullable
as String?,timezone: freezed == timezone ? _self.timezone : timezone // ignore: cast_nullable_to_non_nullable
as String?,locale: freezed == locale ? _self.locale : locale // ignore: cast_nullable_to_non_nullable
as String?,proxyHost: freezed == proxyHost ? _self.proxyHost : proxyHost // ignore: cast_nullable_to_non_nullable
as String?,proxyPort: freezed == proxyPort ? _self.proxyPort : proxyPort // ignore: cast_nullable_to_non_nullable
as int?,proxyType: freezed == proxyType ? _self.proxyType : proxyType // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}

}


/// Adds pattern-matching-related methods to [DeviceProfile].
extension DeviceProfilePatterns on DeviceProfile {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _DeviceProfile value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _DeviceProfile() when $default != null:
return $default(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _DeviceProfile value)  $default,){
final _that = this;
switch (_that) {
case _DeviceProfile():
return $default(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _DeviceProfile value)?  $default,){
final _that = this;
switch (_that) {
case _DeviceProfile() when $default != null:
return $default(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String id,  String name,  String model,  String brand,  String manufacturer,  String fingerprint,  int screenDensity,  int screenWidth,  int screenHeight,  List<String> abis,  int sdkVersion,  String releaseVersion,  String androidId,  String imei,  String macAddress,  String bluetoothMac,  String gsfId,  String advertisingId,  String? serialNumber,  String? timezone,  String? locale,  String? proxyHost,  int? proxyPort,  String? proxyType)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _DeviceProfile() when $default != null:
return $default(_that.id,_that.name,_that.model,_that.brand,_that.manufacturer,_that.fingerprint,_that.screenDensity,_that.screenWidth,_that.screenHeight,_that.abis,_that.sdkVersion,_that.releaseVersion,_that.androidId,_that.imei,_that.macAddress,_that.bluetoothMac,_that.gsfId,_that.advertisingId,_that.serialNumber,_that.timezone,_that.locale,_that.proxyHost,_that.proxyPort,_that.proxyType);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String id,  String name,  String model,  String brand,  String manufacturer,  String fingerprint,  int screenDensity,  int screenWidth,  int screenHeight,  List<String> abis,  int sdkVersion,  String releaseVersion,  String androidId,  String imei,  String macAddress,  String bluetoothMac,  String gsfId,  String advertisingId,  String? serialNumber,  String? timezone,  String? locale,  String? proxyHost,  int? proxyPort,  String? proxyType)  $default,) {final _that = this;
switch (_that) {
case _DeviceProfile():
return $default(_that.id,_that.name,_that.model,_that.brand,_that.manufacturer,_that.fingerprint,_that.screenDensity,_that.screenWidth,_that.screenHeight,_that.abis,_that.sdkVersion,_that.releaseVersion,_that.androidId,_that.imei,_that.macAddress,_that.bluetoothMac,_that.gsfId,_that.advertisingId,_that.serialNumber,_that.timezone,_that.locale,_that.proxyHost,_that.proxyPort,_that.proxyType);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String id,  String name,  String model,  String brand,  String manufacturer,  String fingerprint,  int screenDensity,  int screenWidth,  int screenHeight,  List<String> abis,  int sdkVersion,  String releaseVersion,  String androidId,  String imei,  String macAddress,  String bluetoothMac,  String gsfId,  String advertisingId,  String? serialNumber,  String? timezone,  String? locale,  String? proxyHost,  int? proxyPort,  String? proxyType)?  $default,) {final _that = this;
switch (_that) {
case _DeviceProfile() when $default != null:
return $default(_that.id,_that.name,_that.model,_that.brand,_that.manufacturer,_that.fingerprint,_that.screenDensity,_that.screenWidth,_that.screenHeight,_that.abis,_that.sdkVersion,_that.releaseVersion,_that.androidId,_that.imei,_that.macAddress,_that.bluetoothMac,_that.gsfId,_that.advertisingId,_that.serialNumber,_that.timezone,_that.locale,_that.proxyHost,_that.proxyPort,_that.proxyType);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _DeviceProfile implements DeviceProfile {
  const _DeviceProfile({required this.id, required this.name, required this.model, required this.brand, required this.manufacturer, required this.fingerprint, required this.screenDensity, required this.screenWidth, required this.screenHeight, required final  List<String> abis, required this.sdkVersion, required this.releaseVersion, required this.androidId, required this.imei, required this.macAddress, required this.bluetoothMac, required this.gsfId, required this.advertisingId, this.serialNumber, this.timezone, this.locale, this.proxyHost, this.proxyPort, this.proxyType}): _abis = abis;
  factory _DeviceProfile.fromJson(Map<String, dynamic> json) => _$DeviceProfileFromJson(json);

@override final  String id;
@override final  String name;
@override final  String model;
@override final  String brand;
@override final  String manufacturer;
@override final  String fingerprint;
@override final  int screenDensity;
@override final  int screenWidth;
@override final  int screenHeight;
 final  List<String> _abis;
@override List<String> get abis {
  if (_abis is EqualUnmodifiableListView) return _abis;
  // ignore: implicit_dynamic_type
  return EqualUnmodifiableListView(_abis);
}

@override final  int sdkVersion;
@override final  String releaseVersion;
@override final  String androidId;
@override final  String imei;
@override final  String macAddress;
@override final  String bluetoothMac;
@override final  String gsfId;
@override final  String advertisingId;
@override final  String? serialNumber;
@override final  String? timezone;
@override final  String? locale;
@override final  String? proxyHost;
@override final  int? proxyPort;
@override final  String? proxyType;

/// Create a copy of DeviceProfile
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$DeviceProfileCopyWith<_DeviceProfile> get copyWith => __$DeviceProfileCopyWithImpl<_DeviceProfile>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$DeviceProfileToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _DeviceProfile&&(identical(other.id, id) || other.id == id)&&(identical(other.name, name) || other.name == name)&&(identical(other.model, model) || other.model == model)&&(identical(other.brand, brand) || other.brand == brand)&&(identical(other.manufacturer, manufacturer) || other.manufacturer == manufacturer)&&(identical(other.fingerprint, fingerprint) || other.fingerprint == fingerprint)&&(identical(other.screenDensity, screenDensity) || other.screenDensity == screenDensity)&&(identical(other.screenWidth, screenWidth) || other.screenWidth == screenWidth)&&(identical(other.screenHeight, screenHeight) || other.screenHeight == screenHeight)&&const DeepCollectionEquality().equals(other._abis, _abis)&&(identical(other.sdkVersion, sdkVersion) || other.sdkVersion == sdkVersion)&&(identical(other.releaseVersion, releaseVersion) || other.releaseVersion == releaseVersion)&&(identical(other.androidId, androidId) || other.androidId == androidId)&&(identical(other.imei, imei) || other.imei == imei)&&(identical(other.macAddress, macAddress) || other.macAddress == macAddress)&&(identical(other.bluetoothMac, bluetoothMac) || other.bluetoothMac == bluetoothMac)&&(identical(other.gsfId, gsfId) || other.gsfId == gsfId)&&(identical(other.advertisingId, advertisingId) || other.advertisingId == advertisingId)&&(identical(other.serialNumber, serialNumber) || other.serialNumber == serialNumber)&&(identical(other.timezone, timezone) || other.timezone == timezone)&&(identical(other.locale, locale) || other.locale == locale)&&(identical(other.proxyHost, proxyHost) || other.proxyHost == proxyHost)&&(identical(other.proxyPort, proxyPort) || other.proxyPort == proxyPort)&&(identical(other.proxyType, proxyType) || other.proxyType == proxyType));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hashAll([runtimeType,id,name,model,brand,manufacturer,fingerprint,screenDensity,screenWidth,screenHeight,const DeepCollectionEquality().hash(_abis),sdkVersion,releaseVersion,androidId,imei,macAddress,bluetoothMac,gsfId,advertisingId,serialNumber,timezone,locale,proxyHost,proxyPort,proxyType]);

@override
String toString() {
  return 'DeviceProfile(id: $id, name: $name, model: $model, brand: $brand, manufacturer: $manufacturer, fingerprint: $fingerprint, screenDensity: $screenDensity, screenWidth: $screenWidth, screenHeight: $screenHeight, abis: $abis, sdkVersion: $sdkVersion, releaseVersion: $releaseVersion, androidId: $androidId, imei: $imei, macAddress: $macAddress, bluetoothMac: $bluetoothMac, gsfId: $gsfId, advertisingId: $advertisingId, serialNumber: $serialNumber, timezone: $timezone, locale: $locale, proxyHost: $proxyHost, proxyPort: $proxyPort, proxyType: $proxyType)';
}


}

/// @nodoc
abstract mixin class _$DeviceProfileCopyWith<$Res> implements $DeviceProfileCopyWith<$Res> {
  factory _$DeviceProfileCopyWith(_DeviceProfile value, $Res Function(_DeviceProfile) _then) = __$DeviceProfileCopyWithImpl;
@override @useResult
$Res call({
 String id, String name, String model, String brand, String manufacturer, String fingerprint, int screenDensity, int screenWidth, int screenHeight, List<String> abis, int sdkVersion, String releaseVersion, String androidId, String imei, String macAddress, String bluetoothMac, String gsfId, String advertisingId, String? serialNumber, String? timezone, String? locale, String? proxyHost, int? proxyPort, String? proxyType
});




}
/// @nodoc
class __$DeviceProfileCopyWithImpl<$Res>
    implements _$DeviceProfileCopyWith<$Res> {
  __$DeviceProfileCopyWithImpl(this._self, this._then);

  final _DeviceProfile _self;
  final $Res Function(_DeviceProfile) _then;

/// Create a copy of DeviceProfile
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? name = null,Object? model = null,Object? brand = null,Object? manufacturer = null,Object? fingerprint = null,Object? screenDensity = null,Object? screenWidth = null,Object? screenHeight = null,Object? abis = null,Object? sdkVersion = null,Object? releaseVersion = null,Object? androidId = null,Object? imei = null,Object? macAddress = null,Object? bluetoothMac = null,Object? gsfId = null,Object? advertisingId = null,Object? serialNumber = freezed,Object? timezone = freezed,Object? locale = freezed,Object? proxyHost = freezed,Object? proxyPort = freezed,Object? proxyType = freezed,}) {
  return _then(_DeviceProfile(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,name: null == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String,model: null == model ? _self.model : model // ignore: cast_nullable_to_non_nullable
as String,brand: null == brand ? _self.brand : brand // ignore: cast_nullable_to_non_nullable
as String,manufacturer: null == manufacturer ? _self.manufacturer : manufacturer // ignore: cast_nullable_to_non_nullable
as String,fingerprint: null == fingerprint ? _self.fingerprint : fingerprint // ignore: cast_nullable_to_non_nullable
as String,screenDensity: null == screenDensity ? _self.screenDensity : screenDensity // ignore: cast_nullable_to_non_nullable
as int,screenWidth: null == screenWidth ? _self.screenWidth : screenWidth // ignore: cast_nullable_to_non_nullable
as int,screenHeight: null == screenHeight ? _self.screenHeight : screenHeight // ignore: cast_nullable_to_non_nullable
as int,abis: null == abis ? _self._abis : abis // ignore: cast_nullable_to_non_nullable
as List<String>,sdkVersion: null == sdkVersion ? _self.sdkVersion : sdkVersion // ignore: cast_nullable_to_non_nullable
as int,releaseVersion: null == releaseVersion ? _self.releaseVersion : releaseVersion // ignore: cast_nullable_to_non_nullable
as String,androidId: null == androidId ? _self.androidId : androidId // ignore: cast_nullable_to_non_nullable
as String,imei: null == imei ? _self.imei : imei // ignore: cast_nullable_to_non_nullable
as String,macAddress: null == macAddress ? _self.macAddress : macAddress // ignore: cast_nullable_to_non_nullable
as String,bluetoothMac: null == bluetoothMac ? _self.bluetoothMac : bluetoothMac // ignore: cast_nullable_to_non_nullable
as String,gsfId: null == gsfId ? _self.gsfId : gsfId // ignore: cast_nullable_to_non_nullable
as String,advertisingId: null == advertisingId ? _self.advertisingId : advertisingId // ignore: cast_nullable_to_non_nullable
as String,serialNumber: freezed == serialNumber ? _self.serialNumber : serialNumber // ignore: cast_nullable_to_non_nullable
as String?,timezone: freezed == timezone ? _self.timezone : timezone // ignore: cast_nullable_to_non_nullable
as String?,locale: freezed == locale ? _self.locale : locale // ignore: cast_nullable_to_non_nullable
as String?,proxyHost: freezed == proxyHost ? _self.proxyHost : proxyHost // ignore: cast_nullable_to_non_nullable
as String?,proxyPort: freezed == proxyPort ? _self.proxyPort : proxyPort // ignore: cast_nullable_to_non_nullable
as int?,proxyType: freezed == proxyType ? _self.proxyType : proxyType // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}


}

// dart format on

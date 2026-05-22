// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'clone_info.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$CloneInfo {

 String get id; String get packageName; String get appName; int get userId; CloneStatus get status; DateTime get createdAt; DeviceProfile? get profile; String? get appIconPath; int? get memoryUsageMb; DateTime? get lastLaunched;
/// Create a copy of CloneInfo
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$CloneInfoCopyWith<CloneInfo> get copyWith => _$CloneInfoCopyWithImpl<CloneInfo>(this as CloneInfo, _$identity);

  /// Serializes this CloneInfo to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is CloneInfo&&(identical(other.id, id) || other.id == id)&&(identical(other.packageName, packageName) || other.packageName == packageName)&&(identical(other.appName, appName) || other.appName == appName)&&(identical(other.userId, userId) || other.userId == userId)&&(identical(other.status, status) || other.status == status)&&(identical(other.createdAt, createdAt) || other.createdAt == createdAt)&&(identical(other.profile, profile) || other.profile == profile)&&(identical(other.appIconPath, appIconPath) || other.appIconPath == appIconPath)&&(identical(other.memoryUsageMb, memoryUsageMb) || other.memoryUsageMb == memoryUsageMb)&&(identical(other.lastLaunched, lastLaunched) || other.lastLaunched == lastLaunched));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,id,packageName,appName,userId,status,createdAt,profile,appIconPath,memoryUsageMb,lastLaunched);

@override
String toString() {
  return 'CloneInfo(id: $id, packageName: $packageName, appName: $appName, userId: $userId, status: $status, createdAt: $createdAt, profile: $profile, appIconPath: $appIconPath, memoryUsageMb: $memoryUsageMb, lastLaunched: $lastLaunched)';
}


}

/// @nodoc
abstract mixin class $CloneInfoCopyWith<$Res>  {
  factory $CloneInfoCopyWith(CloneInfo value, $Res Function(CloneInfo) _then) = _$CloneInfoCopyWithImpl;
@useResult
$Res call({
 String id, String packageName, String appName, int userId, CloneStatus status, DateTime createdAt, DeviceProfile? profile, String? appIconPath, int? memoryUsageMb, DateTime? lastLaunched
});


$DeviceProfileCopyWith<$Res>? get profile;

}
/// @nodoc
class _$CloneInfoCopyWithImpl<$Res>
    implements $CloneInfoCopyWith<$Res> {
  _$CloneInfoCopyWithImpl(this._self, this._then);

  final CloneInfo _self;
  final $Res Function(CloneInfo) _then;

/// Create a copy of CloneInfo
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,Object? packageName = null,Object? appName = null,Object? userId = null,Object? status = null,Object? createdAt = null,Object? profile = freezed,Object? appIconPath = freezed,Object? memoryUsageMb = freezed,Object? lastLaunched = freezed,}) {
  return _then(_self.copyWith(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,packageName: null == packageName ? _self.packageName : packageName // ignore: cast_nullable_to_non_nullable
as String,appName: null == appName ? _self.appName : appName // ignore: cast_nullable_to_non_nullable
as String,userId: null == userId ? _self.userId : userId // ignore: cast_nullable_to_non_nullable
as int,status: null == status ? _self.status : status // ignore: cast_nullable_to_non_nullable
as CloneStatus,createdAt: null == createdAt ? _self.createdAt : createdAt // ignore: cast_nullable_to_non_nullable
as DateTime,profile: freezed == profile ? _self.profile : profile // ignore: cast_nullable_to_non_nullable
as DeviceProfile?,appIconPath: freezed == appIconPath ? _self.appIconPath : appIconPath // ignore: cast_nullable_to_non_nullable
as String?,memoryUsageMb: freezed == memoryUsageMb ? _self.memoryUsageMb : memoryUsageMb // ignore: cast_nullable_to_non_nullable
as int?,lastLaunched: freezed == lastLaunched ? _self.lastLaunched : lastLaunched // ignore: cast_nullable_to_non_nullable
as DateTime?,
  ));
}
/// Create a copy of CloneInfo
/// with the given fields replaced by the non-null parameter values.
@override
@pragma('vm:prefer-inline')
$DeviceProfileCopyWith<$Res>? get profile {
    if (_self.profile == null) {
    return null;
  }

  return $DeviceProfileCopyWith<$Res>(_self.profile!, (value) {
    return _then(_self.copyWith(profile: value));
  });
}
}


/// Adds pattern-matching-related methods to [CloneInfo].
extension CloneInfoPatterns on CloneInfo {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _CloneInfo value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _CloneInfo() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _CloneInfo value)  $default,){
final _that = this;
switch (_that) {
case _CloneInfo():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _CloneInfo value)?  $default,){
final _that = this;
switch (_that) {
case _CloneInfo() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String id,  String packageName,  String appName,  int userId,  CloneStatus status,  DateTime createdAt,  DeviceProfile? profile,  String? appIconPath,  int? memoryUsageMb,  DateTime? lastLaunched)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _CloneInfo() when $default != null:
return $default(_that.id,_that.packageName,_that.appName,_that.userId,_that.status,_that.createdAt,_that.profile,_that.appIconPath,_that.memoryUsageMb,_that.lastLaunched);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String id,  String packageName,  String appName,  int userId,  CloneStatus status,  DateTime createdAt,  DeviceProfile? profile,  String? appIconPath,  int? memoryUsageMb,  DateTime? lastLaunched)  $default,) {final _that = this;
switch (_that) {
case _CloneInfo():
return $default(_that.id,_that.packageName,_that.appName,_that.userId,_that.status,_that.createdAt,_that.profile,_that.appIconPath,_that.memoryUsageMb,_that.lastLaunched);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String id,  String packageName,  String appName,  int userId,  CloneStatus status,  DateTime createdAt,  DeviceProfile? profile,  String? appIconPath,  int? memoryUsageMb,  DateTime? lastLaunched)?  $default,) {final _that = this;
switch (_that) {
case _CloneInfo() when $default != null:
return $default(_that.id,_that.packageName,_that.appName,_that.userId,_that.status,_that.createdAt,_that.profile,_that.appIconPath,_that.memoryUsageMb,_that.lastLaunched);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _CloneInfo implements CloneInfo {
  const _CloneInfo({required this.id, required this.packageName, required this.appName, required this.userId, required this.status, required this.createdAt, this.profile, this.appIconPath, this.memoryUsageMb, this.lastLaunched});
  factory _CloneInfo.fromJson(Map<String, dynamic> json) => _$CloneInfoFromJson(json);

@override final  String id;
@override final  String packageName;
@override final  String appName;
@override final  int userId;
@override final  CloneStatus status;
@override final  DateTime createdAt;
@override final  DeviceProfile? profile;
@override final  String? appIconPath;
@override final  int? memoryUsageMb;
@override final  DateTime? lastLaunched;

/// Create a copy of CloneInfo
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$CloneInfoCopyWith<_CloneInfo> get copyWith => __$CloneInfoCopyWithImpl<_CloneInfo>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$CloneInfoToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _CloneInfo&&(identical(other.id, id) || other.id == id)&&(identical(other.packageName, packageName) || other.packageName == packageName)&&(identical(other.appName, appName) || other.appName == appName)&&(identical(other.userId, userId) || other.userId == userId)&&(identical(other.status, status) || other.status == status)&&(identical(other.createdAt, createdAt) || other.createdAt == createdAt)&&(identical(other.profile, profile) || other.profile == profile)&&(identical(other.appIconPath, appIconPath) || other.appIconPath == appIconPath)&&(identical(other.memoryUsageMb, memoryUsageMb) || other.memoryUsageMb == memoryUsageMb)&&(identical(other.lastLaunched, lastLaunched) || other.lastLaunched == lastLaunched));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,id,packageName,appName,userId,status,createdAt,profile,appIconPath,memoryUsageMb,lastLaunched);

@override
String toString() {
  return 'CloneInfo(id: $id, packageName: $packageName, appName: $appName, userId: $userId, status: $status, createdAt: $createdAt, profile: $profile, appIconPath: $appIconPath, memoryUsageMb: $memoryUsageMb, lastLaunched: $lastLaunched)';
}


}

/// @nodoc
abstract mixin class _$CloneInfoCopyWith<$Res> implements $CloneInfoCopyWith<$Res> {
  factory _$CloneInfoCopyWith(_CloneInfo value, $Res Function(_CloneInfo) _then) = __$CloneInfoCopyWithImpl;
@override @useResult
$Res call({
 String id, String packageName, String appName, int userId, CloneStatus status, DateTime createdAt, DeviceProfile? profile, String? appIconPath, int? memoryUsageMb, DateTime? lastLaunched
});


@override $DeviceProfileCopyWith<$Res>? get profile;

}
/// @nodoc
class __$CloneInfoCopyWithImpl<$Res>
    implements _$CloneInfoCopyWith<$Res> {
  __$CloneInfoCopyWithImpl(this._self, this._then);

  final _CloneInfo _self;
  final $Res Function(_CloneInfo) _then;

/// Create a copy of CloneInfo
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? packageName = null,Object? appName = null,Object? userId = null,Object? status = null,Object? createdAt = null,Object? profile = freezed,Object? appIconPath = freezed,Object? memoryUsageMb = freezed,Object? lastLaunched = freezed,}) {
  return _then(_CloneInfo(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,packageName: null == packageName ? _self.packageName : packageName // ignore: cast_nullable_to_non_nullable
as String,appName: null == appName ? _self.appName : appName // ignore: cast_nullable_to_non_nullable
as String,userId: null == userId ? _self.userId : userId // ignore: cast_nullable_to_non_nullable
as int,status: null == status ? _self.status : status // ignore: cast_nullable_to_non_nullable
as CloneStatus,createdAt: null == createdAt ? _self.createdAt : createdAt // ignore: cast_nullable_to_non_nullable
as DateTime,profile: freezed == profile ? _self.profile : profile // ignore: cast_nullable_to_non_nullable
as DeviceProfile?,appIconPath: freezed == appIconPath ? _self.appIconPath : appIconPath // ignore: cast_nullable_to_non_nullable
as String?,memoryUsageMb: freezed == memoryUsageMb ? _self.memoryUsageMb : memoryUsageMb // ignore: cast_nullable_to_non_nullable
as int?,lastLaunched: freezed == lastLaunched ? _self.lastLaunched : lastLaunched // ignore: cast_nullable_to_non_nullable
as DateTime?,
  ));
}

/// Create a copy of CloneInfo
/// with the given fields replaced by the non-null parameter values.
@override
@pragma('vm:prefer-inline')
$DeviceProfileCopyWith<$Res>? get profile {
    if (_self.profile == null) {
    return null;
  }

  return $DeviceProfileCopyWith<$Res>(_self.profile!, (value) {
    return _then(_self.copyWith(profile: value));
  });
}
}


/// @nodoc
mixin _$InstalledApp {

 String get packageName; String get appName; String? get iconPath; String? get versionName; int? get versionCode; bool get isSystemApp;
/// Create a copy of InstalledApp
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$InstalledAppCopyWith<InstalledApp> get copyWith => _$InstalledAppCopyWithImpl<InstalledApp>(this as InstalledApp, _$identity);

  /// Serializes this InstalledApp to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is InstalledApp&&(identical(other.packageName, packageName) || other.packageName == packageName)&&(identical(other.appName, appName) || other.appName == appName)&&(identical(other.iconPath, iconPath) || other.iconPath == iconPath)&&(identical(other.versionName, versionName) || other.versionName == versionName)&&(identical(other.versionCode, versionCode) || other.versionCode == versionCode)&&(identical(other.isSystemApp, isSystemApp) || other.isSystemApp == isSystemApp));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,packageName,appName,iconPath,versionName,versionCode,isSystemApp);

@override
String toString() {
  return 'InstalledApp(packageName: $packageName, appName: $appName, iconPath: $iconPath, versionName: $versionName, versionCode: $versionCode, isSystemApp: $isSystemApp)';
}


}

/// @nodoc
abstract mixin class $InstalledAppCopyWith<$Res>  {
  factory $InstalledAppCopyWith(InstalledApp value, $Res Function(InstalledApp) _then) = _$InstalledAppCopyWithImpl;
@useResult
$Res call({
 String packageName, String appName, String? iconPath, String? versionName, int? versionCode, bool isSystemApp
});




}
/// @nodoc
class _$InstalledAppCopyWithImpl<$Res>
    implements $InstalledAppCopyWith<$Res> {
  _$InstalledAppCopyWithImpl(this._self, this._then);

  final InstalledApp _self;
  final $Res Function(InstalledApp) _then;

/// Create a copy of InstalledApp
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? packageName = null,Object? appName = null,Object? iconPath = freezed,Object? versionName = freezed,Object? versionCode = freezed,Object? isSystemApp = null,}) {
  return _then(_self.copyWith(
packageName: null == packageName ? _self.packageName : packageName // ignore: cast_nullable_to_non_nullable
as String,appName: null == appName ? _self.appName : appName // ignore: cast_nullable_to_non_nullable
as String,iconPath: freezed == iconPath ? _self.iconPath : iconPath // ignore: cast_nullable_to_non_nullable
as String?,versionName: freezed == versionName ? _self.versionName : versionName // ignore: cast_nullable_to_non_nullable
as String?,versionCode: freezed == versionCode ? _self.versionCode : versionCode // ignore: cast_nullable_to_non_nullable
as int?,isSystemApp: null == isSystemApp ? _self.isSystemApp : isSystemApp // ignore: cast_nullable_to_non_nullable
as bool,
  ));
}

}


/// Adds pattern-matching-related methods to [InstalledApp].
extension InstalledAppPatterns on InstalledApp {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _InstalledApp value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _InstalledApp() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _InstalledApp value)  $default,){
final _that = this;
switch (_that) {
case _InstalledApp():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _InstalledApp value)?  $default,){
final _that = this;
switch (_that) {
case _InstalledApp() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String packageName,  String appName,  String? iconPath,  String? versionName,  int? versionCode,  bool isSystemApp)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _InstalledApp() when $default != null:
return $default(_that.packageName,_that.appName,_that.iconPath,_that.versionName,_that.versionCode,_that.isSystemApp);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String packageName,  String appName,  String? iconPath,  String? versionName,  int? versionCode,  bool isSystemApp)  $default,) {final _that = this;
switch (_that) {
case _InstalledApp():
return $default(_that.packageName,_that.appName,_that.iconPath,_that.versionName,_that.versionCode,_that.isSystemApp);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String packageName,  String appName,  String? iconPath,  String? versionName,  int? versionCode,  bool isSystemApp)?  $default,) {final _that = this;
switch (_that) {
case _InstalledApp() when $default != null:
return $default(_that.packageName,_that.appName,_that.iconPath,_that.versionName,_that.versionCode,_that.isSystemApp);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _InstalledApp implements InstalledApp {
  const _InstalledApp({required this.packageName, required this.appName, this.iconPath, this.versionName, this.versionCode, this.isSystemApp = false});
  factory _InstalledApp.fromJson(Map<String, dynamic> json) => _$InstalledAppFromJson(json);

@override final  String packageName;
@override final  String appName;
@override final  String? iconPath;
@override final  String? versionName;
@override final  int? versionCode;
@override@JsonKey() final  bool isSystemApp;

/// Create a copy of InstalledApp
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$InstalledAppCopyWith<_InstalledApp> get copyWith => __$InstalledAppCopyWithImpl<_InstalledApp>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$InstalledAppToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _InstalledApp&&(identical(other.packageName, packageName) || other.packageName == packageName)&&(identical(other.appName, appName) || other.appName == appName)&&(identical(other.iconPath, iconPath) || other.iconPath == iconPath)&&(identical(other.versionName, versionName) || other.versionName == versionName)&&(identical(other.versionCode, versionCode) || other.versionCode == versionCode)&&(identical(other.isSystemApp, isSystemApp) || other.isSystemApp == isSystemApp));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,packageName,appName,iconPath,versionName,versionCode,isSystemApp);

@override
String toString() {
  return 'InstalledApp(packageName: $packageName, appName: $appName, iconPath: $iconPath, versionName: $versionName, versionCode: $versionCode, isSystemApp: $isSystemApp)';
}


}

/// @nodoc
abstract mixin class _$InstalledAppCopyWith<$Res> implements $InstalledAppCopyWith<$Res> {
  factory _$InstalledAppCopyWith(_InstalledApp value, $Res Function(_InstalledApp) _then) = __$InstalledAppCopyWithImpl;
@override @useResult
$Res call({
 String packageName, String appName, String? iconPath, String? versionName, int? versionCode, bool isSystemApp
});




}
/// @nodoc
class __$InstalledAppCopyWithImpl<$Res>
    implements _$InstalledAppCopyWith<$Res> {
  __$InstalledAppCopyWithImpl(this._self, this._then);

  final _InstalledApp _self;
  final $Res Function(_InstalledApp) _then;

/// Create a copy of InstalledApp
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? packageName = null,Object? appName = null,Object? iconPath = freezed,Object? versionName = freezed,Object? versionCode = freezed,Object? isSystemApp = null,}) {
  return _then(_InstalledApp(
packageName: null == packageName ? _self.packageName : packageName // ignore: cast_nullable_to_non_nullable
as String,appName: null == appName ? _self.appName : appName // ignore: cast_nullable_to_non_nullable
as String,iconPath: freezed == iconPath ? _self.iconPath : iconPath // ignore: cast_nullable_to_non_nullable
as String?,versionName: freezed == versionName ? _self.versionName : versionName // ignore: cast_nullable_to_non_nullable
as String?,versionCode: freezed == versionCode ? _self.versionCode : versionCode // ignore: cast_nullable_to_non_nullable
as int?,isSystemApp: null == isSystemApp ? _self.isSystemApp : isSystemApp // ignore: cast_nullable_to_non_nullable
as bool,
  ));
}


}


/// @nodoc
mixin _$CloneEvent {

 String get cloneId; String get eventType; String? get message; Map<String, dynamic>? get data;
/// Create a copy of CloneEvent
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$CloneEventCopyWith<CloneEvent> get copyWith => _$CloneEventCopyWithImpl<CloneEvent>(this as CloneEvent, _$identity);

  /// Serializes this CloneEvent to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is CloneEvent&&(identical(other.cloneId, cloneId) || other.cloneId == cloneId)&&(identical(other.eventType, eventType) || other.eventType == eventType)&&(identical(other.message, message) || other.message == message)&&const DeepCollectionEquality().equals(other.data, data));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,cloneId,eventType,message,const DeepCollectionEquality().hash(data));

@override
String toString() {
  return 'CloneEvent(cloneId: $cloneId, eventType: $eventType, message: $message, data: $data)';
}


}

/// @nodoc
abstract mixin class $CloneEventCopyWith<$Res>  {
  factory $CloneEventCopyWith(CloneEvent value, $Res Function(CloneEvent) _then) = _$CloneEventCopyWithImpl;
@useResult
$Res call({
 String cloneId, String eventType, String? message, Map<String, dynamic>? data
});




}
/// @nodoc
class _$CloneEventCopyWithImpl<$Res>
    implements $CloneEventCopyWith<$Res> {
  _$CloneEventCopyWithImpl(this._self, this._then);

  final CloneEvent _self;
  final $Res Function(CloneEvent) _then;

/// Create a copy of CloneEvent
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? cloneId = null,Object? eventType = null,Object? message = freezed,Object? data = freezed,}) {
  return _then(_self.copyWith(
cloneId: null == cloneId ? _self.cloneId : cloneId // ignore: cast_nullable_to_non_nullable
as String,eventType: null == eventType ? _self.eventType : eventType // ignore: cast_nullable_to_non_nullable
as String,message: freezed == message ? _self.message : message // ignore: cast_nullable_to_non_nullable
as String?,data: freezed == data ? _self.data : data // ignore: cast_nullable_to_non_nullable
as Map<String, dynamic>?,
  ));
}

}


/// Adds pattern-matching-related methods to [CloneEvent].
extension CloneEventPatterns on CloneEvent {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _CloneEvent value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _CloneEvent() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _CloneEvent value)  $default,){
final _that = this;
switch (_that) {
case _CloneEvent():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _CloneEvent value)?  $default,){
final _that = this;
switch (_that) {
case _CloneEvent() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String cloneId,  String eventType,  String? message,  Map<String, dynamic>? data)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _CloneEvent() when $default != null:
return $default(_that.cloneId,_that.eventType,_that.message,_that.data);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String cloneId,  String eventType,  String? message,  Map<String, dynamic>? data)  $default,) {final _that = this;
switch (_that) {
case _CloneEvent():
return $default(_that.cloneId,_that.eventType,_that.message,_that.data);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String cloneId,  String eventType,  String? message,  Map<String, dynamic>? data)?  $default,) {final _that = this;
switch (_that) {
case _CloneEvent() when $default != null:
return $default(_that.cloneId,_that.eventType,_that.message,_that.data);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _CloneEvent implements CloneEvent {
  const _CloneEvent({required this.cloneId, required this.eventType, this.message, final  Map<String, dynamic>? data}): _data = data;
  factory _CloneEvent.fromJson(Map<String, dynamic> json) => _$CloneEventFromJson(json);

@override final  String cloneId;
@override final  String eventType;
@override final  String? message;
 final  Map<String, dynamic>? _data;
@override Map<String, dynamic>? get data {
  final value = _data;
  if (value == null) return null;
  if (_data is EqualUnmodifiableMapView) return _data;
  // ignore: implicit_dynamic_type
  return EqualUnmodifiableMapView(value);
}


/// Create a copy of CloneEvent
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$CloneEventCopyWith<_CloneEvent> get copyWith => __$CloneEventCopyWithImpl<_CloneEvent>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$CloneEventToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _CloneEvent&&(identical(other.cloneId, cloneId) || other.cloneId == cloneId)&&(identical(other.eventType, eventType) || other.eventType == eventType)&&(identical(other.message, message) || other.message == message)&&const DeepCollectionEquality().equals(other._data, _data));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,cloneId,eventType,message,const DeepCollectionEquality().hash(_data));

@override
String toString() {
  return 'CloneEvent(cloneId: $cloneId, eventType: $eventType, message: $message, data: $data)';
}


}

/// @nodoc
abstract mixin class _$CloneEventCopyWith<$Res> implements $CloneEventCopyWith<$Res> {
  factory _$CloneEventCopyWith(_CloneEvent value, $Res Function(_CloneEvent) _then) = __$CloneEventCopyWithImpl;
@override @useResult
$Res call({
 String cloneId, String eventType, String? message, Map<String, dynamic>? data
});




}
/// @nodoc
class __$CloneEventCopyWithImpl<$Res>
    implements _$CloneEventCopyWith<$Res> {
  __$CloneEventCopyWithImpl(this._self, this._then);

  final _CloneEvent _self;
  final $Res Function(_CloneEvent) _then;

/// Create a copy of CloneEvent
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? cloneId = null,Object? eventType = null,Object? message = freezed,Object? data = freezed,}) {
  return _then(_CloneEvent(
cloneId: null == cloneId ? _self.cloneId : cloneId // ignore: cast_nullable_to_non_nullable
as String,eventType: null == eventType ? _self.eventType : eventType // ignore: cast_nullable_to_non_nullable
as String,message: freezed == message ? _self.message : message // ignore: cast_nullable_to_non_nullable
as String?,data: freezed == data ? _self._data : data // ignore: cast_nullable_to_non_nullable
as Map<String, dynamic>?,
  ));
}


}

// dart format on

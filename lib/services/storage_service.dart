import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../core/constants/app_constants.dart';

final storageServiceProvider = Provider<StorageService>((ref) {
  return StorageService();
});

class StorageService {
  SharedPreferences? _prefs;

  Future<SharedPreferences> get _preferences async {
    return _prefs ??= await SharedPreferences.getInstance();
  }

  Future<String?> getString(String key) async {
    final prefs = await _preferences;
    return prefs.getString(key);
  }

  Future<bool> setString(String key, String value) async {
    final prefs = await _preferences;
    return prefs.setString(key, value);
  }

  Future<bool?> getBool(String key) async {
    final prefs = await _preferences;
    return prefs.getBool(key);
  }

  Future<bool> setBool(String key, bool value) async {
    final prefs = await _preferences;
    return prefs.setBool(key, value);
  }

  Future<int?> getInt(String key) async {
    final prefs = await _preferences;
    return prefs.getInt(key);
  }

  Future<bool> setInt(String key, int value) async {
    final prefs = await _preferences;
    return prefs.setInt(key, value);
  }

  Future<bool> get isOnboardingCompleted async {
    final result = await getBool(StorageKeys.onboardingCompleted);
    return result ?? false;
  }

  Future<void> setOnboardingCompleted() async {
    await setBool(StorageKeys.onboardingCompleted, true);
  }

  Future<String?> get lastActiveClone async {
    return getString(StorageKeys.lastActiveClone);
  }

  Future<void> setLastActiveClone(String cloneId) async {
    await setString(StorageKeys.lastActiveClone, cloneId);
  }
}

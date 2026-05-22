import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants/app_constants.dart';
import '../../../services/storage_service.dart';

final themeModeProvider =
    StateNotifierProvider<ThemeModeNotifier, ThemeMode>((ref) {
  return ThemeModeNotifier(ref);
});

class ThemeModeNotifier extends StateNotifier<ThemeMode> {
  ThemeModeNotifier(this._ref) : super(ThemeMode.system) {
    _load();
  }

  final Ref _ref;

  Future<void> _load() async {
    final storage = _ref.read(storageServiceProvider);
    final value = await storage.getString(StorageKeys.themeMode);
    if (value != null) {
      state = ThemeMode.values.firstWhere(
        (m) => m.name == value,
        orElse: () => ThemeMode.system,
      );
    }
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    state = mode;
    final storage = _ref.read(storageServiceProvider);
    await storage.setString(StorageKeys.themeMode, mode.name);
  }
}

final proxyEnabledProvider =
    StateNotifierProvider<ProxyEnabledNotifier, bool>((ref) {
  return ProxyEnabledNotifier(ref);
});

class ProxyEnabledNotifier extends StateNotifier<bool> {
  ProxyEnabledNotifier(this._ref) : super(false) {
    _load();
  }

  final Ref _ref;

  Future<void> _load() async {
    final storage = _ref.read(storageServiceProvider);
    final value = await storage.getBool(StorageKeys.proxyEnabled);
    state = value ?? false;
  }

  Future<void> toggle() async {
    state = !state;
    final storage = _ref.read(storageServiceProvider);
    await storage.setBool(StorageKeys.proxyEnabled, state);
  }
}

final maxConcurrentClonesProvider =
    StateNotifierProvider<MaxConcurrentClonesNotifier, int>((ref) {
  return MaxConcurrentClonesNotifier(ref);
});

class MaxConcurrentClonesNotifier extends StateNotifier<int> {
  MaxConcurrentClonesNotifier(this._ref)
      : super(AppConstants.defaultMaxConcurrentClones) {
    _load();
  }

  final Ref _ref;

  Future<void> _load() async {
    final storage = _ref.read(storageServiceProvider);
    final value = await storage.getInt(StorageKeys.maxConcurrentClones);
    state = value ?? AppConstants.defaultMaxConcurrentClones;
  }

  Future<void> set(int value) async {
    state = value;
    final storage = _ref.read(storageServiceProvider);
    await storage.setInt(StorageKeys.maxConcurrentClones, value);
  }
}

final memoryLimitProvider =
    StateNotifierProvider<MemoryLimitNotifier, int>((ref) {
  return MemoryLimitNotifier(ref);
});

class MemoryLimitNotifier extends StateNotifier<int> {
  MemoryLimitNotifier(this._ref)
      : super(AppConstants.defaultMemoryLimitMb) {
    _load();
  }

  final Ref _ref;

  Future<void> _load() async {
    final storage = _ref.read(storageServiceProvider);
    final value = await storage.getInt(StorageKeys.memoryLimitMb);
    state = value ?? AppConstants.defaultMemoryLimitMb;
  }

  Future<void> set(int value) async {
    state = value;
    final storage = _ref.read(storageServiceProvider);
    await storage.setInt(StorageKeys.memoryLimitMb, value);
  }
}

final autoStartProvider =
    StateNotifierProvider<AutoStartNotifier, bool>((ref) {
  return AutoStartNotifier(ref);
});

class AutoStartNotifier extends StateNotifier<bool> {
  AutoStartNotifier(this._ref) : super(false) {
    _load();
  }

  final Ref _ref;

  Future<void> _load() async {
    final storage = _ref.read(storageServiceProvider);
    final value = await storage.getBool(StorageKeys.autoStartEnabled);
    state = value ?? false;
  }

  Future<void> toggle() async {
    state = !state;
    final storage = _ref.read(storageServiceProvider);
    await storage.setBool(StorageKeys.autoStartEnabled, state);
  }
}

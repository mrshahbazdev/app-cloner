import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

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
    final value = await storage.getString('theme_mode');
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
    await storage.setString('theme_mode', mode.name);
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
    final value = await storage.getBool('proxy_enabled');
    state = value ?? false;
  }

  Future<void> toggle() async {
    state = !state;
    final storage = _ref.read(storageServiceProvider);
    await storage.setBool('proxy_enabled', state);
  }
}

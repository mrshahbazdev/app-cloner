import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../models/clone_info.dart';
import '../../../services/clone_engine_service.dart';

final appPickerProvider =
    AsyncNotifierProvider<AppPickerNotifier, List<InstalledApp>>(
  AppPickerNotifier.new,
);

final appSearchQueryProvider = StateProvider<String>((ref) => '');

final filteredAppsProvider = Provider<AsyncValue<List<InstalledApp>>>((ref) {
  final query = ref.watch(appSearchQueryProvider).toLowerCase();
  final appsAsync = ref.watch(appPickerProvider);

  return appsAsync.whenData((apps) {
    if (query.isEmpty) return apps;
    return apps
        .where((app) =>
            app.appName.toLowerCase().contains(query) ||
            app.packageName.toLowerCase().contains(query))
        .toList();
  });
});

class AppPickerNotifier extends AsyncNotifier<List<InstalledApp>> {
  @override
  Future<List<InstalledApp>> build() async {
    final engine = ref.watch(cloneEngineServiceProvider);
    final apps = await engine.getInstalledApps();
    apps.sort((a, b) => a.appName.compareTo(b.appName));
    return apps;
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final engine = ref.read(cloneEngineServiceProvider);
      final apps = await engine.getInstalledApps();
      apps.sort((a, b) => a.appName.compareTo(b.appName));
      return apps;
    });
  }
}

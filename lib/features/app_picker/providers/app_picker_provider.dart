import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants/app_constants.dart';
import '../../../models/clone_info.dart';
import '../../../services/clone_engine_service.dart';

final appPickerProvider =
    AsyncNotifierProvider<AppPickerNotifier, List<InstalledApp>>(
  AppPickerNotifier.new,
);

final appSearchQueryProvider = StateProvider<String>((ref) => '');

final appCategoryFilterProvider = StateProvider<String>((ref) => 'All');

final filteredAppsProvider = Provider<AsyncValue<List<InstalledApp>>>((ref) {
  final query = ref.watch(appSearchQueryProvider).toLowerCase();
  final category = ref.watch(appCategoryFilterProvider);
  final appsAsync = ref.watch(appPickerProvider);

  return appsAsync.whenData((apps) {
    var filtered = apps;

    if (category != 'All') {
      filtered = filtered
          .where((app) =>
              (app.category ?? 'other').toLowerCase() ==
              category.toLowerCase())
          .toList();
    }

    if (query.isNotEmpty) {
      filtered = filtered
          .where((app) =>
              app.appName.toLowerCase().contains(query) ||
              app.packageName.toLowerCase().contains(query))
          .toList();
    }

    return filtered;
  });
});

final appCategoriesProvider = Provider<List<String>>((ref) {
  return AppConstants.appCategories;
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

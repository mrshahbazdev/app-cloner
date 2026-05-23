import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/app_picker/presentation/screens/app_picker_screen.dart';
import '../../features/clone_detail/presentation/screens/clone_detail_screen.dart';
import '../../features/dashboard/presentation/screens/dashboard_screen.dart';
import '../../features/profile_editor/presentation/screens/profile_editor_screen.dart';
import '../../features/settings/presentation/screens/settings_screen.dart';
import '../../features/splash/presentation/screens/splash_screen.dart';

abstract final class AppRoutes {
  static const String splash = '/';
  static const String dashboard = '/dashboard';
  static const String appPicker = '/app-picker';
  static const String cloneDetail = '/clone/:id';
  static const String profileEditor = '/clone/:id/profile';
  static const String settings = '/settings';
}

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: AppRoutes.splash,
    routes: [
      GoRoute(
        path: AppRoutes.splash,
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: AppRoutes.dashboard,
        builder: (context, state) => const DashboardScreen(),
      ),
      GoRoute(
        path: AppRoutes.appPicker,
        builder: (context, state) => const AppPickerScreen(),
      ),
      GoRoute(
        path: AppRoutes.cloneDetail,
        builder: (context, state) {
          final cloneId = state.pathParameters['id']!;
          return CloneDetailScreen(cloneId: cloneId);
        },
        routes: [
          GoRoute(
            path: 'profile',
            builder: (context, state) {
              final cloneId = state.pathParameters['id']!;
              return ProfileEditorScreen(cloneId: cloneId);
            },
          ),
        ],
      ),
      GoRoute(
        path: AppRoutes.settings,
        builder: (context, state) => const SettingsScreen(),
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Text('Route not found: ${state.uri}'),
      ),
    ),
  );
});

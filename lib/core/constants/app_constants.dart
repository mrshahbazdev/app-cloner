abstract final class AppConstants {
  static const String appName = 'TitanClone';
  static const String packageName = 'com.titanclone.titan_clone';
  static const int maxClones = 12;
  static const int defaultMaxConcurrentClones = 5;
  static const int defaultMemoryLimitMb = 256;

  static const String bridgeChannelName = 'com.titanclone/bridge';
  static const String eventChannelName = 'com.titanclone/events';

  static const Duration cloneLaunchTimeout = Duration(seconds: 30);
  static const Duration profileSwitchTimeout = Duration(milliseconds: 500);
  static const Duration engineInitTimeout = Duration(seconds: 5);

  static const List<String> appCategories = [
    'All',
    'Social',
    'Games',
    'Productivity',
    'Media',
    'Tools',
    'Other',
  ];
}

abstract final class StorageKeys {
  static const String themeMode = 'theme_mode';
  static const String onboardingCompleted = 'onboarding_completed';
  static const String lastActiveClone = 'last_active_clone';
  static const String proxyEnabled = 'proxy_enabled';
  static const String maxConcurrentClones = 'max_concurrent_clones';
  static const String memoryLimitMb = 'memory_limit_mb';
  static const String autoStartEnabled = 'auto_start_enabled';
  static const String backgroundProcessPolicy = 'background_process_policy';
}

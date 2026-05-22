import 'dart:developer' as developer;

abstract final class AppLogger {
  static void info(String message, {String tag = 'TitanClone'}) {
    developer.log(message, name: tag, level: 800);
  }

  static void warning(String message, {String tag = 'TitanClone'}) {
    developer.log(message, name: tag, level: 900);
  }

  static void error(
    String message, {
    String tag = 'TitanClone',
    Object? error,
    StackTrace? stackTrace,
  }) {
    developer.log(
      message,
      name: tag,
      level: 1000,
      error: error,
      stackTrace: stackTrace,
    );
  }
}

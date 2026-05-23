import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../services/clone_engine_service.dart';

final memorySnapshotProvider = FutureProvider<MemorySnapshot>((ref) async {
  final engine = ref.watch(cloneEngineServiceProvider);
  return engine.getMemorySnapshot();
});

final securityStatusProvider = FutureProvider<SecurityStatus>((ref) async {
  final engine = ref.watch(cloneEngineServiceProvider);
  return engine.performSecurityCheck();
});

final performanceMetricsProvider =
    FutureProvider<PerformanceMetrics>((ref) async {
  final engine = ref.watch(cloneEngineServiceProvider);
  return engine.getPerformanceMetrics();
});

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../services/clone_engine_service.dart';

final gmsStateProvider = FutureProvider<GmsState>((ref) async {
  final engine = ref.watch(cloneEngineServiceProvider);
  return engine.getGmsState();
});

final compatReportProvider = FutureProvider<CompatReport>((ref) async {
  final engine = ref.watch(cloneEngineServiceProvider);
  return engine.checkCompatibility();
});

final batteryInfoProvider = FutureProvider<BatteryInfo>((ref) async {
  final engine = ref.watch(cloneEngineServiceProvider);
  return engine.getBatteryOptimizationInfo();
});

import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../models/clone_info.dart';
import '../../../models/clone_status.dart';
import '../../../services/clone_engine_service.dart';
import '../../../services/clone_event_stream.dart';

final dashboardProvider =
    AsyncNotifierProvider<DashboardNotifier, List<CloneInfo>>(
  DashboardNotifier.new,
);

class DashboardNotifier extends AsyncNotifier<List<CloneInfo>> {
  StreamSubscription<CloneEvent>? _eventSubscription;

  @override
  Future<List<CloneInfo>> build() async {
    final engine = ref.watch(cloneEngineServiceProvider);
    await engine.initializeEngine();

    _eventSubscription?.cancel();
    _eventSubscription =
        ref.watch(cloneEventStreamProvider).events.listen(_onCloneEvent);

    ref.onDispose(() => _eventSubscription?.cancel());

    return engine.getClones();
  }

  void _onCloneEvent(CloneEvent event) {
    final currentClones = state.valueOrNull ?? [];
    final index = currentClones.indexWhere((c) => c.id == event.cloneId);
    if (index == -1) return;

    final updatedClone = currentClones[index].copyWith(
      status: CloneStatus.values.firstWhere(
        (s) => s.name == event.eventType,
        orElse: () => currentClones[index].status,
      ),
    );

    final updated = [...currentClones];
    updated[index] = updatedClone;
    state = AsyncData(updated);
  }

  Future<void> refreshClones() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() {
      final engine = ref.read(cloneEngineServiceProvider);
      return engine.getClones();
    });
  }

  Future<bool> launchClone(String cloneId) async {
    final engine = ref.read(cloneEngineServiceProvider);
    final success = await engine.launchClone(cloneId);
    if (success) await refreshClones();
    return success;
  }

  Future<bool> stopClone(String cloneId) async {
    final engine = ref.read(cloneEngineServiceProvider);
    final success = await engine.stopClone(cloneId);
    if (success) await refreshClones();
    return success;
  }

  Future<bool> deleteClone(String cloneId) async {
    final engine = ref.read(cloneEngineServiceProvider);
    final success = await engine.deleteClone(cloneId);
    if (success) await refreshClones();
    return success;
  }
}

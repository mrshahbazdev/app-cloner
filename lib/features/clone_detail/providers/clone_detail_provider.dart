import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../models/clone_info.dart';
import '../../../models/clone_status.dart';
import '../../../models/device_profile.dart';
import '../../../services/clone_engine_service.dart';

final cloneDetailProvider =
    AsyncNotifierProvider.family<CloneDetailNotifier, CloneInfo?, String>(
  CloneDetailNotifier.new,
);

class CloneDetailNotifier extends FamilyAsyncNotifier<CloneInfo?, String> {
  @override
  Future<CloneInfo?> build(String arg) async {
    final engine = ref.watch(cloneEngineServiceProvider);
    final clones = await engine.getClones();
    return clones.cast<CloneInfo?>().firstWhere(
          (c) => c?.id == arg,
          orElse: () => null,
        );
  }

  Future<bool> launch() async {
    final engine = ref.read(cloneEngineServiceProvider);
    final success = await engine.launchClone(arg);
    if (success) {
      state = AsyncData(state.valueOrNull?.copyWith(
        status: CloneStatus.running,
        lastLaunched: DateTime.now(),
      ));
    }
    return success;
  }

  Future<bool> stop() async {
    final engine = ref.read(cloneEngineServiceProvider);
    final success = await engine.stopClone(arg);
    if (success) {
      state = AsyncData(state.valueOrNull?.copyWith(
        status: CloneStatus.stopped,
      ));
    }
    return success;
  }

  Future<bool> delete() async {
    final engine = ref.read(cloneEngineServiceProvider);
    return engine.deleteClone(arg);
  }

  Future<bool> updateProfile(DeviceProfile profile) async {
    final engine = ref.read(cloneEngineServiceProvider);
    final success = await engine.updateCloneProfile(arg, profile);
    if (success) {
      state = AsyncData(state.valueOrNull?.copyWith(profile: profile));
    }
    return success;
  }
}

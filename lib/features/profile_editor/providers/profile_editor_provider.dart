import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../models/device_profile.dart';
import '../../../services/clone_engine_service.dart';

final profileEditorProvider =
    AsyncNotifierProvider.family<ProfileEditorNotifier, DeviceProfile?, String>(
  ProfileEditorNotifier.new,
);

class ProfileEditorNotifier extends FamilyAsyncNotifier<DeviceProfile?, String> {
  @override
  Future<DeviceProfile?> build(String arg) async {
    final engine = ref.watch(cloneEngineServiceProvider);
    return engine.getCloneProfile(arg);
  }

  Future<bool> saveProfile(DeviceProfile profile) async {
    final engine = ref.read(cloneEngineServiceProvider);
    final success = await engine.updateCloneProfile(arg, profile);
    if (success) {
      state = AsyncData(profile);
    }
    return success;
  }
}

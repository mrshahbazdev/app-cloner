import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../services/clone_engine_service.dart';

final engineInitProvider = FutureProvider<bool>((ref) async {
  final engine = ref.watch(cloneEngineServiceProvider);
  return engine.initializeEngine();
});

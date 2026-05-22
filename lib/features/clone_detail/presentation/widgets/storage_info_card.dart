import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../services/clone_engine_service.dart';

final cloneStorageProvider =
    FutureProvider.family<StorageInfo, String>((ref, cloneId) async {
  final engine = ref.watch(cloneEngineServiceProvider);
  return engine.getCloneStorageInfo(cloneId);
});

class StorageInfoCard extends ConsumerWidget {
  const StorageInfoCard({super.key, required this.cloneId});

  final String cloneId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final storageAsync = ref.watch(cloneStorageProvider(cloneId));

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.storage,
                    size: 20, color: context.colorScheme.primary),
                const SizedBox(width: 8),
                Text(
                  'Storage',
                  style: context.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const Divider(),
            storageAsync.when(
              loading: () => const Center(
                child: Padding(
                  padding: EdgeInsets.all(8),
                  child: SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
                ),
              ),
              error: (_, __) => const Text('Unable to load storage info'),
              data: (info) => Column(
                children: [
                  _storageRow(context, 'Total', info.formattedTotal),
                  _storageRow(context, 'App Data', info.formattedData),
                  _storageRow(context, 'Cache', info.formattedCache),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _storageRow(BuildContext context, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label,
              style: context.textTheme.bodySmall?.copyWith(
                color: context.colorScheme.outline,
              )),
          Text(value, style: context.textTheme.bodyMedium),
        ],
      ),
    );
  }
}

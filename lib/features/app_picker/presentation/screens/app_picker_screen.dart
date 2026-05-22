import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../models/clone_info.dart';
import '../../../../services/clone_engine_service.dart';
import '../../providers/app_picker_provider.dart';
import '../widgets/app_list_tile.dart';

class AppPickerScreen extends ConsumerWidget {
  const AppPickerScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final filteredApps = ref.watch(filteredAppsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Select App to Clone'),
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(56),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: TextField(
              onChanged: (value) =>
                  ref.read(appSearchQueryProvider.notifier).state = value,
              decoration: InputDecoration(
                hintText: 'Search apps...',
                prefixIcon: const Icon(Icons.search),
                filled: true,
                fillColor: context.colorScheme.surfaceContainerHighest,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(28),
                  borderSide: BorderSide.none,
                ),
                contentPadding: const EdgeInsets.symmetric(horizontal: 16),
              ),
            ),
          ),
        ),
      ),
      body: filteredApps.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.error_outline, size: 48),
              const SizedBox(height: 16),
              Text('Failed to load apps: $error'),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () =>
                    ref.read(appPickerProvider.notifier).refresh(),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
        data: (apps) {
          if (apps.isEmpty) {
            return const Center(child: Text('No apps found'));
          }

          return ListView.builder(
            padding: const EdgeInsets.symmetric(vertical: 8),
            itemCount: apps.length,
            itemBuilder: (context, index) {
              return AppListTile(
                app: apps[index],
                onTap: () => _createClone(context, ref, apps[index]),
              );
            },
          );
        },
      ),
    );
  }

  Future<void> _createClone(
    BuildContext context,
    WidgetRef ref,
    InstalledApp app,
  ) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Create Clone'),
        content: Text('Clone "${app.appName}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Clone'),
          ),
        ],
      ),
    );

    if (confirmed != true || !context.mounted) return;

    final engine = ref.read(cloneEngineServiceProvider);
    final clone = await engine.createClone(
      packageName: app.packageName,
      userId: 0,
    );

    if (!context.mounted) return;

    if (clone != null) {
      context.showSnackBar('Clone created: ${app.appName}');
      context.pop();
    } else {
      context.showSnackBar('Failed to create clone', isError: true);
    }
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../core/router/app_router.dart';
import '../../providers/dashboard_provider.dart';
import '../widgets/clone_card.dart';
import '../widgets/empty_state.dart';

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final clonesAsync = ref.watch(dashboardProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('TitanClone'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () => context.push(AppRoutes.settings),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () =>
            ref.read(dashboardProvider.notifier).refreshClones(),
        child: clonesAsync.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (error, stack) => Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.error_outline,
                    size: 48, color: context.colorScheme.error),
                const SizedBox(height: 16),
                Text('Failed to load clones',
                    style: context.textTheme.titleMedium),
                const SizedBox(height: 8),
                Text(error.toString(),
                    style: context.textTheme.bodySmall),
                const SizedBox(height: 16),
                FilledButton(
                  onPressed: () =>
                      ref.read(dashboardProvider.notifier).refreshClones(),
                  child: const Text('Retry'),
                ),
              ],
            ),
          ),
          data: (clones) {
            if (clones.isEmpty) return const EmptyState();

            return ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: clones.length,
              itemBuilder: (context, index) {
                final clone = clones[index];
                return CloneCard(
                  clone: clone,
                  onTap: () => context.push('/clone/${clone.id}'),
                  onLaunch: () => ref
                      .read(dashboardProvider.notifier)
                      .launchClone(clone.id),
                  onStop: () => ref
                      .read(dashboardProvider.notifier)
                      .stopClone(clone.id),
                );
              },
            );
          },
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push(AppRoutes.appPicker),
        icon: const Icon(Icons.add),
        label: const Text('New Clone'),
      ),
    );
  }
}

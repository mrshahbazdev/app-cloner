import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../core/router/app_router.dart';
import '../../../../models/clone_status.dart';
import '../../providers/dashboard_provider.dart';
import '../widgets/add_clone_card.dart';
import '../widgets/clone_card.dart';
import '../widgets/empty_state.dart';
import '../widgets/running_clones_bar.dart';

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final clonesAsync = ref.watch(dashboardProvider);
    final isWide = context.screenSize.width > 600;

    return Scaffold(
      appBar: AppBar(
        title: const Text('TitanClone'),
        actions: [
          clonesAsync.whenOrNull(
                data: (clones) {
                  final runningCount =
                      clones.where((c) => c.status == CloneStatus.running).length;
                  if (runningCount == 0) return null;
                  return Padding(
                    padding: const EdgeInsets.only(right: 4),
                    child: Badge(
                      label: Text('$runningCount'),
                      child: const Icon(Icons.play_circle_outline),
                    ),
                  );
                },
              ) ??
              const SizedBox.shrink(),
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

            final runningClones =
                clones.where((c) => c.status == CloneStatus.running).toList();

            return CustomScrollView(
              slivers: [
                if (runningClones.isNotEmpty)
                  SliverToBoxAdapter(
                    child: RunningClonesBar(
                      runningCount: runningClones.length,
                      totalCount: clones.length,
                    ),
                  ),
                SliverPadding(
                  padding: const EdgeInsets.all(16),
                  sliver: SliverGrid(
                    gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: isWide ? 3 : 2,
                      mainAxisSpacing: 12,
                      crossAxisSpacing: 12,
                      childAspectRatio: isWide ? 1.1 : 0.9,
                    ),
                    delegate: SliverChildBuilderDelegate(
                      (context, index) {
                        if (index == clones.length) {
                          return AddCloneCard(
                            onTap: () => context.push(AppRoutes.appPicker),
                          );
                        }

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
                      childCount: clones.length + 1,
                    ),
                  ),
                ),
              ],
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

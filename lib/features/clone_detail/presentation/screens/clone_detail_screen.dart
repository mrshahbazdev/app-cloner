import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../../models/clone_status.dart';
import '../../providers/clone_detail_provider.dart';
import '../widgets/profile_summary_card.dart';
import '../widgets/clone_actions.dart';

class CloneDetailScreen extends ConsumerWidget {
  const CloneDetailScreen({super.key, required this.cloneId});

  final String cloneId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cloneAsync = ref.watch(cloneDetailProvider(cloneId));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Clone Details'),
        actions: [
          IconButton(
            icon: const Icon(Icons.delete_outline),
            onPressed: () => _confirmDelete(context, ref),
          ),
        ],
      ),
      body: cloneAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(child: Text('Error: $error')),
        data: (clone) {
          if (clone == null) {
            return const Center(child: Text('Clone not found'));
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildHeader(context, clone.appName, clone.status),
                const SizedBox(height: 24),
                CloneActions(
                  status: clone.status,
                  onLaunch: () => ref
                      .read(cloneDetailProvider(cloneId).notifier)
                      .launch(),
                  onStop: () => ref
                      .read(cloneDetailProvider(cloneId).notifier)
                      .stop(),
                ),
                const SizedBox(height: 24),
                if (clone.profile != null) ...[
                  ProfileSummaryCard(
                    profile: clone.profile!,
                    onEdit: () => context.push('/clone/$cloneId/profile'),
                  ),
                  const SizedBox(height: 16),
                ],
                _buildInfoSection(context, clone),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildHeader(
      BuildContext context, String appName, CloneStatus status) {
    return Row(
      children: [
        Container(
          width: 64,
          height: 64,
          decoration: BoxDecoration(
            color: context.colorScheme.primaryContainer,
            borderRadius: BorderRadius.circular(16),
          ),
          child: Icon(Icons.apps, size: 32, color: context.colorScheme.primary),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(appName, style: context.textTheme.headlineSmall),
              const SizedBox(height: 4),
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: _statusColor(status).withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  status.label,
                  style: context.textTheme.labelSmall?.copyWith(
                    color: _statusColor(status),
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildInfoSection(BuildContext context, dynamic clone) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Information',
                style: context.textTheme.titleMedium
                    ?.copyWith(fontWeight: FontWeight.w600)),
            const Divider(),
            _infoRow(context, 'Package', clone.packageName),
            _infoRow(context, 'User ID', clone.userId.toString()),
            _infoRow(context, 'Created', clone.createdAt.toString()),
            if (clone.lastLaunched != null)
              _infoRow(
                  context, 'Last Launched', clone.lastLaunched.toString()),
            if (clone.memoryUsageMb != null)
              _infoRow(
                  context, 'Memory', '${clone.memoryUsageMb} MB'),
          ],
        ),
      ),
    );
  }

  Widget _infoRow(BuildContext context, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: context.textTheme.bodyMedium?.copyWith(
            color: context.colorScheme.outline,
          )),
          Flexible(
            child: Text(value, style: context.textTheme.bodyMedium,
                textAlign: TextAlign.end),
          ),
        ],
      ),
    );
  }

  Color _statusColor(CloneStatus status) {
    return switch (status) {
      CloneStatus.running => AppColors.cloneRunning,
      CloneStatus.installing => AppColors.cloneInstalling,
      CloneStatus.error => AppColors.cloneError,
      _ => AppColors.cloneStopped,
    };
  }

  Future<void> _confirmDelete(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Clone'),
        content: const Text(
            'This will permanently delete the clone and all its data.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            style: FilledButton.styleFrom(
              backgroundColor: context.colorScheme.error,
            ),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirmed != true || !context.mounted) return;

    final success =
        await ref.read(cloneDetailProvider(cloneId).notifier).delete();
    if (context.mounted) {
      if (success) {
        context.pop();
      } else {
        context.showSnackBar('Failed to delete clone', isError: true);
      }
    }
  }
}

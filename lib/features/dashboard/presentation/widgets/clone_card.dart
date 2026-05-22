import 'package:flutter/material.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../../models/clone_info.dart';
import '../../../../models/clone_status.dart';

class CloneCard extends StatelessWidget {
  const CloneCard({
    super.key,
    required this.clone,
    required this.onTap,
    required this.onLaunch,
    required this.onStop,
  });

  final CloneInfo clone;
  final VoidCallback onTap;
  final VoidCallback onLaunch;
  final VoidCallback onStop;

  Color _statusColor(CloneStatus status) {
    return switch (status) {
      CloneStatus.running => AppColors.cloneRunning,
      CloneStatus.installing => AppColors.cloneInstalling,
      CloneStatus.error => AppColors.cloneError,
      _ => AppColors.cloneStopped,
    };
  }

  IconData _statusIcon(CloneStatus status) {
    return switch (status) {
      CloneStatus.running => Icons.play_circle_filled,
      CloneStatus.installing => Icons.downloading,
      CloneStatus.ready => Icons.check_circle,
      CloneStatus.stopped => Icons.stop_circle,
      CloneStatus.error => Icons.error,
    };
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: context.colorScheme.primaryContainer,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  Icons.apps,
                  color: context.colorScheme.primary,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      clone.appName,
                      style: context.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        Icon(
                          _statusIcon(clone.status),
                          size: 14,
                          color: _statusColor(clone.status),
                        ),
                        const SizedBox(width: 4),
                        Text(
                          clone.status.label,
                          style: context.textTheme.bodySmall?.copyWith(
                            color: _statusColor(clone.status),
                          ),
                        ),
                        if (clone.profile != null) ...[
                          const SizedBox(width: 12),
                          Icon(
                            Icons.smartphone,
                            size: 14,
                            color: context.colorScheme.outline,
                          ),
                          const SizedBox(width: 4),
                          Expanded(
                            child: Text(
                              clone.profile!.name,
                              style: context.textTheme.bodySmall?.copyWith(
                                color: context.colorScheme.outline,
                              ),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ],
                ),
              ),
              if (clone.status.isRunning)
                IconButton(
                  icon: const Icon(Icons.stop),
                  onPressed: onStop,
                  tooltip: 'Stop clone',
                )
              else if (clone.status.canLaunch)
                IconButton(
                  icon: const Icon(Icons.play_arrow),
                  onPressed: onLaunch,
                  tooltip: 'Launch clone',
                ),
              const Icon(Icons.chevron_right),
            ],
          ),
        ),
      ),
    );
  }
}

import 'dart:io';
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
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                width: 52,
                height: 52,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(14),
                  image: clone.appIconPath != null && clone.appIconPath!.isNotEmpty
                      ? DecorationImage(
                          image: FileImage(File(clone.appIconPath!)),
                          fit: BoxFit.cover,
                        )
                      : null,
                ),
                child: clone.appIconPath != null && clone.appIconPath!.isNotEmpty
                    ? null
                    : Icon(
                        Icons.apps,
                        size: 28,
                        color: context.colorScheme.primary,
                      ),
              ),
              const SizedBox(height: 10),
              Text(
                clone.appName,
                style: context.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 4),
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(
                    _statusIcon(clone.status),
                    size: 12,
                    color: _statusColor(clone.status),
                  ),
                  const SizedBox(width: 4),
                  Text(
                    clone.status.label,
                    style: context.textTheme.labelSmall?.copyWith(
                      color: _statusColor(clone.status),
                    ),
                  ),
                ],
              ),
              if (clone.profile != null) ...[
                const SizedBox(height: 2),
                Text(
                  clone.profile!.name,
                  style: context.textTheme.labelSmall?.copyWith(
                    color: context.colorScheme.outline,
                    fontSize: 10,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
              const Spacer(),
              SizedBox(
                height: 32,
                child: _buildActionButton(context),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildActionButton(BuildContext context) {
    if (clone.status.isRunning) {
      return FilledButton.tonal(
        onPressed: onStop,
        style: FilledButton.styleFrom(
          padding: const EdgeInsets.symmetric(horizontal: 12),
          minimumSize: Size.zero,
          tapTargetSize: MaterialTapTargetSize.shrinkWrap,
        ),
        child: const Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.stop, size: 14),
            SizedBox(width: 4),
            Text('Stop', style: TextStyle(fontSize: 12)),
          ],
        ),
      );
    }
    if (clone.status.canLaunch) {
      return FilledButton(
        onPressed: onLaunch,
        style: FilledButton.styleFrom(
          padding: const EdgeInsets.symmetric(horizontal: 12),
          minimumSize: Size.zero,
          tapTargetSize: MaterialTapTargetSize.shrinkWrap,
        ),
        child: const Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.play_arrow, size: 14),
            SizedBox(width: 4),
            Text('Launch', style: TextStyle(fontSize: 12)),
          ],
        ),
      );
    }
    if (clone.status == CloneStatus.installing) {
      return const SizedBox(
        width: 16,
        height: 16,
        child: CircularProgressIndicator(strokeWidth: 2),
      );
    }
    return const SizedBox.shrink();
  }
}

import 'package:flutter/material.dart';

import '../../../../core/theme/app_theme.dart';
import '../../../../models/clone_status.dart';

class CloneActions extends StatelessWidget {
  const CloneActions({
    super.key,
    required this.status,
    required this.onLaunch,
    required this.onStop,
  });

  final CloneStatus status;
  final VoidCallback onLaunch;
  final VoidCallback onStop;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (status.canLaunch)
          Expanded(
            child: FilledButton.icon(
              onPressed: onLaunch,
              icon: const Icon(Icons.play_arrow),
              label: const Text('Launch'),
              style: FilledButton.styleFrom(
                backgroundColor: AppColors.cloneRunning,
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
            ),
          ),
        if (status.isRunning)
          Expanded(
            child: FilledButton.icon(
              onPressed: onStop,
              icon: const Icon(Icons.stop),
              label: const Text('Stop'),
              style: FilledButton.styleFrom(
                backgroundColor: AppColors.cloneError,
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
            ),
          ),
        if (status == CloneStatus.installing)
          const Expanded(
            child: Center(
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
                  SizedBox(width: 12),
                  Text('Installing...'),
                ],
              ),
            ),
          ),
      ],
    );
  }
}

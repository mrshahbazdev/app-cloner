import 'package:flutter/material.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../core/theme/app_theme.dart';

class RunningClonesBar extends StatelessWidget {
  const RunningClonesBar({
    super.key,
    required this.runningCount,
    required this.totalCount,
  });

  final int runningCount;
  final int totalCount;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.fromLTRB(16, 8, 16, 0),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: AppColors.cloneRunning.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: AppColors.cloneRunning.withValues(alpha: 0.3),
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: const BoxDecoration(
              color: AppColors.cloneRunning,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 12),
          Text(
            '$runningCount of $totalCount clones running',
            style: context.textTheme.bodyMedium?.copyWith(
              color: AppColors.cloneRunning,
              fontWeight: FontWeight.w600,
            ),
          ),
          const Spacer(),
          Icon(
            Icons.memory,
            size: 16,
            color: AppColors.cloneRunning.withValues(alpha: 0.7),
          ),
        ],
      ),
    );
  }
}

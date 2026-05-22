import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../core/router/app_router.dart';

class EmptyState extends StatelessWidget {
  const EmptyState({super.key});

  @override
  Widget build(BuildContext context) {
    return CustomScrollView(
      slivers: [
        SliverFillRemaining(
          hasScrollBody: false,
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(32),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(
                    Icons.content_copy_outlined,
                    size: 80,
                    color: context.colorScheme.primary
                        .withValues(alpha: 0.5),
                  ),
                  const SizedBox(height: 24),
                  Text(
                    'No Clones Yet',
                    style: context.textTheme.headlineSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Create your first app clone to run multiple instances '
                    'of any app on this device.',
                    textAlign: TextAlign.center,
                    style: context.textTheme.bodyMedium?.copyWith(
                      color: context.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 32),
                  FilledButton.icon(
                    onPressed: () => context.push(AppRoutes.appPicker),
                    icon: const Icon(Icons.add),
                    label: const Text('Create Clone'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }
}

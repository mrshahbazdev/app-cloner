import 'dart:io';
import 'package:flutter/material.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../models/clone_info.dart';

class AppListTile extends StatelessWidget {
  const AppListTile({
    super.key,
    required this.app,
    required this.onTap,
  });

  final InstalledApp app;
  final VoidCallback onTap;

  IconData _categoryIcon(String? category) {
    return switch (category?.toLowerCase()) {
      'social' => Icons.people,
      'games' => Icons.sports_esports,
      'productivity' => Icons.work,
      'media' => Icons.play_circle,
      'tools' => Icons.build,
      _ => Icons.android,
    };
  }

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Container(
        width: 44,
        height: 44,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(10),
          image: app.iconPath != null && app.iconPath!.isNotEmpty
              ? DecorationImage(
                  image: FileImage(File(app.iconPath!)),
                  fit: BoxFit.cover,
                )
              : null,
        ),
        child: app.iconPath != null && app.iconPath!.isNotEmpty
            ? null
            : Icon(
                _categoryIcon(app.category),
                color: context.colorScheme.secondary,
              ),
      ),
      title: Text(app.appName),
      subtitle: Row(
        children: [
          Expanded(
            child: Text(
              app.packageName,
              style: context.textTheme.bodySmall?.copyWith(
                color: context.colorScheme.outline,
              ),
              overflow: TextOverflow.ellipsis,
            ),
          ),
          if (app.category != null && app.category!.isNotEmpty) ...[
            const SizedBox(width: 8),
            Container(
              padding: const EdgeInsets.symmetric(
                  horizontal: 6, vertical: 1),
              decoration: BoxDecoration(
                color: context.colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(
                app.category!,
                style: context.textTheme.labelSmall?.copyWith(
                  color: context.colorScheme.outline,
                  fontSize: 10,
                ),
              ),
            ),
          ],
        ],
      ),
      trailing: app.isSystemApp
          ? Chip(
              label: Text(
                'System',
                style: context.textTheme.labelSmall,
              ),
              visualDensity: VisualDensity.compact,
            )
          : const Icon(Icons.content_copy, size: 20),
      onTap: onTap,
    );
  }
}

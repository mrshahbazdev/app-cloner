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

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Container(
        width: 44,
        height: 44,
        decoration: BoxDecoration(
          color: context.colorScheme.secondaryContainer,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Icon(
          Icons.android,
          color: context.colorScheme.secondary,
        ),
      ),
      title: Text(app.appName),
      subtitle: Text(
        app.packageName,
        style: context.textTheme.bodySmall?.copyWith(
          color: context.colorScheme.outline,
        ),
      ),
      trailing: app.isSystemApp
          ? Chip(
              label: Text(
                'System',
                style: context.textTheme.labelSmall,
              ),
              visualDensity: VisualDensity.compact,
            )
          : null,
      onTap: onTap,
    );
  }
}

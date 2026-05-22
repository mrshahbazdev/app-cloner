import 'package:flutter/material.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../models/device_profile.dart';

class ProfileSummaryCard extends StatelessWidget {
  const ProfileSummaryCard({
    super.key,
    required this.profile,
    required this.onEdit,
  });

  final DeviceProfile profile;
  final VoidCallback onEdit;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Virtual Identity',
                  style: context.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                TextButton.icon(
                  onPressed: onEdit,
                  icon: const Icon(Icons.edit, size: 16),
                  label: const Text('Edit'),
                ),
              ],
            ),
            const Divider(),
            _profileRow(context, 'Device', profile.name),
            _profileRow(context, 'Model', profile.model),
            _profileRow(context, 'Brand', profile.brand),
            _profileRow(context, 'Android ID', _truncate(profile.androidId)),
            _profileRow(context, 'IMEI', _truncate(profile.imei)),
            _profileRow(
                context, 'Screen', '${profile.screenWidth}x${profile.screenHeight}'),
            if (profile.proxyHost != null)
              _profileRow(
                  context, 'Proxy', '${profile.proxyHost}:${profile.proxyPort}'),
          ],
        ),
      ),
    );
  }

  Widget _profileRow(BuildContext context, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: context.textTheme.bodySmall?.copyWith(
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

  String _truncate(String s) {
    if (s.length <= 12) return s;
    return '${s.substring(0, 6)}...${s.substring(s.length - 4)}';
  }
}

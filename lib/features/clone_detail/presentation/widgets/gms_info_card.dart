import 'package:flutter/material.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../services/clone_engine_service.dart';

class GmsInfoCard extends StatelessWidget {
  const GmsInfoCard({
    super.key,
    required this.gmsState,
    required this.isPlayStoreClone,
  });

  final GmsState gmsState;
  final bool isPlayStoreClone;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.play_arrow_rounded,
                  color: context.colorScheme.primary,
                ),
                const SizedBox(width: 8),
                Text(
                  'Google Services',
                  style: context.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const Divider(),
            _infoRow(
              context,
              'GMS',
              gmsState.gmsAvailable ? 'Available' : 'Not Available',
              gmsState.gmsAvailable ? Colors.green : Colors.red,
            ),
            if (gmsState.gmsVersion != null)
              _infoRow(context, 'GMS Version', gmsState.gmsVersion!, null),
            _infoRow(
              context,
              'Play Store',
              gmsState.playStoreVersion ?? 'Not Installed',
              gmsState.playStoreVersion != null ? Colors.green : Colors.orange,
            ),
            _infoRow(
              context,
              'Mode',
              isPlayStoreClone ? 'Real GMS (Proxied)' : 'MicroG',
              null,
            ),
            _infoRow(
              context,
              'Clone Slots',
              '${gmsState.activePlayStoreClones}/${gmsState.maxPlayStoreClones}',
              null,
            ),
          ],
        ),
      ),
    );
  }

  Widget _infoRow(
    BuildContext context,
    String label,
    String value,
    Color? valueColor,
  ) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: context.textTheme.bodySmall?.copyWith(
              color: context.colorScheme.outline,
            ),
          ),
          Text(
            value,
            style: context.textTheme.bodyMedium?.copyWith(
              color: valueColor,
              fontWeight: valueColor != null ? FontWeight.w600 : null,
            ),
          ),
        ],
      ),
    );
  }
}

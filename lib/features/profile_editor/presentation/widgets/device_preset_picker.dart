import 'package:flutter/material.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../models/device_profile.dart';

class DevicePresetPicker extends StatelessWidget {
  const DevicePresetPicker({super.key, required this.onPresetSelected});

  final void Function(Map<String, dynamic> preset) onPresetSelected;

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
                Icon(Icons.devices, color: context.colorScheme.primary),
                const SizedBox(width: 8),
                Text('Quick Preset',
                    style: context.textTheme.titleSmall
                        ?.copyWith(fontWeight: FontWeight.w600)),
              ],
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: DevicePresets.presets.map((preset) {
                return ActionChip(
                  label: Text(preset['name'] as String),
                  onPressed: () => onPresetSelected(preset),
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../providers/settings_provider.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeMode = ref.watch(themeModeProvider);
    final proxyEnabled = ref.watch(proxyEnabledProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        children: [
          _buildSectionHeader(context, 'Appearance'),
          ListTile(
            leading: const Icon(Icons.palette_outlined),
            title: const Text('Theme'),
            subtitle: Text(themeMode.name.toUpperCase()),
            onTap: () => _showThemePicker(context, ref, themeMode),
          ),
          const Divider(),
          _buildSectionHeader(context, 'Network'),
          SwitchListTile(
            secondary: const Icon(Icons.vpn_key_outlined),
            title: const Text('Per-Clone Proxy'),
            subtitle:
                const Text('Route each clone through a different proxy'),
            value: proxyEnabled,
            onChanged: (_) =>
                ref.read(proxyEnabledProvider.notifier).toggle(),
          ),
          const Divider(),
          _buildSectionHeader(context, 'Engine'),
          ListTile(
            leading: const Icon(Icons.memory),
            title: const Text('Engine Version'),
            subtitle: const Text('BlackBox v1.0.0 (TitanClone fork)'),
          ),
          ListTile(
            leading: const Icon(Icons.android),
            title: const Text('Supported Android'),
            subtitle: const Text('Android 10 (API 29) - Android 15 (API 35)'),
          ),
          const Divider(),
          _buildSectionHeader(context, 'About'),
          ListTile(
            leading: const Icon(Icons.info_outline),
            title: const Text('TitanClone'),
            subtitle: const Text('v1.0.0'),
          ),
          ListTile(
            leading: const Icon(Icons.description_outlined),
            title: const Text('Licenses'),
            onTap: () => showLicensePage(
              context: context,
              applicationName: 'TitanClone',
              applicationVersion: '1.0.0',
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      child: Text(
        title,
        style: context.textTheme.labelLarge?.copyWith(
          color: context.colorScheme.primary,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }

  void _showThemePicker(
    BuildContext context,
    WidgetRef ref,
    ThemeMode current,
  ) {
    showDialog(
      context: context,
      builder: (context) => SimpleDialog(
        title: const Text('Select Theme'),
        children: ThemeMode.values.map((mode) {
          return RadioListTile<ThemeMode>(
            title: Text(mode.name.toUpperCase()),
            value: mode,
            groupValue: current,
            onChanged: (value) {
              if (value != null) {
                ref.read(themeModeProvider.notifier).setThemeMode(value);
              }
              Navigator.pop(context);
            },
          );
        }).toList(),
      ),
    );
  }
}

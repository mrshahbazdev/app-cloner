import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/extensions/context_extensions.dart';
import '../../providers/gms_provider.dart';
import '../../providers/settings_provider.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeMode = ref.watch(themeModeProvider);
    final proxyEnabled = ref.watch(proxyEnabledProvider);
    final maxClones = ref.watch(maxConcurrentClonesProvider);
    final memoryLimit = ref.watch(memoryLimitProvider);
    final autoStart = ref.watch(autoStartProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        children: [
          _buildSectionHeader(context, 'General'),
          ListTile(
            leading: const Icon(Icons.palette_outlined),
            title: const Text('Theme'),
            subtitle: Text(themeMode.name.toUpperCase()),
            onTap: () => _showThemePicker(context, ref, themeMode),
          ),
          SwitchListTile(
            secondary: const Icon(Icons.play_arrow_outlined),
            title: const Text('Auto-Start Last Clone'),
            subtitle: const Text('Launch the last active clone on app start'),
            value: autoStart,
            onChanged: (_) =>
                ref.read(autoStartProvider.notifier).toggle(),
          ),
          const Divider(),
          _buildSectionHeader(context, 'Performance'),
          ListTile(
            leading: const Icon(Icons.layers_outlined),
            title: const Text('Max Concurrent Clones'),
            subtitle: Text('$maxClones'),
            trailing: SizedBox(
              width: 120,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  IconButton(
                    icon: const Icon(Icons.remove),
                    onPressed: maxClones > 1
                        ? () => ref
                            .read(maxConcurrentClonesProvider.notifier)
                            .set(maxClones - 1)
                        : null,
                    iconSize: 20,
                  ),
                  Text('$maxClones',
                      style: context.textTheme.titleMedium),
                  IconButton(
                    icon: const Icon(Icons.add),
                    onPressed: maxClones < AppConstants.maxClones
                        ? () => ref
                            .read(maxConcurrentClonesProvider.notifier)
                            .set(maxClones + 1)
                        : null,
                    iconSize: 20,
                  ),
                ],
              ),
            ),
          ),
          ListTile(
            leading: const Icon(Icons.memory),
            title: const Text('Memory Limit per Clone'),
            subtitle: Text('$memoryLimit MB'),
            onTap: () =>
                _showMemoryLimitPicker(context, ref, memoryLimit),
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
          _buildSectionHeader(context, 'Storage'),
          ListTile(
            leading: const Icon(Icons.folder_outlined),
            title: const Text('Storage Usage'),
            subtitle: const Text('View per-clone storage breakdown'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => _showStorageBreakdown(context),
          ),
          ListTile(
            leading: const Icon(Icons.delete_sweep_outlined),
            title: const Text('Clear All Cache'),
            subtitle: const Text('Free up space by clearing all clone caches'),
            onTap: () => _clearAllCache(context, ref),
          ),
          const Divider(),
          _buildSectionHeader(context, 'Engine'),
          ListTile(
            leading: const Icon(Icons.settings_applications_outlined),
            title: const Text('Engine Version'),
            subtitle: const Text('BlackBox v1.0.0 (TitanClone fork)'),
          ),
          ListTile(
            leading: const Icon(Icons.android),
            title: const Text('Supported Android'),
            subtitle: const Text('Android 10 (API 29) - Android 15 (API 35)'),
          ),
          const Divider(),
          _buildSectionHeader(context, 'Google Services'),
          ref.watch(gmsStateProvider).when(
                data: (gmsState) => Column(
                  children: [
                    ListTile(
                      leading: Icon(
                        Icons.check_circle,
                        color: gmsState.gmsAvailable ? Colors.green : Colors.red,
                      ),
                      title: const Text('GMS Status'),
                      subtitle: Text(
                        gmsState.gmsAvailable
                            ? 'Available (${gmsState.gmsVersion ?? "unknown"})'
                            : 'Not Available',
                      ),
                    ),
                    ListTile(
                      leading: const Icon(Icons.store),
                      title: const Text('Play Store'),
                      subtitle: Text(
                        gmsState.playStoreVersion != null
                            ? 'v${gmsState.playStoreVersion}'
                            : 'Not Installed',
                      ),
                    ),
                    ListTile(
                      leading: const Icon(Icons.copy_all),
                      title: const Text('Play Store Clones'),
                      subtitle: Text(
                        '${gmsState.activePlayStoreClones}/${gmsState.maxPlayStoreClones} slots used',
                      ),
                    ),
                  ],
                ),
                loading: () => const ListTile(
                  leading: SizedBox(
                    width: 24,
                    height: 24,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
                  title: Text('Loading GMS info...'),
                ),
                error: (_, __) => const ListTile(
                  leading: Icon(Icons.error_outline, color: Colors.red),
                  title: Text('Failed to load GMS info'),
                ),
              ),
          const Divider(),
          _buildSectionHeader(context, 'Device Compatibility'),
          ref.watch(compatReportProvider).when(
                data: (report) => Column(
                  children: [
                    ListTile(
                      leading: Icon(
                        report.isSupported ? Icons.verified : Icons.warning,
                        color: report.isSupported ? Colors.green : Colors.orange,
                      ),
                      title: Text('Android ${report.androidVersion} (API ${report.apiLevel})'),
                      subtitle: Text(
                        report.isSupported ? 'Fully Supported' : 'Limited Support',
                      ),
                    ),
                    if (report.issues.isNotEmpty)
                      ListTile(
                        leading: const Icon(Icons.info_outline),
                        title: const Text('Issues'),
                        subtitle: Text(report.issues.join('\n')),
                        isThreeLine: report.issues.length > 1,
                      ),
                    if (report.recommendations.isNotEmpty)
                      ListTile(
                        leading: const Icon(Icons.lightbulb_outline),
                        title: const Text('Recommendations'),
                        subtitle: Text(report.recommendations.join('\n')),
                        isThreeLine: report.recommendations.length > 1,
                      ),
                  ],
                ),
                loading: () => const ListTile(
                  title: Text('Checking compatibility...'),
                ),
                error: (_, __) => const SizedBox.shrink(),
              ),
          ref.watch(batteryInfoProvider).when(
                data: (info) => ListTile(
                  leading: Icon(
                    Icons.battery_alert,
                    color: info.isIgnoringOptimization ? Colors.green : Colors.orange,
                  ),
                  title: const Text('Battery Optimization'),
                  subtitle: Text(
                    info.isIgnoringOptimization
                        ? 'Disabled (recommended)'
                        : 'Enabled — clones may be killed in background',
                  ),
                ),
                loading: () => const SizedBox.shrink(),
                error: (_, __) => const SizedBox.shrink(),
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
          const SizedBox(height: 24),
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
          return SimpleDialogOption(
            onPressed: () {
              ref.read(themeModeProvider.notifier).setThemeMode(mode);
              Navigator.pop(context);
            },
            child: Row(
              children: [
                Icon(
                  mode == current
                      ? Icons.radio_button_checked
                      : Icons.radio_button_unchecked,
                  size: 20,
                ),
                const SizedBox(width: 12),
                Text(mode.name.toUpperCase()),
              ],
            ),
          );
        }).toList(),
      ),
    );
  }

  void _showMemoryLimitPicker(
    BuildContext context,
    WidgetRef ref,
    int current,
  ) {
    final options = [128, 192, 256, 384, 512];
    showDialog(
      context: context,
      builder: (context) => SimpleDialog(
        title: const Text('Memory Limit per Clone'),
        children: options.map((mb) {
          return SimpleDialogOption(
            onPressed: () {
              ref.read(memoryLimitProvider.notifier).set(mb);
              Navigator.pop(context);
            },
            child: Row(
              children: [
                Icon(
                  mb == current
                      ? Icons.radio_button_checked
                      : Icons.radio_button_unchecked,
                  size: 20,
                ),
                const SizedBox(width: 12),
                Text('$mb MB'),
              ],
            ),
          );
        }).toList(),
      ),
    );
  }

  void _showStorageBreakdown(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Storage Usage'),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: Text('No clones yet'),
              subtitle: Text('Create clones to see storage usage'),
              dense: true,
              contentPadding: EdgeInsets.zero,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  void _clearAllCache(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Clear All Cache'),
        content: const Text(
            'This will clear the cache for all clones. App data will not be affected.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('All caches cleared'),
                  behavior: SnackBarBehavior.floating,
                ),
              );
            },
            child: const Text('Clear'),
          ),
        ],
      ),
    );
  }
}

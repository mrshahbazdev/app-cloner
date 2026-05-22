import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../models/device_profile.dart';
import '../../../../services/clone_engine_service.dart';
import '../../providers/profile_editor_provider.dart';
import '../widgets/device_preset_picker.dart';
import '../widgets/profile_form_field.dart';

class ProfileEditorScreen extends ConsumerStatefulWidget {
  const ProfileEditorScreen({super.key, required this.cloneId});

  final String cloneId;

  @override
  ConsumerState<ProfileEditorScreen> createState() =>
      _ProfileEditorScreenState();
}

class _ProfileEditorScreenState extends ConsumerState<ProfileEditorScreen> {
  final _formKey = GlobalKey<FormState>();

  late TextEditingController _nameController;
  late TextEditingController _modelController;
  late TextEditingController _brandController;
  late TextEditingController _manufacturerController;
  late TextEditingController _proxyHostController;
  late TextEditingController _proxyPortController;

  bool _isRandomizing = false;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController();
    _modelController = TextEditingController();
    _brandController = TextEditingController();
    _manufacturerController = TextEditingController();
    _proxyHostController = TextEditingController();
    _proxyPortController = TextEditingController();
  }

  @override
  void dispose() {
    _nameController.dispose();
    _modelController.dispose();
    _brandController.dispose();
    _manufacturerController.dispose();
    _proxyHostController.dispose();
    _proxyPortController.dispose();
    super.dispose();
  }

  void _populateFields(DeviceProfile profile) {
    _nameController.text = profile.name;
    _modelController.text = profile.model;
    _brandController.text = profile.brand;
    _manufacturerController.text = profile.manufacturer;
    _proxyHostController.text = profile.proxyHost ?? '';
    _proxyPortController.text = profile.proxyPort?.toString() ?? '';
  }

  @override
  Widget build(BuildContext context) {
    final profileAsync = ref.watch(profileEditorProvider(widget.cloneId));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Edit Virtual Profile'),
        actions: [
          IconButton(
            icon: _isRandomizing
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.shuffle),
            tooltip: 'Randomize Profile',
            onPressed: _isRandomizing ? null : () => _randomizeProfile(ref),
          ),
          TextButton(
            onPressed: () => _saveProfile(ref),
            child: const Text('Save'),
          ),
        ],
      ),
      body: profileAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(child: Text('Error: $error')),
        data: (profile) {
          if (profile == null) {
            return const Center(child: Text('Profile not found'));
          }

          if (_nameController.text.isEmpty) {
            _populateFields(profile);
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  DevicePresetPicker(
                    onPresetSelected: (preset) {
                      _nameController.text = preset['name'] as String;
                      _modelController.text = preset['model'] as String;
                      _brandController.text = preset['brand'] as String;
                      _manufacturerController.text =
                          preset['manufacturer'] as String;
                    },
                  ),
                  const SizedBox(height: 24),
                  Text('Device Identity',
                      style: context.textTheme.titleMedium),
                  const SizedBox(height: 12),
                  ProfileFormField(
                    controller: _nameController,
                    label: 'Device Name',
                    hint: 'e.g., Google Pixel 8 Pro',
                  ),
                  ProfileFormField(
                    controller: _modelController,
                    label: 'Model',
                    hint: 'e.g., husky',
                  ),
                  ProfileFormField(
                    controller: _brandController,
                    label: 'Brand',
                    hint: 'e.g., google',
                  ),
                  ProfileFormField(
                    controller: _manufacturerController,
                    label: 'Manufacturer',
                    hint: 'e.g., Google',
                  ),
                  const Divider(height: 32),
                  Text('Network Proxy (Optional)',
                      style: context.textTheme.titleMedium),
                  const SizedBox(height: 12),
                  ProfileFormField(
                    controller: _proxyHostController,
                    label: 'Proxy Host',
                    hint: 'e.g., 127.0.0.1',
                  ),
                  ProfileFormField(
                    controller: _proxyPortController,
                    label: 'Proxy Port',
                    hint: 'e.g., 1080',
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 24),
                  _buildReadOnlySection(context, profile),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildReadOnlySection(BuildContext context, DeviceProfile profile) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.fingerprint,
                    size: 20, color: context.colorScheme.primary),
                const SizedBox(width: 8),
                Text('Auto-Generated Identifiers',
                    style: context.textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w600,
                    )),
              ],
            ),
            const Divider(),
            _readOnlyRow('Android ID', profile.androidId),
            _readOnlyRow('IMEI', profile.imei),
            _readOnlyRow('MAC Address', profile.macAddress),
            _readOnlyRow('BT MAC', profile.bluetoothMac),
            _readOnlyRow('GSF ID', profile.gsfId),
            _readOnlyRow('Advertising ID', profile.advertisingId),
            if (profile.fingerprint.isNotEmpty)
              _readOnlyRow('Fingerprint', profile.fingerprint),
          ],
        ),
      ),
    );
  }

  Widget _readOnlyRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
          Flexible(
            child: SelectableText(
              _truncate(value),
              style: const TextStyle(fontSize: 12),
              textAlign: TextAlign.end,
            ),
          ),
        ],
      ),
    );
  }

  String _truncate(String s) {
    if (s.length <= 20) return s;
    return '${s.substring(0, 8)}...${s.substring(s.length - 6)}';
  }

  Future<void> _randomizeProfile(WidgetRef ref) async {
    setState(() => _isRandomizing = true);

    try {
      final engine = ref.read(cloneEngineServiceProvider);
      final newProfile = await engine.resetCloneProfile(widget.cloneId);
      if (newProfile != null && mounted) {
        _populateFields(newProfile);
        ref.invalidate(profileEditorProvider(widget.cloneId));
        context.showSnackBar('Profile randomized');
      }
    } finally {
      if (mounted) setState(() => _isRandomizing = false);
    }
  }

  Future<void> _saveProfile(WidgetRef ref) async {
    if (!_formKey.currentState!.validate()) return;

    final currentProfile =
        ref.read(profileEditorProvider(widget.cloneId)).valueOrNull;
    if (currentProfile == null) return;

    final updated = currentProfile.copyWith(
      name: _nameController.text,
      model: _modelController.text,
      brand: _brandController.text,
      manufacturer: _manufacturerController.text,
      proxyHost: _proxyHostController.text.isNotEmpty
          ? _proxyHostController.text
          : null,
      proxyPort: _proxyPortController.text.isNotEmpty
          ? int.tryParse(_proxyPortController.text)
          : null,
    );

    final success = await ref
        .read(profileEditorProvider(widget.cloneId).notifier)
        .saveProfile(updated);

    if (!mounted) return;
    if (success) {
      context.showSnackBar('Profile saved');
      context.pop();
    } else {
      context.showSnackBar('Failed to save profile', isError: true);
    }
  }
}

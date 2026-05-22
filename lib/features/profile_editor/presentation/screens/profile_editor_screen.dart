import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/extensions/context_extensions.dart';
import '../../../../models/device_profile.dart';
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
            Text('Auto-Generated Identifiers (Read-Only)',
                style: context.textTheme.titleSmall),
            const Divider(),
            _readOnlyRow('Android ID', profile.androidId),
            _readOnlyRow('IMEI', profile.imei),
            _readOnlyRow('MAC Address', profile.macAddress),
            _readOnlyRow('BT MAC', profile.bluetoothMac),
            _readOnlyRow('GSF ID', profile.gsfId),
            _readOnlyRow('Advertising ID', profile.advertisingId),
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
          SelectableText(value, style: const TextStyle(fontSize: 12)),
        ],
      ),
    );
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

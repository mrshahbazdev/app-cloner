import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/constants/app_constants.dart';
import '../core/utils/logger.dart';
import '../models/clone_info.dart';

final cloneEventStreamProvider = Provider<CloneEventStream>((ref) {
  final stream = CloneEventStream();
  ref.onDispose(stream.dispose);
  return stream;
});

final cloneEventsProvider = StreamProvider<CloneEvent>((ref) {
  final eventStream = ref.watch(cloneEventStreamProvider);
  return eventStream.events;
});

final installProgressProvider =
    StreamProvider.family<int, String>((ref, cloneId) {
  final eventStream = ref.watch(cloneEventStreamProvider);
  return eventStream.events
      .where((e) =>
          e.cloneId == cloneId && e.eventType == 'install_progress')
      .map((e) => e.data?['percent'] as int? ?? 0);
});

class CloneEventStream {
  static const _eventChannel = EventChannel(AppConstants.eventChannelName);

  StreamController<CloneEvent>? _controller;

  Stream<CloneEvent> get events {
    _controller ??= StreamController<CloneEvent>.broadcast(
      onListen: _startListening,
      onCancel: _stopListening,
    );
    return _controller!.stream;
  }

  StreamSubscription<dynamic>? _subscription;

  void _startListening() {
    _subscription = _eventChannel.receiveBroadcastStream().listen(
      (event) {
        if (event is Map) {
          try {
            final cloneEvent =
                CloneEvent.fromJson(Map<String, dynamic>.from(event));
            _controller?.add(cloneEvent);
          } catch (e) {
            AppLogger.error('Failed to parse clone event', error: e);
          }
        }
      },
      onError: (error) {
        AppLogger.error('Clone event stream error', error: error);
      },
    );
  }

  void _stopListening() {
    _subscription?.cancel();
    _subscription = null;
  }

  void dispose() {
    _stopListening();
    _controller?.close();
    _controller = null;
  }
}

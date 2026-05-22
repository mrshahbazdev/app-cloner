enum CloneStatus {
  installing('Installing'),
  ready('Ready'),
  running('Running'),
  stopped('Stopped'),
  error('Error');

  const CloneStatus(this.label);
  final String label;

  bool get isRunning => this == CloneStatus.running;
  bool get canLaunch =>
      this == CloneStatus.ready || this == CloneStatus.stopped;
  bool get canDelete => this != CloneStatus.installing;
}

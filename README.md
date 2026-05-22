# TitanClone — Android App Cloning & Virtualization Platform

A production-grade Android application that enables users to run multiple isolated instances (clones) of any Android app on a single device. Each clone operates in its own sandboxed environment with a unique virtual identity, isolated storage, and independent process management.

## Core Features

- **Multi-Instance** — Run 2+ copies of WhatsApp, Telegram, Instagram, etc. simultaneously
- **Play Store Cloning** — Clone Google Play Store 12+ times with different Gmail accounts
- **Complete Isolation** — Each clone has separate storage, accounts, notifications
- **Virtual Identity** — 126+ spoofed device properties per clone (device model, IMEI, Android ID, etc.)
- **Per-Clone Network** — Each clone can route through different VPN/proxy for unique IP addresses
- **No Root Required** — Works entirely in userspace without rooting
- **Cross-Version** — Supports Android 10 (API 29) through Android 15 (API 35)

## Architecture

```
┌─────────────────────────────────────────────┐
│            FLUTTER UI LAYER (Dart)           │
│  Riverpod + GoRouter + Material 3           │
└─────────────────┬───────────────────────────┘
                  │ MethodChannel / EventChannel
┌─────────────────▼───────────────────────────┐
│         ANDROID NATIVE BRIDGE (Kotlin)       │
│  FlutterBridgePlugin + Profile Management   │
└─────────────────┬───────────────────────────┘
                  │ Direct API Calls
┌─────────────────▼───────────────────────────┐
│      CORE VIRTUALIZATION ENGINE (Java + C++) │
│  System Service Proxies + Process Mgmt      │
│  IO Redirection + Binder Intercept (NDK)    │
└─────────────────────────────────────────────┘
```

### Process Model

| Process | Name | Role |
|---------|------|------|
| **Main** | `:main` | Host Flutter app, UI rendering |
| **Virtual Server** | `:x` | Central service manager, IPC routing |
| **Clone Processes** | `:p0`-`:p11` | Each clone runs in isolated child process |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Flutter 3.x, Dart |
| State Management | Riverpod 2.x + freezed |
| Navigation | go_router |
| Local Storage | drift (SQLite) |
| Native Bridge | Kotlin, Pigeon |
| Core Engine | Java (BlackBox fork) |
| Native Layer | C++ (NDK), CMake |

## Project Structure

```
titan_clone/
├── lib/                          # Flutter (Dart) code
│   ├── main.dart                 # Entry point
│   ├── app.dart                  # MaterialApp + GoRouter
│   ├── core/                     # Constants, theme, utils, router
│   ├── features/                 # Feature modules
│   │   ├── dashboard/            # Main clone list
│   │   ├── app_picker/           # Select app to clone
│   │   ├── clone_detail/         # Clone info & actions
│   │   ├── profile_editor/       # Edit virtual identity
│   │   └── settings/             # App settings
│   ├── services/                 # Bridge services
│   └── models/                   # Data models
├── android/                      # Android native code
│   └── app/src/main/
│       ├── kotlin/.../bridge/    # Flutter-Native bridge
│       ├── kotlin/.../profile/   # Virtual identity system
│       ├── kotlin/.../gms/       # GMS/MicroG handling
│       ├── kotlin/.../notification/ # Notification routing
│       ├── java/.../engine/      # Core virtualization engine
│       │   ├── core/             # VirtualCore, EngineConfig
│       │   ├── stubs/            # System service proxies
│       │   ├── process/          # Process management
│       │   ├── storage/          # Virtual storage
│       │   ├── installer/        # APK installation
│       │   └── am/               # Activity management
│       └── cpp/                  # NDK / C++ code
│           ├── io_redirect.cpp
│           ├── memory_manager.cpp
│           ├── binder_intercept.cpp
│           ├── property_redirect.cpp
│           └── proc_maps_filter.cpp
├── pigeon/                       # Pigeon bridge definitions
├── docs/                         # Documentation
│   ├── architecture/             # ADRs
│   ├── compatibility/            # Android version matrix
│   └── api/                      # Bridge API docs
└── pubspec.yaml
```

## Getting Started

### Prerequisites

- Flutter SDK 3.x
- Android SDK (API 29-35)
- Android NDK (for C++ native code)
- Java 11+

### Setup

```bash
# Install Flutter dependencies
flutter pub get

# Generate code (freezed, riverpod, pigeon)
dart run build_runner build --delete-conflicting-outputs

# Build debug APK
flutter build apk --debug
```

## Development Phases

| Phase | Weeks | Description |
|-------|-------|-------------|
| 1. Research & Setup | 1-2 | Engine evaluation, project setup |
| 2. Core Sandboxing | 3-8 | APK cloning, system service proxies |
| 3. Stealth Profiles | 9-11 | 126+ property spoofing, anti-detection |
| 4. Flutter UI | 12-14 | Dashboard, clone management, profile editor |
| 5. Play Store Cloning | 15-18 | GMS proxy, multi-account, edge cases |
| 6. Optimization | 19-22 | Memory, security, deployment |

## License

Apache 2.0 — Based on [BlackBox](https://github.com/ArmchairAncap/BlackBox) (Apache 2.0 licensed).

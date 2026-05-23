# ADR-002: Flutter-Native Communication Bridge

## Status
Accepted

## Context
The Flutter UI layer needs to communicate with the Android native engine for clone lifecycle management, profile editing, and status updates.

## Decision
Use a dual-channel approach:
- **MethodChannel** — For request/response calls (create, launch, stop, delete clones)
- **EventChannel** — For real-time event streaming (status changes, errors)
- **Pigeon** — For type-safe code generation of the bridge API

## Bridge API

### MethodChannel (`com.titanclone/bridge`)
| Method | Parameters | Returns |
|--------|-----------|---------|
| `initializeEngine` | none | `bool` |
| `getInstalledApps` | none | `List<InstalledApp>` |
| `createClone` | `packageName`, `userId` | `CloneInfo` |
| `launchClone` | `cloneId` | `bool` |
| `stopClone` | `cloneId` | `bool` |
| `deleteClone` | `cloneId` | `bool` |
| `getClones` | none | `List<CloneInfo>` |
| `getCloneStatus` | `cloneId` | `String` |
| `getCloneProfile` | `cloneId` | `DeviceProfile` |
| `updateProfile` | `cloneId`, `profile` | `bool` |

### EventChannel (`com.titanclone/events`)
Streams `CloneEvent` objects with:
- `cloneId` — which clone the event is about
- `eventType` — status change type (installing, running, stopped, error)
- `message` — human-readable event description

## Consequences
- Type-safe bridge via Pigeon code generation
- Real-time UI updates via EventChannel streaming
- Clean separation between Flutter UI and native engine

# Bridge API Documentation

## Overview

The Flutter-Native bridge uses MethodChannel for request/response calls and EventChannel for real-time event streaming.

## MethodChannel API

Channel: `com.titanclone/bridge`

### `initializeEngine() -> bool`
Initialize the virtualization engine. Must be called before any other operations.

### `getInstalledApps() -> List<InstalledApp>`
Returns list of user-installed apps on the device.

### `createClone(packageName: String, userId: int, profile?: DeviceProfile) -> CloneInfo`
Create a new clone of the specified app.

### `launchClone(cloneId: String) -> bool`
Launch a clone in its own isolated process.

### `stopClone(cloneId: String) -> bool`
Stop a running clone process.

### `deleteClone(cloneId: String) -> bool`
Delete a clone and all its data.

### `getClones() -> List<CloneInfo>`
Get all registered clones.

### `getCloneStatus(cloneId: String) -> String`
Get the current status of a clone (installing, ready, running, stopped, error).

### `getCloneProfile(cloneId: String) -> DeviceProfile?`
Get the virtual device profile for a clone.

### `updateProfile(cloneId: String, profile: DeviceProfile) -> bool`
Update a clone's virtual device profile.

## EventChannel API

Channel: `com.titanclone/events`

### CloneEvent
```json
{
  "cloneId": "com.whatsapp_clone_0",
  "eventType": "running",
  "message": "Clone launched successfully",
  "data": { ... }
}
```

Event types: `installing`, `ready`, `running`, `stopped`, `error`, `engineInitialized`

## Data Models

### InstalledApp
```json
{
  "packageName": "com.whatsapp",
  "appName": "WhatsApp",
  "versionName": "2.24.1.5",
  "versionCode": 224010500,
  "isSystemApp": false
}
```

### CloneInfo
```json
{
  "id": "com.whatsapp_clone_0",
  "packageName": "com.whatsapp",
  "appName": "WhatsApp",
  "userId": 0,
  "status": "ready",
  "createdAt": "2026-05-22T00:00:00Z"
}
```

### DeviceProfile
```json
{
  "id": "uuid-here",
  "name": "Google Pixel 8 Pro",
  "model": "husky",
  "brand": "google",
  "manufacturer": "Google",
  "fingerprint": "google/husky/husky:14/...",
  "screenDensity": 420,
  "screenWidth": 1344,
  "screenHeight": 2992,
  "sdkVersion": 34,
  "releaseVersion": "14",
  "androidId": "a1b2c3d4e5f67890",
  "imei": "354321098765432",
  "macAddress": "02:AB:CD:EF:12:34",
  "bluetoothMac": "02:12:34:56:78:9A",
  "gsfId": "3a4b5c6d7e8f",
  "advertisingId": "uuid-here",
  "proxyHost": "127.0.0.1",
  "proxyPort": 1080,
  "proxyType": "SOCKS5"
}
```

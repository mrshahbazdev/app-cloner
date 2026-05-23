# Android Version Compatibility Matrix

| Feature | Android 10 (29) | Android 11 (30) | Android 12 (31) | Android 13 (33) | Android 14 (34) | Android 15 (35) |
|---------|:---:|:---:|:---:|:---:|:---:|:---:|
| Basic Cloning | Full | Full | Full | Full | Partial* | Partial* |
| IO Redirection | Full | Full | Full | Full | Full | Full |
| System Service Proxy | Full | Full | Full | Full | Full | Testing |
| Virtual Identity | Full | Full | Full | Full | Full | Full |
| Push Notifications (MicroG) | Full | Full | Full | Full | Partial* | Partial* |
| Background Services | Full | Full | Partial* | Partial* | Partial* | Partial* |
| Scoped Storage | Bypass | Partial | Partial | Enforced | Enforced | Enforced |
| `QUERY_ALL_PACKAGES` | N/A | Requires | Requires | Requires | Strict | Strict |
| Play Store Cloning | Full | Full | Full | Full | Full* | Testing |
| Per-Clone IP Proxy | Full | Full | Full | Full | Full | Full |
| 126+ Property Spoofing | Full | Full | Full | Full | Full | Full |
| GMS Proxy | Full | Full | Full | Full | Partial* | Testing |

_*Partial = Works with specific workarounds/configurations documented per version_

## Version-Specific Notes

### Android 10 (API 29)
- Scoped storage introduced but can opt-out with `requestLegacyExternalStorage`

### Android 11 (API 30)
- `QUERY_ALL_PACKAGES` restricted — declare `<queries>` in manifest
- `MANAGE_EXTERNAL_STORAGE` introduced

### Android 12 (API 31-32)
- Exact alarm permissions required
- Bluetooth permissions revamp
- Exported components must be explicit

### Android 13 (API 33)
- Notification runtime permission (`POST_NOTIFICATIONS`)
- Per-app language support
- Granular media permissions

### Android 14 (API 34)
- Foreground service types required
- Stricter background restrictions
- `QUERY_ALL_PACKAGES` Play Store enforcement

### Android 15 (API 35)
- Stricter scoped storage
- Edge-to-edge enforced
- `READ_MEDIA_VISUAL_USER_SELECTED`

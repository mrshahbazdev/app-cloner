# Play Store Compliance Documentation

## QUERY_ALL_PACKAGES Permission Justification

### Core Functionality Requirement

TitanClone requires the `QUERY_ALL_PACKAGES` permission because its core functionality — creating isolated instances (work profiles) of installed applications — is impossible without the ability to discover what applications are installed on the user's device.

**Specific use case:** Our app provides a similar experience to Android's built-in Work Profile feature, allowing users to run separate instances of applications for work/personal separation. To present the list of clonable applications, we must query installed packages.

**Why alternatives are insufficient:**
- `<queries>` declarations cannot cover all possible user-installed apps since we don't know which apps the user will want to clone
- The user experience would be severely degraded without full package visibility
- This is core functionality, not optional or supplementary

### Data Handling

- Package information is used only for display in the app picker UI
- No package data is transmitted to external servers
- No package data is shared with third parties

## Content Rating

- **IARC Classification:** Utility / Productivity
- **Target Audience:** Adults (18+)
- **Content:** No user-generated content, no social features

## Store Listing Positioning

TitanClone is positioned as a **productivity and work-life balance tool**:
- "Run separate app instances for work and personal use"
- "Test apps with different accounts on one device"
- "Developer tool for multi-account testing"

## Release Track Strategy

1. **Internal Testing** — Team testing (up to 100 testers)
2. **Closed Alpha** — Limited external testing (up to 100 testers)
3. **Open Beta** — Public beta testing
4. **Production** — General availability

## AAB Requirements

- App signed with Play App Signing
- Uploaded as Android App Bundle (AAB) format
- Supports arm64-v8a and armeabi-v7a architectures
- Target SDK: 35 (Android 15)
- Min SDK: 29 (Android 10)

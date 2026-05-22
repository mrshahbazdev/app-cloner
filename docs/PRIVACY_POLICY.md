# TitanClone Privacy Policy

**Last Updated:** May 2026

## Overview

TitanClone is a productivity tool that enables users to run multiple isolated instances of Android applications on a single device. This privacy policy describes how TitanClone handles user data.

## Data Collection

**TitanClone does NOT collect, transmit, or store any user data on external servers.**

All data is stored locally on the user's device:
- Clone configurations and virtual profiles
- Application data for each clone instance
- User preferences and settings

## Data Storage

All clone data is stored in the app's private internal storage directory, which is:
- Encrypted using AES-256-CBC with device-specific keys
- Not included in Android backup (cloud or device-to-device)
- Accessible only to the TitanClone application
- Completely deleted when the app is uninstalled

## Virtual Identifiers

TitanClone generates virtual device identifiers for each clone to provide isolation between instances. These identifiers are:
- Generated locally on the device
- Used solely for application isolation purposes
- Not transmitted to any external server
- Not used for tracking, advertising, or fraud

## Permissions

TitanClone requires the following permissions:

| Permission | Purpose |
|------------|---------|
| `QUERY_ALL_PACKAGES` | Discover installed apps available for cloning |
| `FOREGROUND_SERVICE` | Keep clone processes running in background |
| `POST_NOTIFICATIONS` | Show clone notifications and service status |
| `WAKE_LOCK` | Prevent clone processes from being killed |
| `RECEIVE_BOOT_COMPLETED` | Restart auto-start clones after device reboot |
| `INTERNET` | Required by cloned applications for network access |

## Third-Party Services

TitanClone does not integrate with any third-party analytics, advertising, or tracking services.

Cloned applications may independently communicate with their own servers according to their own privacy policies. TitanClone does not intercept, modify, or monitor this communication.

## Data Deletion

Users can delete all data for any clone at any time through the app interface. When a clone is deleted:
- All application data for that clone is securely wiped
- Virtual profiles and identifiers are permanently removed
- The deletion is irreversible

Uninstalling TitanClone removes all clone data from the device.

## Children's Privacy

TitanClone is not directed at children under 13. We do not knowingly collect data from children.

## Changes to This Policy

We may update this privacy policy from time to time. Changes will be reflected in the "Last Updated" date above.

## Contact

For privacy-related inquiries, please open an issue on our GitHub repository.

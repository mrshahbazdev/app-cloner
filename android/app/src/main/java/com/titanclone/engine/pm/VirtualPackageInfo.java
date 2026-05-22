package com.titanclone.engine.pm;

import java.util.List;

/**
 * Stores parsed package metadata for a virtually installed clone.
 * Provides all data that the virtual PackageManager returns to clone apps.
 */
public class VirtualPackageInfo {

    public final String packageName;
    public final int userId;
    public final String appName;
    public final String versionName;
    public final long versionCode;
    public final List<String> apkPaths;
    public final List<String> activities;
    public final List<String> services;
    public final List<String> receivers;
    public final List<String> providers;
    public final List<String> permissions;
    public final int targetSdkVersion;
    public final int minSdkVersion;
    public final long totalSizeBytes;
    public final long installedAt;

    public VirtualPackageInfo(
            String packageName, int userId, String appName,
            String versionName, long versionCode,
            List<String> apkPaths,
            List<String> activities, List<String> services,
            List<String> receivers, List<String> providers,
            List<String> permissions,
            int targetSdkVersion, int minSdkVersion,
            long totalSizeBytes, long installedAt) {
        this.packageName = packageName;
        this.userId = userId;
        this.appName = appName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.apkPaths = apkPaths;
        this.activities = activities;
        this.services = services;
        this.receivers = receivers;
        this.providers = providers;
        this.permissions = permissions;
        this.targetSdkVersion = targetSdkVersion;
        this.minSdkVersion = minSdkVersion;
        this.totalSizeBytes = totalSizeBytes;
        this.installedAt = installedAt;
    }

    public String getCloneId() {
        return packageName + "_user" + userId;
    }
}

package com.titanclone.engine.pm;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Virtual PackageManager — maintains registry of all virtually installed
 * packages. When a clone app queries PackageManager, responses are built
 * from this registry instead of the real system PM.
 *
 * Thread-safe: all operations are synchronized on the packages map.
 */
public class VirtualPackageManager {

    private static final String TAG = "VirtualPM";

    // Key: "packageName:userId" -> VirtualPackageInfo
    private final Map<String, VirtualPackageInfo> packages = new HashMap<>();

    // Key: packageName -> list of userIds that have this package installed
    private final Map<String, List<Integer>> packageUsers = new HashMap<>();

    /**
     * Register a newly installed virtual package.
     */
    public synchronized void registerPackage(VirtualPackageInfo info) {
        String key = makeKey(info.packageName, info.userId);
        packages.put(key, info);

        packageUsers.computeIfAbsent(info.packageName, k -> new ArrayList<>());
        List<Integer> users = packageUsers.get(info.packageName);
        if (!users.contains(info.userId)) {
            users.add(info.userId);
        }

        Log.i(TAG, "Registered: " + info.packageName + " user=" + info.userId
                + " (" + info.appName + " v" + info.versionName + ")");
    }

    /**
     * Unregister a virtual package for a specific user.
     */
    public synchronized void unregisterPackage(String packageName, int userId) {
        String key = makeKey(packageName, userId);
        packages.remove(key);

        List<Integer> users = packageUsers.get(packageName);
        if (users != null) {
            users.remove(Integer.valueOf(userId));
            if (users.isEmpty()) {
                packageUsers.remove(packageName);
            }
        }

        Log.i(TAG, "Unregistered: " + packageName + " user=" + userId);
    }

    /**
     * Get package info for a specific package and user.
     */
    public synchronized VirtualPackageInfo getPackageInfo(String packageName, int userId) {
        return packages.get(makeKey(packageName, userId));
    }

    /**
     * Get all virtual packages for a specific user.
     */
    public synchronized List<VirtualPackageInfo> getInstalledPackages(int userId) {
        List<VirtualPackageInfo> result = new ArrayList<>();
        for (VirtualPackageInfo info : packages.values()) {
            if (info.userId == userId) {
                result.add(info);
            }
        }
        return result;
    }

    /**
     * Get all virtual packages across all users.
     */
    public synchronized List<VirtualPackageInfo> getAllPackages() {
        return new ArrayList<>(packages.values());
    }

    /**
     * Check if a package is installed for any user.
     */
    public synchronized boolean isPackageInstalledForAnyUser(String packageName) {
        List<Integer> users = packageUsers.get(packageName);
        return users != null && !users.isEmpty();
    }

    /**
     * Check if a specific package+user combination is installed.
     */
    public synchronized boolean isPackageInstalled(String packageName, int userId) {
        return packages.containsKey(makeKey(packageName, userId));
    }

    /**
     * Get all user IDs that have a specific package installed.
     */
    public synchronized List<Integer> getUsersForPackage(String packageName) {
        List<Integer> users = packageUsers.get(packageName);
        return users != null ? new ArrayList<>(users) : new ArrayList<>();
    }

    /**
     * Resolve which activities can handle a given action for a user.
     */
    public synchronized List<String> queryIntentActivities(String action, int userId) {
        List<String> results = new ArrayList<>();
        for (VirtualPackageInfo info : packages.values()) {
            if (info.userId == userId) {
                results.addAll(info.activities);
            }
        }
        return results;
    }

    /**
     * Get total count of installed clones.
     */
    public synchronized int getInstalledCount() {
        return packages.size();
    }

    private String makeKey(String packageName, int userId) {
        return packageName + ":" + userId;
    }
}

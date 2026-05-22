package com.titanclone.engine.stubs;

import android.util.Log;

/**
 * Virtual PackageManager proxy — returns virtual package info,
 * spoofed signatures, and isolated component lists per clone.
 *
 * TODO: Implement IPackageManager proxy.
 * Critical for Play Store cloning: must return correct Play Store
 * signature so GMS considers the cloned Play Store legitimate.
 */
public class PackageManagerStub {

    private static final String TAG = "PackageManagerStub";

    /**
     * Hook getPackageInfo to return virtual package details.
     */
    public static void hookGetPackageInfo() {
        // TODO: Return virtual versionCode, versionName, signatures
        // for cloned packages. Play Store signature must match real.
        Log.d(TAG, "PackageManager getPackageInfo hook registered");
    }

    /**
     * Hook getInstallerPackageName to return "com.android.vending"
     * for apps installed through cloned Play Store.
     */
    public static void hookGetInstallerPackageName() {
        // TODO: Apps installed via clone Play Store must report
        // their installer as "com.android.vending" (real Play Store package).
        Log.d(TAG, "InstallerPackageName hook registered");
    }

    /**
     * Hook queryIntentActivities to return only clone-visible components.
     */
    public static void hookQueryIntentActivities() {
        // TODO: Filter results to prevent cross-clone component leakage.
        Log.d(TAG, "QueryIntentActivities hook registered");
    }
}

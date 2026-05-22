package com.titanclone.engine.stubs;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Virtual PackageManager proxy — returns virtual package info,
 * spoofed signatures, and isolated component lists per clone.
 *
 * Critical for Play Store cloning: must return correct Play Store
 * signature so GMS considers the cloned Play Store legitimate.
 *
 * Hooked methods:
 * - getPackageInfo() — return virtual version/signatures
 * - queryIntentActivities() — return only clone-visible components
 * - getInstalledPackages() — return virtual package list
 * - resolveActivity() — resolve to clone's stub Activities
 * - getInstallerPackageName() — return "com.android.vending"
 * - getPackageUid() — return virtual UID
 */
public class PackageManagerStub extends MethodInvocationProxy {

    private static final String TAG = "PMStub";
    private static final String PLAY_STORE_PACKAGE = "com.android.vending";

    @Override
    public String getName() {
        return "PackageManager";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("getPackageInfo", this::handleGetPackageInfo);
        addMethodHandler("getApplicationInfo", this::handleGetApplicationInfo);
        addMethodHandler("queryIntentActivities", this::handleQueryIntentActivities);
        addMethodHandler("getInstalledPackages", this::handleGetInstalledPackages);
        addMethodHandler("getInstalledApplications", this::handleGetInstalledApplications);
        addMethodHandler("resolveActivity", this::handleResolveActivity);
        addMethodHandler("resolveService", this::handleResolveService);
        addMethodHandler("getInstallerPackageName", this::handleGetInstallerPackageName);
        addMethodHandler("getPackageUid", this::handleGetPackageUid);
        addMethodHandler("checkPermission", this::handleCheckPermission);

        // TODO: Use reflection to get IPackageManager:
        //   IBinder binder = ServiceManager.getService("package");
        //   IPackageManager original = IPackageManager.Stub.asInterface(binder);
        //   Replace ActivityThread.sPackageManager

        markInjected();
    }

    private Object handleGetPackageInfo(Object original, Method method, Object[] args)
            throws Throwable {
        String packageName = (String) args[0];
        Log.d(TAG, "getPackageInfo: " + packageName);

        // TODO: If the package is virtually installed, return VirtualPackageInfo
        // converted to a real PackageInfo object with correct signatures
        return method.invoke(original, args);
    }

    private Object handleGetApplicationInfo(Object original, Method method, Object[] args)
            throws Throwable {
        String packageName = (String) args[0];
        // TODO: Return virtual ApplicationInfo with redirected data directories
        return method.invoke(original, args);
    }

    private Object handleQueryIntentActivities(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Filter results to only show clone-visible components
        // Add virtual package activities to results
        return method.invoke(original, args);
    }

    private Object handleGetInstalledPackages(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Return list of packages visible to this clone
        // Include both virtually installed packages and system packages
        return method.invoke(original, args);
    }

    private Object handleGetInstalledApplications(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Similar to getInstalledPackages but returns ApplicationInfo list
        return method.invoke(original, args);
    }

    private Object handleResolveActivity(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Resolve to clone's stub Activity if target is a virtual package
        return method.invoke(original, args);
    }

    private Object handleResolveService(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Resolve to clone's stub Service
        return method.invoke(original, args);
    }

    private Object handleGetInstallerPackageName(Object original, Method method, Object[] args)
            throws Throwable {
        // For apps installed via cloned Play Store, return real Play Store package
        String packageName = (String) args[0];
        Log.d(TAG, "getInstallerPackageName: " + packageName);
        // TODO: Check if package was installed via virtual Play Store
        // If so, return PLAY_STORE_PACKAGE
        return method.invoke(original, args);
    }

    private Object handleGetPackageUid(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Return virtual UID for clone packages
        return method.invoke(original, args);
    }

    private Object handleCheckPermission(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Check permission against virtual permission registry
        return method.invoke(original, args);
    }
}

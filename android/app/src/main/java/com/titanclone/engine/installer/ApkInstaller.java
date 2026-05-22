package com.titanclone.engine.installer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.titanclone.engine.storage.VirtualStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles APK extraction, parsing, and virtual installation into the sandbox.
 *
 * TODO: Implement full APK parsing (split APKs, native libs extraction,
 * dex optimization).
 */
public class ApkInstaller {

    private static final String TAG = "ApkInstaller";

    private final Context context;
    private final VirtualStorage storage;

    public ApkInstaller(Context context) {
        this.context = context;
        this.storage = new VirtualStorage(context);
    }

    /**
     * Extract and install an APK into the virtual sandbox.
     *
     * @param packageName The package to install
     * @param userId      The virtual user ID
     * @return true if installation succeeded
     */
    public boolean installPackage(String packageName, int userId) {
        try {
            // Step 1: Find source APK
            String sourceApkPath = getSourceApkPath(packageName);
            if (sourceApkPath == null) {
                Log.e(TAG, "Source APK not found for: " + packageName);
                return false;
            }

            // Step 2: Copy APK to virtual packages directory
            File cachedApk = storage.getCachedApk(packageName);
            if (!cachedApk.exists()) {
                copyFile(new File(sourceApkPath), cachedApk);
                Log.d(TAG, "APK cached: " + cachedApk.getAbsolutePath());
            }

            // Step 3: Create clone data directory
            File cloneDir = storage.getCloneDataDir(packageName, userId);
            Log.d(TAG, "Clone data dir created: " + cloneDir.getAbsolutePath());

            // TODO: Step 4: Parse APK, extract native libs, optimize dex
            // TODO: Step 5: Register with virtual PackageManager
            // TODO: Step 6: Set up ContentProvider stubs

            Log.i(TAG, "Package installed: " + packageName + " user=" + userId);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Installation failed: " + packageName, e);
            return false;
        }
    }

    /**
     * Get the source APK path for an installed package.
     */
    private String getSourceApkPath(String packageName) {
        try {
            PackageInfo info = context.getPackageManager()
                    .getPackageInfo(packageName, 0);
            return info.applicationInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Uninstall a package from the virtual sandbox.
     */
    public boolean uninstallPackage(String packageName, int userId) {
        boolean dataDeleted = storage.deleteCloneData(packageName, userId);
        File cachedApk = storage.getCachedApk(packageName);
        boolean apkDeleted = !cachedApk.exists() || cachedApk.delete();
        return dataDeleted && apkDeleted;
    }

    private void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}

package com.titanclone.engine.installer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.titanclone.engine.pm.VirtualPackageManager;
import com.titanclone.engine.pm.VirtualPackageInfo;
import com.titanclone.engine.storage.VirtualStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the full virtual installation pipeline:
 * 1. Locate source APK(s) including splits
 * 2. Copy APKs to virtual storage via FileChannel (fast)
 * 3. Parse manifest and extract component info
 * 4. Register in VirtualPackageManager
 * 5. Create isolated data directories
 * 6. Extract native libraries if needed
 */
public class VirtualPackageInstaller {

    private static final String TAG = "VirtualPkgInstaller";
    private static final int BUFFER_SIZE = 1024 * 1024; // 1MB

    private final Context context;
    private final VirtualStorage storage;
    private final VirtualPackageManager virtualPm;

    public VirtualPackageInstaller(Context context, VirtualPackageManager virtualPm) {
        this.context = context;
        this.storage = new VirtualStorage(context);
        this.virtualPm = virtualPm;
    }

    /**
     * Full installation pipeline for cloning a package.
     */
    public InstallResult installPackage(String packageName, int userId) {
        Log.i(TAG, "Installing " + packageName + " for user " + userId);
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Get source package info
            PackageInfo sourceInfo = getSourcePackageInfo(packageName);
            if (sourceInfo == null) {
                return InstallResult.error("Package not found: " + packageName);
            }

            // Step 2: Check ABI compatibility
            if (!checkAbiCompatibility(sourceInfo)) {
                return InstallResult.error("Incompatible CPU architecture");
            }

            // Step 3: Copy base APK + splits to virtual storage
            CopyResult copyResult = copyApks(packageName, sourceInfo);
            if (!copyResult.success) {
                return InstallResult.error("APK copy failed: " + copyResult.error);
            }

            // Step 4: Create isolated data directory structure
            createDataDirectories(packageName, userId);

            // Step 5: Extract native libraries
            extractNativeLibraries(packageName, sourceInfo);

            // Step 6: Register in virtual PackageManager
            VirtualPackageInfo vpInfo = buildVirtualPackageInfo(
                    packageName, userId, sourceInfo, copyResult);
            virtualPm.registerPackage(vpInfo);

            long elapsed = System.currentTimeMillis() - startTime;
            Log.i(TAG, "Installed " + packageName + " in " + elapsed + "ms");
            return InstallResult.success(vpInfo);

        } catch (Exception e) {
            Log.e(TAG, "Installation failed: " + packageName, e);
            return InstallResult.error(e.getMessage());
        }
    }

    /**
     * Uninstall a cloned package and clean up all data.
     */
    public boolean uninstallPackage(String packageName, int userId) {
        try {
            // Remove from virtual PM
            virtualPm.unregisterPackage(packageName, userId);

            // Delete clone data
            storage.deleteCloneData(packageName, userId);

            // Delete cached APK only if no other clones use it
            if (!virtualPm.isPackageInstalledForAnyUser(packageName)) {
                deleteCachedApks(packageName);
            }

            Log.i(TAG, "Uninstalled " + packageName + " user=" + userId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Uninstall failed: " + packageName, e);
            return false;
        }
    }

    private PackageInfo getSourcePackageInfo(String packageName) {
        try {
            int flags = PackageManager.GET_META_DATA
                    | PackageManager.GET_ACTIVITIES
                    | PackageManager.GET_SERVICES
                    | PackageManager.GET_RECEIVERS
                    | PackageManager.GET_PROVIDERS
                    | PackageManager.GET_PERMISSIONS
                    | PackageManager.GET_SIGNING_CERTIFICATES;
            return context.getPackageManager().getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private boolean checkAbiCompatibility(PackageInfo info) {
        ApplicationInfo appInfo = info.applicationInfo;
        if (appInfo == null || appInfo.nativeLibraryDir == null) {
            return true; // No native code, compatible with all
        }

        File nativeDir = new File(appInfo.nativeLibraryDir);
        if (!nativeDir.exists() || !nativeDir.isDirectory()) {
            return true;
        }

        String[] supportedAbis = Build.SUPPORTED_ABIS;
        String nativeAbi = nativeDir.getName(); // Usually "arm64", "arm", "x86", "x86_64"

        String mappedAbi = nativeAbi;
        if ("arm64".equals(nativeAbi)) mappedAbi = "arm64-v8a";
        else if ("arm".equals(nativeAbi)) mappedAbi = "armeabi-v7a"; // or armeabi

        for (String abi : supportedAbis) {
            if (abi.equals(nativeAbi) || abi.equals(mappedAbi)) return true;
            // arm64-v8a devices can run armeabi-v7a code
            if ("arm64-v8a".equals(abi) && ("armeabi-v7a".equals(mappedAbi) || "armeabi".equals(mappedAbi))) return true;
        }

        Log.w(TAG, "ABI mismatch: app=" + nativeAbi + " (mapped=" + mappedAbi + ") device=" + Arrays.toString(supportedAbis));
        return false;
    }

    private CopyResult copyApks(String packageName, PackageInfo info) {
        ApplicationInfo appInfo = info.applicationInfo;
        List<String> copiedPaths = new ArrayList<>();

        try {
            File pkgDir = storage.getPackageApkDir(packageName);

            // Copy base APK
            File baseApk = new File(pkgDir, "base.apk");
            if (!baseApk.exists()) {
                copyFileChannel(new File(appInfo.sourceDir), baseApk);
            }
            copiedPaths.add(baseApk.getAbsolutePath());

            // Copy split APKs (App Bundles)
            if (appInfo.splitSourceDirs != null) {
                for (int i = 0; i < appInfo.splitSourceDirs.length; i++) {
                    String splitName = "split_" + i + ".apk";
                    if (appInfo.splitNames != null && i < appInfo.splitNames.length) {
                        splitName = appInfo.splitNames[i] + ".apk";
                    }
                    File splitApk = new File(pkgDir, splitName);
                    if (!splitApk.exists()) {
                        copyFileChannel(new File(appInfo.splitSourceDirs[i]), splitApk);
                    }
                    copiedPaths.add(splitApk.getAbsolutePath());
                }
            }

            long totalSize = 0;
            for (String path : copiedPaths) {
                totalSize += new File(path).length();
            }

            return new CopyResult(true, copiedPaths, totalSize, null);

        } catch (IOException e) {
            Log.e(TAG, "APK copy failed for " + packageName, e);
            return new CopyResult(false, copiedPaths, 0, e.getMessage());
        }
    }

    private void createDataDirectories(String packageName, int userId) {
        File dataDir = storage.getCloneDataDir(packageName, userId);
        new File(dataDir, "cache").mkdirs();
        new File(dataDir, "files").mkdirs();
        new File(dataDir, "databases").mkdirs();
        new File(dataDir, "shared_prefs").mkdirs();
        new File(dataDir, "lib").mkdirs();
        new File(dataDir, "code_cache").mkdirs();
        new File(dataDir, "no_backup").mkdirs();
        Log.d(TAG, "Data directories created: " + dataDir.getAbsolutePath());
    }

    private void extractNativeLibraries(String packageName, PackageInfo info) {
        ApplicationInfo appInfo = info.applicationInfo;
        if (appInfo == null || appInfo.nativeLibraryDir == null) return;

        File sourceLibDir = new File(appInfo.nativeLibraryDir);
        if (!sourceLibDir.exists() || !sourceLibDir.isDirectory()) return;

        File targetLibDir = new File(storage.getPackageApkDir(packageName), "lib");
        targetLibDir.mkdirs();

        File[] libs = sourceLibDir.listFiles();
        if (libs == null) return;

        for (File lib : libs) {
            try {
                File target = new File(targetLibDir, lib.getName());
                if (!target.exists()) {
                    copyFileChannel(lib, target);
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed to copy native lib: " + lib.getName(), e);
            }
        }
    }

    private VirtualPackageInfo buildVirtualPackageInfo(
            String packageName, int userId,
            PackageInfo sourceInfo, CopyResult copyResult) {

        ApplicationInfo appInfo = sourceInfo.applicationInfo;

        long versionCode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            versionCode = sourceInfo.getLongVersionCode();
        } else {
            versionCode = sourceInfo.versionCode;
        }

        List<String> activities = new ArrayList<>();
        if (sourceInfo.activities != null) {
            for (var a : sourceInfo.activities) activities.add(a.name);
        }
        List<String> services = new ArrayList<>();
        if (sourceInfo.services != null) {
            for (var s : sourceInfo.services) services.add(s.name);
        }
        List<String> receivers = new ArrayList<>();
        if (sourceInfo.receivers != null) {
            for (var r : sourceInfo.receivers) receivers.add(r.name);
        }
        List<String> providers = new ArrayList<>();
        if (sourceInfo.providers != null) {
            for (var p : sourceInfo.providers) providers.add(p.name);
        }
        List<String> permissions = new ArrayList<>();
        if (sourceInfo.requestedPermissions != null) {
            permissions.addAll(Arrays.asList(sourceInfo.requestedPermissions));
        }

        return new VirtualPackageInfo(
                packageName,
                userId,
                appInfo != null ? context.getPackageManager()
                        .getApplicationLabel(appInfo).toString() : packageName,
                sourceInfo.versionName,
                versionCode,
                copyResult.copiedPaths,
                activities,
                services,
                receivers,
                providers,
                permissions,
                appInfo != null ? appInfo.targetSdkVersion : Build.VERSION.SDK_INT,
                appInfo != null ? appInfo.minSdkVersion : 29,
                copyResult.totalSize,
                System.currentTimeMillis()
        );
    }

    private void deleteCachedApks(String packageName) {
        File pkgDir = storage.getPackageApkDir(packageName);
        if (pkgDir.exists()) {
            deleteRecursive(pkgDir);
        }
    }

    private void copyFileChannel(File source, File dest) throws IOException {
        dest.getParentFile().mkdirs();
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest);
             FileChannel srcChannel = fis.getChannel();
             FileChannel dstChannel = fos.getChannel()) {
            long size = srcChannel.size();
            long pos = 0;
            while (pos < size) {
                long transferred = srcChannel.transferTo(pos, BUFFER_SIZE, dstChannel);
                pos += transferred;
            }
        }
    }

    private void deleteRecursive(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            File[] children = fileOrDir.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursive(child);
            }
        }
        fileOrDir.delete();
    }

    public static class InstallResult {
        public final boolean success;
        public final String error;
        public final VirtualPackageInfo packageInfo;

        private InstallResult(boolean success, String error, VirtualPackageInfo info) {
            this.success = success;
            this.error = error;
            this.packageInfo = info;
        }

        static InstallResult success(VirtualPackageInfo info) {
            return new InstallResult(true, null, info);
        }

        static InstallResult error(String message) {
            return new InstallResult(false, message, null);
        }
    }

    private static class CopyResult {
        final boolean success;
        final List<String> copiedPaths;
        final long totalSize;
        final String error;

        CopyResult(boolean success, List<String> paths, long size, String error) {
            this.success = success;
            this.copiedPaths = paths;
            this.totalSize = size;
            this.error = error;
        }
    }
}

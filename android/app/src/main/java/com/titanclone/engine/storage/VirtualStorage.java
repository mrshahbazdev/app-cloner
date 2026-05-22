package com.titanclone.engine.storage;

import android.content.Context;
import android.util.Log;

import com.titanclone.engine.core.EngineConfig;

import java.io.File;

/**
 * Manages isolated storage directories for each clone.
 * Each clone gets its own data directory under:
 * /data/data/com.titanclone/virtual/user_{userId}/{packageName}/
 *
 * TODO: Implement file IO redirection via JNI syscall hooks.
 */
public class VirtualStorage {

    private static final String TAG = "VirtualStorage";

    private final Context context;
    private final File virtualRoot;

    public VirtualStorage(Context context) {
        this.context = context;
        this.virtualRoot = new File(context.getFilesDir(), EngineConfig.VIRTUAL_ROOT);
        if (!virtualRoot.exists()) {
            virtualRoot.mkdirs();
        }
    }

    /**
     * Get the isolated data directory for a specific clone.
     */
    public File getCloneDataDir(String packageName, int userId) {
        File userDir = new File(virtualRoot, EngineConfig.USER_DIR_PREFIX + userId);
        File cloneDir = new File(userDir, packageName);
        if (!cloneDir.exists()) {
            cloneDir.mkdirs();
        }
        return cloneDir;
    }

    /**
     * Get the APK cache directory for extracted APKs.
     */
    public File getPackagesDir() {
        File packagesDir = new File(virtualRoot, EngineConfig.PACKAGES_DIR);
        if (!packagesDir.exists()) {
            packagesDir.mkdirs();
        }
        return packagesDir;
    }

    /**
     * Get the cached APK file for a package.
     */
    public File getCachedApk(String packageName) {
        return new File(getPackagesDir(), packageName + ".apk");
    }

    /**
     * Delete all data for a clone.
     */
    public boolean deleteCloneData(String packageName, int userId) {
        File cloneDir = getCloneDataDir(packageName, userId);
        boolean success = deleteRecursive(cloneDir);
        Log.d(TAG, "Deleted clone data for " + packageName + " user=" + userId + ": " + success);
        return success;
    }

    /**
     * Get total storage used by all clones.
     */
    public long getTotalStorageUsed() {
        return dirSize(virtualRoot);
    }

    /**
     * Get storage used by a specific clone.
     */
    public long getCloneStorageUsed(String packageName, int userId) {
        return dirSize(getCloneDataDir(packageName, userId));
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }

    private long dirSize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += dirSize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        } else {
            size = dir.length();
        }
        return size;
    }
}

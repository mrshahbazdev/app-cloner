package com.titanclone.engine.storage;

import android.content.Context;
import android.util.Log;

import com.titanclone.engine.core.EngineConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Extended storage manager handling IO redirection rules,
 * ContentProvider isolation, SharedPreferences isolation,
 * SQLite database isolation, and external storage.
 *
 * Works with the C++ io_redirect.cpp layer to intercept
 * filesystem operations at the syscall level.
 */
public class VirtualStorageManager {

    private static final String TAG = "VirtualStorageMgr";

    private final Context context;
    private final VirtualStorage storage;

    // Cached redirect rules: realPath -> virtualPath
    private final Map<String, String> redirectRules = new HashMap<>();

    // Track which user/package combos have redirect rules installed
    private final Map<String, Boolean> installedRedirects = new HashMap<>();

    public VirtualStorageManager(Context context) {
        this.context = context;
        this.storage = new VirtualStorage(context);
    }

    /**
     * Install IO redirect rules for a clone.
     * Must be called before launching the clone process.
     */
    public void installRedirectRules(String packageName, int userId) {
        String key = packageName + ":" + userId;
        if (installedRedirects.containsKey(key)) return;

        File cloneDataDir = storage.getCloneDataDir(packageName, userId);

        // Redirect app's data directory
        String realDataPath = "/data/data/" + packageName;
        String virtualDataPath = cloneDataDir.getAbsolutePath();
        addRedirectRule(realDataPath, virtualDataPath);

        // Redirect app's user-specific data directory (multi-user Android)
        String realUserDataPath = "/data/user/0/" + packageName;
        addRedirectRule(realUserDataPath, virtualDataPath);

        // Redirect cache directory
        String realCachePath = "/data/data/" + packageName + "/cache";
        String virtualCachePath = new File(cloneDataDir, "cache").getAbsolutePath();
        addRedirectRule(realCachePath, virtualCachePath);

        // Redirect external files directory
        File externalDir = getCloneExternalDir(packageName, userId);
        String realExternalPath = "/storage/emulated/0/Android/data/" + packageName;
        addRedirectRule(realExternalPath, externalDir.getAbsolutePath());

        // Redirect external OBB directory
        File obbDir = getCloneObbDir(packageName, userId);
        String realObbPath = "/storage/emulated/0/Android/obb/" + packageName;
        addRedirectRule(realObbPath, obbDir.getAbsolutePath());

        installedRedirects.put(key, true);
        Log.i(TAG, "Redirect rules installed for " + packageName + " user=" + userId);
    }

    /**
     * Get SharedPreferences redirect path for a clone.
     */
    public File getCloneSharedPrefsDir(String packageName, int userId) {
        File dataDir = storage.getCloneDataDir(packageName, userId);
        File prefsDir = new File(dataDir, "shared_prefs");
        prefsDir.mkdirs();
        return prefsDir;
    }

    /**
     * Get database redirect path for a clone.
     */
    public File getCloneDatabasesDir(String packageName, int userId) {
        File dataDir = storage.getCloneDataDir(packageName, userId);
        File dbDir = new File(dataDir, "databases");
        dbDir.mkdirs();
        return dbDir;
    }

    /**
     * Get external files directory for a clone.
     */
    public File getCloneExternalDir(String packageName, int userId) {
        File externalBase = new File(storage.getCloneDataDir(packageName, userId), "external");
        externalBase.mkdirs();
        return externalBase;
    }

    /**
     * Get OBB directory for a clone.
     */
    public File getCloneObbDir(String packageName, int userId) {
        File obbBase = new File(storage.getCloneDataDir(packageName, userId), "obb");
        obbBase.mkdirs();
        return obbBase;
    }

    /**
     * Get ContentProvider authority redirect for a clone.
     * Each clone's ContentProvider gets a unique authority.
     */
    public String getRedirectedAuthority(String originalAuthority, int userId) {
        return EngineConfig.HOST_PACKAGE + ".virtual.u" + userId + "." + originalAuthority;
    }

    /**
     * Get all redirect rules as a map (real path -> virtual path).
     */
    public Map<String, String> getRedirectRules() {
        return new HashMap<>(redirectRules);
    }

    /**
     * Remove redirect rules for a clone (on stop/uninstall).
     */
    public void removeRedirectRules(String packageName, int userId) {
        String key = packageName + ":" + userId;
        installedRedirects.remove(key);

        String prefix = "/data/data/" + packageName;
        redirectRules.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));

        Log.d(TAG, "Redirect rules removed for " + packageName + " user=" + userId);
    }

    /**
     * Get total storage used by a clone in bytes.
     */
    public long getCloneStorageUsed(String packageName, int userId) {
        return storage.getCloneStorageUsed(packageName, userId);
    }

    /**
     * Clean up cache for a specific clone.
     */
    public void clearCloneCache(String packageName, int userId) {
        File cacheDir = new File(storage.getCloneDataDir(packageName, userId), "cache");
        if (cacheDir.exists()) {
            deleteRecursive(cacheDir);
            cacheDir.mkdirs();
        }
        File codeCacheDir = new File(storage.getCloneDataDir(packageName, userId), "code_cache");
        if (codeCacheDir.exists()) {
            deleteRecursive(codeCacheDir);
            codeCacheDir.mkdirs();
        }
    }

    private void addRedirectRule(String realPath, String virtualPath) {
        redirectRules.put(realPath, virtualPath);
        try {
            com.titanclone.engine.core.VirtualCore.get().nativeAddRedirectRule(realPath, virtualPath);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to propagate redirect rule to native JNI: " + realPath + " -> " + virtualPath, e);
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
}

package com.titanclone.engine.core;

import android.content.Context;
import android.util.Log;

import com.titanclone.engine.installer.VirtualPackageInstaller;
import com.titanclone.engine.ipc.BinderRouter;
import com.titanclone.engine.ipc.IntentRouter;
import com.titanclone.engine.pm.VirtualPackageManager;
import com.titanclone.engine.process.VirtualProcessManager;
import com.titanclone.engine.storage.VirtualStorage;
import com.titanclone.engine.storage.VirtualStorageManager;
import com.titanclone.engine.stubs.StubManager;

/**
 * Core engine singleton — manages the virtualization lifecycle.
 * Wires together all engine subsystems: package management,
 * process management, storage isolation, service proxying,
 * intent routing, and Binder IPC.
 *
 * Initialization order:
 * 1. doAttachBaseContext() — early init in Application.attachBaseContext()
 * 2. doCreate() — full init in Application.onCreate()
 */
public class VirtualCore {

    private static final String TAG = "VirtualCore";
    private static volatile VirtualCore sInstance;

    static {
        try {
            System.loadLibrary("titan_clone_native");
            Log.i(TAG, "Successfully loaded native virtualization engine (libtitan_clone_native.so)");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native virtualization engine library (libtitan_clone_native.so)", e);
        }
    }

    private Context context;
    private boolean initialized = false;

    // Subsystems
    private VirtualPackageManager packageManager;
    private VirtualProcessManager processManager;
    private VirtualStorage storage;
    private VirtualStorageManager storageManager;
    private VirtualPackageInstaller installer;
    private IntentRouter intentRouter;
    private BinderRouter binderRouter;
    private StubManager stubManager;

    private VirtualCore() {}

    public static VirtualCore get() {
        if (sInstance == null) {
            synchronized (VirtualCore.class) {
                if (sInstance == null) {
                    sInstance = new VirtualCore();
                }
            }
        }
        return sInstance;
    }

    /**
     * Initialize the virtualization engine.
     * Must be called in Application.attachBaseContext().
     */
    public void doAttachBaseContext(Context context) {
        Context appCtx = context.getApplicationContext();
        this.context = appCtx != null ? appCtx : context;
        Log.d(TAG, "Engine attached to base context");
    }

    /**
     * Complete engine initialization.
     * Must be called in Application.onCreate().
     */
    public void doCreate() {
        if (initialized) return;
        try {
            // Initialize subsystems
            storage = new VirtualStorage(context);
            storageManager = new VirtualStorageManager(context);
            packageManager = new VirtualPackageManager();
            processManager = VirtualProcessManager.get();
            installer = new VirtualPackageInstaller(context, packageManager);
            intentRouter = new IntentRouter(packageManager);
            binderRouter = new BinderRouter();

            // Initialize and inject system service stubs
            stubManager = StubManager.get();
            stubManager.registerStubs();

            // Listen for process deaths
            processManager.setProcessStateListener((cloneId, processIndex) -> {
                Log.w(TAG, "Clone process died: " + cloneId + " :p" + processIndex);
                storageManager.removeRedirectRules(
                        cloneId.split("_user")[0],
                        Integer.parseInt(cloneId.split("_user")[1]));
                binderRouter.unregisterCloneServices(cloneId);
            });

            // Initialize native JNI virtualization modules
            try {
                String virtualRootPath = storage.getVirtualRoot().getAbsolutePath();
                nativeInitIORedirect(virtualRootPath);
                nativeInitPropertyHook();
                nativeInitBinderHook();
                nativeInitMapsFilter();
                Log.i(TAG, "Native virtualization hooks initialized successfully in pid=" + android.os.Process.myPid());
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Native method call failed - JNI bindings missing or library not loaded", e);
            }

            initialized = true;
            Log.i(TAG, "Engine initialized: "
                    + stubManager.getStubCount() + " stubs, "
                    + processManager.getAvailableSlots() + " process slots");

        } catch (Throwable e) {
            Log.e(TAG, "Engine initialization failed", e);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Context getContext() {
        return context;
    }

    /**
     * Install a package into the virtual environment for a given user.
     */
    public boolean installPackageAsUser(String packageName, int userId) {
        if (!initialized) return false;
        VirtualPackageInstaller.InstallResult result =
                installer.installPackage(packageName, userId);
        if (!result.success) {
            Log.e(TAG, "VirtualCore internal install failed for " + packageName + ": " + result.error);
        }
        return result.success;
    }

    /**
     * Launch a cloned app.
     */
    public boolean launchApp(String packageName, int userId) {
        if (!initialized) return false;

        String cloneId = packageName + "_user" + userId;

        // Check if already running
        if (processManager.isProcessAlive(cloneId)) {
            processManager.setProcessPriority(cloneId, true);
            Log.d(TAG, "Clone already running, bringing to foreground: " + cloneId);
            return true;
        }

        // Install IO redirect rules
        storageManager.installRedirectRules(packageName, userId);

        // Allocate process slot
        VirtualProcessManager.CloneProcessRecord record =
                processManager.allocateProcess(cloneId, packageName, userId);
        if (record == null) {
            Log.e(TAG, "No process slots available for " + cloneId);
            return false;
        }

        // Set intent router context
        intentRouter.setCurrentUserId(userId);

        // Inject system service stubs in clone process
        stubManager.injectAll();

        Log.i(TAG, "Launched clone: " + cloneId + " in :p" + record.processIndex);
        return true;
    }

    /**
     * Stop a running clone gracefully.
     */
    public boolean killApp(String packageName, int userId) {
        if (!initialized) return false;

        String cloneId = packageName + "_user" + userId;
        storageManager.removeRedirectRules(packageName, userId);
        binderRouter.unregisterCloneServices(cloneId);
        processManager.releaseProcess(cloneId);

        Log.d(TAG, "Killed clone: " + cloneId);
        return true;
    }

    /**
     * Force-stop a clone process.
     */
    public boolean forceStopApp(String packageName, int userId) {
        if (!initialized) return false;

        String cloneId = packageName + "_user" + userId;
        processManager.forceStop(cloneId);
        storageManager.removeRedirectRules(packageName, userId);
        binderRouter.unregisterCloneServices(cloneId);
        return true;
    }

    /**
     * Uninstall a clone and clean up its data.
     */
    public boolean uninstallPackageAsUser(String packageName, int userId) {
        if (!initialized) return false;

        // Stop the clone first if running
        killApp(packageName, userId);

        return installer.uninstallPackage(packageName, userId);
    }

    /**
     * Check if a clone is currently running.
     */
    public boolean isAppRunning(String packageName, int userId) {
        String cloneId = packageName + "_user" + userId;
        return processManager.isProcessAlive(cloneId);
    }

    // Subsystem getters
    public VirtualPackageManager getPackageManager() { return packageManager; }
    public VirtualProcessManager getProcessManager() { return processManager; }
    public VirtualStorage getStorage() { return storage; }
    public VirtualStorageManager getStorageManager() { return storageManager; }
    public VirtualPackageInstaller getInstaller() { return installer; }
    public IntentRouter getIntentRouter() { return intentRouter; }
    public BinderRouter getBinderRouter() { return binderRouter; }
    public StubManager getStubManager() { return stubManager; }

    // =========================================================================
    // JNI Native Methods (implemented in C++ under cpp/)
    // =========================================================================
    public native void nativeInitIORedirect(String virtualRoot);
    public native void nativeAddRedirectRule(String fromPath, String toPath);
    public native String nativeRedirectPath(String originalPath);
    
    public native boolean nativeInitBinderHook();
    public native void nativeSetBinderIdentity(int uid, int pid);
    
    public native void nativeOptimizeMemory();
    public native long nativeGetNativeHeapSize();
    public native long nativeGetNativeHeapFreeSize();
    public native void nativeTrimMemory();
    
    public native void nativeInitMapsFilter();
    public native void nativeAddFilterPattern(String pattern);
    public native boolean nativeShouldFilterLine(String line);
    public native String nativeGetFilteredMaps();
    public native void nativeSetFilterPatterns(String[] patterns);
    
    public native void nativeInitPropertyHook();
    public native void nativeSetProperty(String name, String value);
    public native void nativeSetProperties(String[] keys, String[] values);
    public native String nativeGetProperty(String name);
    public native int nativeGetSpoofedPropertyCount();
    public native void nativeClearProperties();
}

package com.titanclone.engine.stubs;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Central manager for all system service proxies.
 * Initializes and injects all stubs during engine startup.
 *
 * Injection order matters — some stubs depend on others.
 * ActivityManager and PackageManager must be injected first.
 */
public class StubManager {

    private static final String TAG = "StubManager";
    private static volatile StubManager sInstance;

    private final List<MethodInvocationProxy> stubs = new ArrayList<>();
    private boolean initialized = false;

    private StubManager() {}

    public static StubManager get() {
        if (sInstance == null) {
            synchronized (StubManager.class) {
                if (sInstance == null) {
                    sInstance = new StubManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * Register all system service stubs.
     * Must be called before inject().
     */
    public void registerStubs() {
        if (initialized) return;

        // Critical stubs (must be first)
        stubs.add(new ActivityManagerStub());
        stubs.add(new PackageManagerStub());

        // Identity stubs
        stubs.add(new TelephonyStub());
        stubs.add(new WifiStub());
        stubs.add(new BluetoothStub());
        stubs.add(new SettingsStub());

        // Service isolation stubs
        stubs.add(new AccountManagerStub());
        stubs.add(new ContentResolverStub());
        stubs.add(new NotificationStub());
        stubs.add(new ClipboardStub());
        stubs.add(new LocationStub());
        stubs.add(new CameraStub());

        Log.i(TAG, "Registered " + stubs.size() + " service stubs");
        initialized = true;
    }

    /**
     * Inject all registered stubs into the current process.
     * Called when a clone process starts.
     */
    public void injectAll() {
        int success = 0;
        int failed = 0;

        for (MethodInvocationProxy stub : stubs) {
            try {
                stub.inject();
                success++;
            } catch (Throwable t) {
                Log.e(TAG, "Failed to inject " + stub.getName(), t);
                failed++;
            }
        }

        Log.i(TAG, "Stub injection complete: " + success + " ok, " + failed + " failed");
    }

    /**
     * Get a specific stub by class.
     */
    @SuppressWarnings("unchecked")
    public <T extends MethodInvocationProxy> T getStub(Class<T> clazz) {
        for (MethodInvocationProxy stub : stubs) {
            if (clazz.isInstance(stub)) {
                return (T) stub;
            }
        }
        return null;
    }

    /**
     * Get count of registered stubs.
     */
    public int getStubCount() {
        return stubs.size();
    }
}

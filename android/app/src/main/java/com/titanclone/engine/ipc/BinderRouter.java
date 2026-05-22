package com.titanclone.engine.ipc;

import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Routes Binder IPC transactions between clone processes and
 * the virtual server (:x).
 *
 * All Binder communications from clone processes pass through
 * the virtual server for proper virtualization. This ensures:
 * 1. System service calls are intercepted by stubs
 * 2. Cross-clone Binder calls are isolated
 * 3. External Binder calls are tagged with clone identity
 */
public class BinderRouter {

    private static final String TAG = "BinderRouter";

    // Map: service name -> virtual service Binder
    private final Map<String, IBinder> virtualServices = new HashMap<>();

    // Map: cloneId -> list of registered Binder interfaces
    private final Map<String, Map<String, IBinder>> cloneBinders = new HashMap<>();

    /**
     * Register a virtual system service that replaces the real one
     * for all clone processes.
     */
    public void registerVirtualService(String serviceName, IBinder binder) {
        virtualServices.put(serviceName, binder);
        Log.d(TAG, "Registered virtual service: " + serviceName);
    }

    /**
     * Lookup a service for a clone process.
     * Returns virtual service if registered, falls through to system otherwise.
     */
    public IBinder getService(String serviceName, String cloneId) {
        // Check for clone-specific override first
        Map<String, IBinder> cloneServices = cloneBinders.get(cloneId);
        if (cloneServices != null) {
            IBinder cloneBinder = cloneServices.get(serviceName);
            if (cloneBinder != null) return cloneBinder;
        }

        // Check for global virtual service
        IBinder virtualBinder = virtualServices.get(serviceName);
        if (virtualBinder != null) return virtualBinder;

        // Fall through to real ServiceManager (via reflection)
        return null;
    }

    /**
     * Register a clone-specific Binder service.
     */
    public void registerCloneService(String cloneId, String serviceName, IBinder binder) {
        cloneBinders.computeIfAbsent(cloneId, k -> new HashMap<>())
                .put(serviceName, binder);
    }

    /**
     * Unregister all services for a clone.
     */
    public void unregisterCloneServices(String cloneId) {
        cloneBinders.remove(cloneId);
        Log.d(TAG, "Unregistered services for clone: " + cloneId);
    }

    /**
     * Check if a Binder transaction should be routed through the virtual server.
     */
    public boolean shouldIntercept(String serviceName) {
        return virtualServices.containsKey(serviceName);
    }

    /**
     * Get count of registered virtual services.
     */
    public int getVirtualServiceCount() {
        return virtualServices.size();
    }
}

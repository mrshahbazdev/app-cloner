package com.titanclone.engine.stubs;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Virtual ActivityManager proxy — intercepts activity lifecycle calls
 * and routes them to the correct clone process.
 *
 * Hooked methods:
 * - startActivity() — wrap intent to route through stub Activity
 * - startService() — redirect to stub Service in clone process
 * - broadcastIntent() — isolate broadcasts per clone
 * - getRunningAppProcesses() — hide engine processes
 * - getAppTasks() — filter to show only clone's tasks
 */
public class ActivityManagerStub extends MethodInvocationProxy {

    private static final String TAG = "AMStub";

    @Override
    public String getName() {
        return "ActivityManager";
    }

    @Override
    public void inject() throws Throwable {
        // Register method handlers
        addMethodHandler("startActivity", this::handleStartActivity);
        addMethodHandler("startService", this::handleStartService);
        addMethodHandler("broadcastIntent", this::handleBroadcastIntent);
        addMethodHandler("getRunningAppProcesses", this::handleGetRunningProcesses);
        addMethodHandler("getAppTasks", this::handleGetAppTasks);
        addMethodHandler("getContentProvider", this::handleGetContentProvider);
        addMethodHandler("getServices", this::handleGetServices);

        // TODO: Use reflection to get IActivityManager singleton:
        //   IBinder binder = ServiceManager.getService("activity");
        //   IActivityManager original = IActivityManager.Stub.asInterface(binder);
        //   IActivityManager proxy = (IActivityManager) createProxy(...);
        //   Replace Singleton field in ActivityManager class

        markInjected();
    }

    private Object handleStartActivity(Object original, Method method, Object[] args)
            throws Throwable {
        // Find the Intent argument (usually index 2 or 3)
        Intent intent = findIntentArg(args);
        if (intent != null) {
            Log.d(TAG, "Intercepted startActivity: " + intent.getComponent());
            // TODO: Wrap intent in StubActivity intent
            // StubActivityRecord.wrap(intent, cloneUserId)
        }
        return method.invoke(original, args);
    }

    private Object handleStartService(Object original, Method method, Object[] args)
            throws Throwable {
        Intent intent = findIntentArg(args);
        if (intent != null) {
            Log.d(TAG, "Intercepted startService: " + intent.getComponent());
            // TODO: Redirect to clone's stub Service
        }
        return method.invoke(original, args);
    }

    private Object handleBroadcastIntent(Object original, Method method, Object[] args)
            throws Throwable {
        Intent intent = findIntentArg(args);
        if (intent != null) {
            Log.d(TAG, "Intercepted broadcast: " + intent.getAction());
            // TODO: Scope broadcast to clone's user space only
        }
        return method.invoke(original, args);
    }

    private Object handleGetRunningProcesses(Object original, Method method, Object[] args)
            throws Throwable {
        Object result = method.invoke(original, args);
        // TODO: Filter out :x, :main, and other :pN processes
        // Only return the clone's own process info
        return result;
    }

    private Object handleGetAppTasks(Object original, Method method, Object[] args)
            throws Throwable {
        Object result = method.invoke(original, args);
        // TODO: Filter to only show clone's own tasks
        return result;
    }

    private Object handleGetContentProvider(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Redirect ContentProvider lookups to clone-specific authorities
        return method.invoke(original, args);
    }

    private Object handleGetServices(Object original, Method method, Object[] args)
            throws Throwable {
        Object result = method.invoke(original, args);
        // TODO: Filter to only show clone's services
        return result;
    }

    private Intent findIntentArg(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof Intent) return (Intent) arg;
        }
        return null;
    }
}

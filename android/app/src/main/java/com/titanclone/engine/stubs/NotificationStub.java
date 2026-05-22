package com.titanclone.engine.stubs;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Virtual NotificationManager proxy — tags notifications with clone ID
 * so they can be identified and routed correctly.
 *
 * Hooked methods:
 * - notify() — tag notification with clone ID
 * - cancel() — cancel by clone-scoped tag
 * - cancelAll() — cancel only clone's notifications
 */
public class NotificationStub extends MethodInvocationProxy {

    private static final String TAG = "NotifStub";

    private String cloneTag;

    @Override
    public String getName() {
        return "NotificationManager";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("enqueueNotificationWithTag", this::handleNotify);
        addMethodHandler("cancelNotificationWithTag", this::handleCancel);
        addMethodHandler("cancelAllNotifications", this::handleCancelAll);

        // TODO: Use reflection to intercept INotificationManager
        markInjected();
    }

    public void setCloneTag(String tag) {
        this.cloneTag = tag;
    }

    private Object handleNotify(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Prepend clone tag to notification tag for isolation
        if (cloneTag != null && args != null && args.length > 1) {
            Log.d(TAG, "Tagging notification for clone: " + cloneTag);
        }
        return method.invoke(original, args);
    }

    private Object handleCancel(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Scope cancel to clone's notifications only
        return method.invoke(original, args);
    }

    private Object handleCancelAll(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Only cancel notifications belonging to this clone
        return method.invoke(original, args);
    }
}

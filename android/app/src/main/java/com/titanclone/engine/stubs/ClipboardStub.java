package com.titanclone.engine.stubs;

import android.content.ClipData;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Virtual ClipboardManager proxy — isolates clipboard per clone.
 * Each clone has its own clipboard buffer.
 */
public class ClipboardStub extends MethodInvocationProxy {

    private static final String TAG = "ClipStub";

    private ClipData virtualClipData;

    @Override
    public String getName() {
        return "ClipboardManager";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("setPrimaryClip", this::handleSetClip);
        addMethodHandler("getPrimaryClip", this::handleGetClip);
        addMethodHandler("hasPrimaryClip", this::handleHasClip);

        // TODO: Use reflection to intercept IClipboard
        markInjected();
    }

    private Object handleSetClip(Object original, Method method, Object[] args)
            throws Throwable {
        if (args != null && args.length > 0 && args[0] instanceof ClipData) {
            virtualClipData = (ClipData) args[0];
            Log.d(TAG, "Clone clipboard set");
            return null;
        }
        return method.invoke(original, args);
    }

    private Object handleGetClip(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualClipData != null) return virtualClipData;
        return method.invoke(original, args);
    }

    private Object handleHasClip(Object original, Method method, Object[] args)
            throws Throwable {
        if (virtualClipData != null) return true;
        return method.invoke(original, args);
    }
}

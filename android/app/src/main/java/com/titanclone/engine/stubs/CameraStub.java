package com.titanclone.engine.stubs;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Virtual Camera/MediaRecorder proxy — redirects camera output paths
 * to clone-specific storage so photos/videos stay isolated.
 */
public class CameraStub extends MethodInvocationProxy {

    private static final String TAG = "CamStub";

    private String virtualOutputDir;

    @Override
    public String getName() {
        return "Camera";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("setOutputFile", this::handleSetOutputFile);

        // TODO: Use reflection to intercept camera output paths
        markInjected();
    }

    public void setVirtualOutputDir(String dir) {
        this.virtualOutputDir = dir;
    }

    private Object handleSetOutputFile(Object original, Method method, Object[] args)
            throws Throwable {
        // TODO: Redirect output file path to clone's storage
        if (virtualOutputDir != null && args != null && args.length > 0
                && args[0] instanceof String) {
            String originalPath = (String) args[0];
            Log.d(TAG, "Redirecting camera output to clone storage");
            // TODO: Build redirected path
        }
        return method.invoke(original, args);
    }
}

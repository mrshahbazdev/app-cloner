package com.titanclone.engine.stubs;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Virtual Settings.Secure/Global proxy — returns spoofed android_id,
 * device name, and other settings per clone.
 *
 * Key hooked settings:
 * - android_id (16-char hex, unique per clone)
 * - bluetooth_address
 * - device_name
 */
public class SettingsStub extends MethodInvocationProxy {

    private static final String TAG = "SettingsStub";

    private final Map<String, String> virtualSettings = new HashMap<>();

    @Override
    public String getName() {
        return "Settings.Secure";
    }

    @Override
    public void inject() throws Throwable {
        addMethodHandler("getString", this::handleGetString);

        // TODO: Intercept Settings.Secure.getString() via ContentProvider proxy
        // The real implementation queries content://settings/secure
        markInjected();
    }

    public void setVirtualSetting(String key, String value) {
        virtualSettings.put(key, value);
    }

    public void setAndroidId(String androidId) {
        virtualSettings.put("android_id", androidId);
    }

    private Object handleGetString(Object original, Method method, Object[] args)
            throws Throwable {
        if (args != null && args.length >= 2 && args[1] instanceof String) {
            String key = (String) args[1];
            String virtualValue = virtualSettings.get(key);
            if (virtualValue != null) {
                Log.d(TAG, "Returning virtual setting: " + key);
                return virtualValue;
            }
        }
        return method.invoke(original, args);
    }
}

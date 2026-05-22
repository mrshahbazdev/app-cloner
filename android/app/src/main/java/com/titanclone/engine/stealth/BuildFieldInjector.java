package com.titanclone.engine.stealth;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Injects spoofed Build.* fields via reflection.
 *
 * Must be called BEFORE the clone app's Application.onCreate().
 * StubContentProvider.onCreate() runs before Application.onCreate(),
 * so injection happens there.
 *
 * Overrides 20+ static fields in android.os.Build and Build.VERSION.
 */
public class BuildFieldInjector {

    private static final String TAG = "BuildFieldInjector";

    /**
     * Inject all Build fields from a virtual profile.
     */
    public static void inject(
            String model, String manufacturer, String brand,
            String product, String device, String hardware,
            String fingerprint, String display, String buildId,
            String type, String tags, String serial,
            String bootloader, String board,
            int sdkInt, String release, String securityPatch,
            String codename, String incremental, String baseOs,
            int previewSdkInt) {

        try {
            // Build class fields
            setStaticField(Build.class, "MODEL", model);
            setStaticField(Build.class, "MANUFACTURER", manufacturer);
            setStaticField(Build.class, "BRAND", brand);
            setStaticField(Build.class, "PRODUCT", product);
            setStaticField(Build.class, "DEVICE", device);
            setStaticField(Build.class, "HARDWARE", hardware);
            setStaticField(Build.class, "FINGERPRINT", fingerprint);
            setStaticField(Build.class, "DISPLAY", display);
            setStaticField(Build.class, "ID", buildId);
            setStaticField(Build.class, "TYPE", type);
            setStaticField(Build.class, "TAGS", tags);
            setStaticField(Build.class, "SERIAL", serial);
            setStaticField(Build.class, "BOOTLOADER", bootloader);
            setStaticField(Build.class, "BOARD", board);
            setStaticField(Build.class, "HOST", "build.android.com");

            // Build.VERSION fields
            setStaticField(Build.VERSION.class, "SDK_INT", sdkInt);
            setStaticField(Build.VERSION.class, "RELEASE", release);
            setStaticField(Build.VERSION.class, "SECURITY_PATCH", securityPatch);
            setStaticField(Build.VERSION.class, "CODENAME", codename);
            setStaticField(Build.VERSION.class, "INCREMENTAL", incremental);
            setStaticField(Build.VERSION.class, "BASE_OS", baseOs);
            setStaticField(Build.VERSION.class, "PREVIEW_SDK_INT", previewSdkInt);

            Log.i(TAG, "Injected Build fields: model=" + model
                    + " manufacturer=" + manufacturer
                    + " fingerprint=" + fingerprint);

        } catch (Exception e) {
            Log.e(TAG, "Failed to inject Build fields", e);
        }
    }

    private static void setStaticField(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            // Remove final modifier
            Field modifiersField;
            try {
                modifiersField = Field.class.getDeclaredField("modifiers");
            } catch (NoSuchFieldException e) {
                // Android 12+ moved modifiers to different location
                modifiersField = Field.class.getDeclaredField("accessFlags");
            }
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

            field.set(null, value);
        } catch (Exception e) {
            Log.w(TAG, "Cannot set " + clazz.getSimpleName() + "." + fieldName + ": " + e.getMessage());
        }
    }
}

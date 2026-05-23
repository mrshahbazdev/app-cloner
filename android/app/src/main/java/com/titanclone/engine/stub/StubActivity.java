package com.titanclone.engine.stub;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.titanclone.engine.core.EngineConfig;
import com.titanclone.engine.core.VirtualCore;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Base stub Activity that acts as a proxy for clone app activities.
 * When the engine intercepts startActivity() from a clone, it wraps
 * the real intent inside a StubActivity intent. The StubActivity
 * unwraps and launches the real activity within the clone process.
 *
 * Each process slot (:p0-:p29) has its own StubActivity subclass
 * declared in AndroidManifest.xml to ensure the activity runs in
 * the correct clone process.
 */
public class StubActivity extends Activity {

    private static final String TAG = "StubActivity";
    public static final String EXTRA_REAL_INTENT = "titanclone.extra.REAL_INTENT";
    public static final String EXTRA_CLONE_USER_ID = "titanclone.extra.CLONE_USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent stubIntent = getIntent();
        if (stubIntent == null) {
            finish();
            return;
        }

        Intent realIntent = stubIntent.getParcelableExtra(EXTRA_REAL_INTENT);
        int cloneUserId = stubIntent.getIntExtra(EXTRA_CLONE_USER_ID, -1);

        if (realIntent != null) {
            String packageName = realIntent.getComponent().getPackageName();
            Log.d(TAG, "Unwrapping real intent for clone user=" + cloneUserId
                    + " package=" + packageName + " component=" + realIntent.getComponent());
            
            try {
                // Ensure VirtualCore is initialized in this process slot
                Context appCtx = getApplicationContext();
                VirtualCore.get().doAttachBaseContext(appCtx);
                VirtualCore.get().doCreate();

                // Install redirection rules for this slot's process
                VirtualCore.get().getStorageManager().installRedirectRules(packageName, cloneUserId);

                // Inject our custom instrumentation to hijack target activity creation
                VAInstrumentation.injectInstrumentation();

                // Forward the launch to the same StubActivity intent, but let VAInstrumentation
                // intercept its instantiation and load the real activity class dynamically.
                Intent launchIntent = new Intent(stubIntent);
                launchIntent.setClass(this, getClass());
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                startActivity(launchIntent);
                finish();

            } catch (Exception e) {
                Log.e(TAG, "Failed to dynamically launch clone activity", e);
                finish();
            }
        } else {
            Log.w(TAG, "No real intent found in stub");
            finish();
        }
    }

    /**
     * Wrap a real intent inside a StubActivity intent for the given process.
     */
    public static Intent wrap(Intent realIntent, int userId, Class<? extends StubActivity> stubClass) {
        Intent stubIntent = new Intent();
        stubIntent.setClassName(EngineConfig.HOST_PACKAGE, stubClass.getName());
        stubIntent.putExtra(EXTRA_REAL_INTENT, realIntent);
        stubIntent.putExtra(EXTRA_CLONE_USER_ID, userId);
        return stubIntent;
    }

    /**
     * Helper to map slot index to StubActivity subclass.
     */
    public static Class<? extends StubActivity> getStubActivityClass(int processIndex) {
        switch (processIndex) {
            case 0: return P0.class;
            case 1: return P1.class;
            case 2: return P2.class;
            case 3: return P3.class;
            case 4: return P4.class;
            case 5: return P5.class;
            case 6: return P6.class;
            case 7: return P7.class;
            case 8: return P8.class;
            case 9: return P9.class;
            case 10: return P10.class;
            case 11: return P11.class;
            case 12: return P12.class;
            case 13: return P13.class;
            case 14: return P14.class;
            case 15: return P15.class;
            case 16: return P16.class;
            case 17: return P17.class;
            case 18: return P18.class;
            case 19: return P19.class;
            case 20: return P20.class;
            case 21: return P21.class;
            case 22: return P22.class;
            case 23: return P23.class;
            case 24: return P24.class;
            case 25: return P25.class;
            case 26: return P26.class;
            case 27: return P27.class;
            case 28: return P28.class;
            case 29: return P29.class;
            default: return P0.class;
        }
    }

    /**
     * Custom Instrumentation that intercepts and loads activities from the cloned APK,
     * injecting resources dynamically.
     */
    public static class VAInstrumentation extends Instrumentation {
        private static final String TAG = "VAInstrumentation";
        private final Instrumentation base;
        
        private static android.app.Application sTargetApp = null;
        private static boolean sTargetAppCreated = false;

        public VAInstrumentation(Instrumentation base) {
            this.base = base;
        }

        public static void injectInstrumentation() {
            try {
                Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
                currentActivityThreadMethod.setAccessible(true);
                Object activityThread = currentActivityThreadMethod.invoke(null);

                Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
                instrumentationField.setAccessible(true);
                Instrumentation original = (Instrumentation) instrumentationField.get(activityThread);

                if (!(original instanceof VAInstrumentation)) {
                    instrumentationField.set(activityThread, new VAInstrumentation(original));
                    Log.i(TAG, "Successfully injected virtual VAInstrumentation");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to inject instrumentation", e);
            }
        }

        @Override
        public Activity newActivity(ClassLoader cl, String className, Intent intent)
                throws InstantiationException, IllegalAccessException, ClassNotFoundException {

            if (className.startsWith("com.titanclone.engine.stub.StubActivity")) {
                Intent realIntent = intent.getParcelableExtra(EXTRA_REAL_INTENT);
                if (realIntent != null && realIntent.getComponent() != null) {
                    String targetPackage = realIntent.getComponent().getPackageName();
                    String targetClass = realIntent.getComponent().getClassName();
                    int userId = intent.getIntExtra(EXTRA_CLONE_USER_ID, -1);

                    try {
                        // Get target APK file path from virtual storage
                        File apkFile = VirtualCore.get().getStorage().getCachedApk(targetPackage);
                        if (!apkFile.exists()) {
                            Log.e(TAG, "Target APK missing: " + apkFile.getAbsolutePath());
                            return super.newActivity(cl, className, intent);
                        }

                        // Build classpath: base.apk + all split APKs
                        File pkgDir = VirtualCore.get().getStorage().getPackageApkDir(targetPackage);
                        StringBuilder dexPath = new StringBuilder(apkFile.getAbsolutePath());
                        File[] splitApks = pkgDir.listFiles((dir, name) ->
                                name.endsWith(".apk") && !name.equals("base.apk"));
                        if (splitApks != null) {
                            for (File split : splitApks) {
                                dexPath.append(File.pathSeparator).append(split.getAbsolutePath());
                            }
                        }

                        File nativeLibDir = new File(pkgDir, "lib");

                        // Also include original app's native lib dir as fallback
                        String nativeLibPath = nativeLibDir.getAbsolutePath();
                        try {
                            ApplicationInfo srcInfo = VirtualCore.get().getContext()
                                    .getPackageManager().getApplicationInfo(targetPackage, 0);
                            if (srcInfo.nativeLibraryDir != null) {
                                nativeLibPath += File.pathSeparator + srcInfo.nativeLibraryDir;
                            }
                        } catch (Exception ignore) {}

                        // Use DelegateLastClassLoader to prioritize cloned app's classes over the host engine's classes
                        final ClassLoader customLoader = new dalvik.system.DelegateLastClassLoader(
                                dexPath.toString(),
                                nativeLibPath,
                                cl
                        );

                        // Fix LoadedApk and create Application BEFORE creating the Activity
                        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
                        currentActivityThreadMethod.setAccessible(true);
                        Object activityThread = currentActivityThreadMethod.invoke(null);
                        
                        Field mBoundApplicationField = activityThreadClass.getDeclaredField("mBoundApplication");
                        mBoundApplicationField.setAccessible(true);
                        Object mBoundApplication = mBoundApplicationField.get(activityThread);
                        
                        Object loadedApk = null;
                        if (mBoundApplication != null) {
                            Field infoField = mBoundApplication.getClass().getDeclaredField("info");
                            infoField.setAccessible(true);
                            loadedApk = infoField.get(mBoundApplication);
                        }
                        
                        final Resources targetRes = VirtualCore.get().getContext().getPackageManager().getResourcesForApplication(targetPackage);

                        if (loadedApk != null) {
                            try {
                                Field mClassLoaderField = loadedApk.getClass().getDeclaredField("mClassLoader");
                                mClassLoaderField.setAccessible(true);
                                mClassLoaderField.set(loadedApk, customLoader);
                                
                                Field mResourcesField = loadedApk.getClass().getDeclaredField("mResources");
                                mResourcesField.setAccessible(true);
                                mResourcesField.set(loadedApk, targetRes);
                            } catch (Exception ignore) {}
                        }

                        if (!sTargetAppCreated) {
                            sTargetAppCreated = true;
                            try {
                                android.content.pm.ApplicationInfo appInfo = VirtualCore.get().getContext().getPackageManager().getApplicationInfo(targetPackage, android.content.pm.PackageManager.GET_META_DATA);
                                String appClassName = appInfo.className;
                                if (appClassName == null) appClassName = "android.app.Application";
                                
                                Class<?> appClass = customLoader.loadClass(appClassName);
                                Context targetContext = new android.content.ContextWrapper(VirtualCore.get().getContext()) {
                                    @Override
                                    public Resources getResources() { return targetRes; }
                                    @Override
                                    public ClassLoader getClassLoader() { return customLoader; }
                                    @Override
                                    public Context getApplicationContext() { return sTargetApp; }
                                };
                                
                                sTargetApp = android.app.Instrumentation.newApplication(appClass, targetContext);
                                
                                if (loadedApk != null) {
                                    Field mApplicationField = loadedApk.getClass().getDeclaredField("mApplication");
                                    mApplicationField.setAccessible(true);
                                    mApplicationField.set(loadedApk, sTargetApp);
                                }
                                
                                Field mInitialApplicationField = activityThreadClass.getDeclaredField("mInitialApplication");
                                mInitialApplicationField.setAccessible(true);
                                mInitialApplicationField.set(activityThread, sTargetApp);
                                
                                sTargetApp.onCreate();
                                Log.i(TAG, "Successfully created and injected target Application: " + appClassName);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to create target Application", e);
                            }
                        }

                        // Load real activity class and instantiate it
                        Class<?> clazz = customLoader.loadClass(targetClass);
                        Activity activity = (Activity) clazz.newInstance();

                        // Set proper class loader for reading parcels/extras
                        intent.setExtrasClassLoader(customLoader);

                        Log.i(TAG, "Successfully instantiated clone activity: " + targetClass);
                        return activity;
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to load cloned class: " + targetClass, e);
                    }
                }
            }

            return base.newActivity(cl, className, intent);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle icicle) {
            if (activity.getClass().getName().startsWith("com.titanclone.engine.stub.StubActivity")) {
                base.callActivityOnCreate(activity, icicle);
                return;
            }

            // Cloned activity! Inject target resources and package context before calling onCreate.
            try {
                String packageName = activity.getClass().getPackage().getName();
                Intent intent = activity.getIntent();
                Intent realIntent = intent != null ? intent.getParcelableExtra(EXTRA_REAL_INTENT) : null;
                if (realIntent != null && realIntent.getComponent() != null) {
                    packageName = realIntent.getComponent().getPackageName();
                }

                Context appContext = activity.getApplicationContext();
                Resources targetRes = appContext.getPackageManager().getResourcesForApplication(packageName);

                // Inject resources into activity
                try {
                    Field resField = Activity.class.getDeclaredField("mResources");
                    resField.setAccessible(true);
                    resField.set(activity, targetRes);
                } catch (NoSuchFieldException ignore) {}

                // Inject into ContextThemeWrapper
                try {
                    Field themeResField = android.view.ContextThemeWrapper.class.getDeclaredField("mResources");
                    themeResField.setAccessible(true);
                    themeResField.set(activity, targetRes);
                } catch (NoSuchFieldException ignore) {}

                // Inject base context resources
                Context baseCtx = activity.getBaseContext();
                try {
                    Field baseResField = baseCtx.getClass().getDeclaredField("mResources");
                    baseResField.setAccessible(true);
                    baseResField.set(baseCtx, targetRes);
                } catch (Exception ex) {
                    // ContextImpl implementation details vary across Android versions, inject via mResources in base context class if possible
                    try {
                        Method getImpl = baseCtx.getClass().getDeclaredMethod("getImpl", Context.class);
                        getImpl.setAccessible(true);
                        Object impl = getImpl.invoke(baseCtx);
                        Field implRes = impl.getClass().getDeclaredField("mResources");
                        implRes.setAccessible(true);
                        implRes.set(impl, targetRes);
                    } catch (Exception e2) {
                        Log.w(TAG, "Base context resource injection fallback failed", e2);
                    }
                }

                // Removed Theme_DeviceDefault to allow the cloned app to use its own Theme (like Theme.AppCompat)

                if (sTargetApp != null) {
                    try {
                        Field mApplicationField = Activity.class.getDeclaredField("mApplication");
                        mApplicationField.setAccessible(true);
                        mApplicationField.set(activity, sTargetApp);
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to inject mApplication", e);
                    }
                }

                Log.i(TAG, "Successfully injected resources for: " + packageName);
            } catch (Exception e) {
                Log.e(TAG, "Failed to inject resources for clone activity", e);
            }

            base.callActivityOnCreate(activity, icicle);
        }

        public android.app.Instrumentation.ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {
            Intent wrappedIntent = interceptIntent(intent, who);
            try {
                Method exec = Instrumentation.class.getDeclaredMethod("execStartActivity", 
                        Context.class, IBinder.class, IBinder.class, Activity.class, 
                        Intent.class, int.class, Bundle.class);
                exec.setAccessible(true);
                return (android.app.Instrumentation.ActivityResult) exec.invoke(base, who, contextThread, token, target, wrappedIntent, requestCode, options);
            } catch (Exception e) {
                Log.e(TAG, "Failed to invoke base execStartActivity", e);
                return null;
            }
        }

        public android.app.Instrumentation.ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, String target,
                Intent intent, int requestCode, Bundle options) {
            Intent wrappedIntent = interceptIntent(intent, who);
            try {
                Method exec = Instrumentation.class.getDeclaredMethod("execStartActivity", 
                        Context.class, IBinder.class, IBinder.class, String.class, 
                        Intent.class, int.class, Bundle.class);
                exec.setAccessible(true);
                return (android.app.Instrumentation.ActivityResult) exec.invoke(base, who, contextThread, token, target, wrappedIntent, requestCode, options);
            } catch (Exception e) {
                Log.e(TAG, "Failed to invoke base execStartActivity", e);
                return null;
            }
        }

        public android.app.Instrumentation.ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options, android.os.UserHandle user) {
            Intent wrappedIntent = interceptIntent(intent, who);
            try {
                Method exec = Instrumentation.class.getDeclaredMethod("execStartActivity", 
                        Context.class, IBinder.class, IBinder.class, Activity.class, 
                        Intent.class, int.class, Bundle.class, android.os.UserHandle.class);
                exec.setAccessible(true);
                return (android.app.Instrumentation.ActivityResult) exec.invoke(base, who, contextThread, token, target, wrappedIntent, requestCode, options, user);
            } catch (Exception e) {
                Log.e(TAG, "Failed to invoke base execStartActivity", e);
                return null;
            }
        }

        private Intent interceptIntent(Intent intent, Context who) {
            if (intent == null) return null;
            if (intent.getComponent() == null) {
                return intent; 
            }
            if (intent.getComponent().getPackageName().equals(EngineConfig.HOST_PACKAGE)) {
                // Check if the target class actually exists in the host app.
                // Clone apps run inside the host process and their Context returns
                // the host package name, so their internal activity intents arrive
                // here with package=HOST but class=clone activity.  If the class is
                // not loadable from the host classloader, it belongs to a clone and
                // must be intercepted.
                try {
                    Class.forName(intent.getComponent().getClassName());
                    return intent; // genuine host activity
                } catch (ClassNotFoundException e) {
                    // not a host class — fall through to wrap as clone activity
                    Log.d(TAG, "Intercepting clone activity with host package: "
                            + intent.getComponent().getClassName());
                }
            }
            
            int processIndex = 0;
            String processName = android.app.Application.getProcessName();
            if (processName != null && processName.contains(":p")) {
                try {
                    processIndex = Integer.parseInt(processName.substring(processName.lastIndexOf(":p") + 2));
                } catch (Exception e) {
                    processIndex = 0;
                }
            }
            Class<? extends StubActivity> stubClass = StubActivity.getStubActivityClass(processIndex);
            
            int userId = 0;
            if (who instanceof Activity) {
                Intent sourceIntent = ((Activity) who).getIntent();
                if (sourceIntent != null) {
                    userId = sourceIntent.getIntExtra(StubActivity.EXTRA_CLONE_USER_ID, 0);
                }
            }
            
            Log.i(TAG, "Intercepted startActivity for " + intent.getComponent().flattenToString() + " wrapping to " + stubClass.getName());
            return StubActivity.wrap(intent, userId, stubClass);
        }
    }

    // Process-specific subclasses (declared in AndroidManifest.xml)
    public static class P0 extends StubActivity {}
    public static class P1 extends StubActivity {}
    public static class P2 extends StubActivity {}
    public static class P3 extends StubActivity {}
    public static class P4 extends StubActivity {}
    public static class P5 extends StubActivity {}
    public static class P6 extends StubActivity {}
    public static class P7 extends StubActivity {}
    public static class P8 extends StubActivity {}
    public static class P9 extends StubActivity {}
    public static class P10 extends StubActivity {}
    public static class P11 extends StubActivity {}
    public static class P12 extends StubActivity {}
    public static class P13 extends StubActivity {}
    public static class P14 extends StubActivity {}
    public static class P15 extends StubActivity {}
    public static class P16 extends StubActivity {}
    public static class P17 extends StubActivity {}
    public static class P18 extends StubActivity {}
    public static class P19 extends StubActivity {}
    public static class P20 extends StubActivity {}
    public static class P21 extends StubActivity {}
    public static class P22 extends StubActivity {}
    public static class P23 extends StubActivity {}
    public static class P24 extends StubActivity {}
    public static class P25 extends StubActivity {}
    public static class P26 extends StubActivity {}
    public static class P27 extends StubActivity {}
    public static class P28 extends StubActivity {}
    public static class P29 extends StubActivity {}
}

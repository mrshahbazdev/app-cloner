package com.titanclone.engine.stub;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

                // Create and inject the target app's Application BEFORE launching
                // the clone activity.  Apps using Hilt/Dagger require the Application
                // to implement GeneratedComponentManager; doing this early ensures
                // the correct Application is returned by makeApplication() when the
                // framework calls performLaunchActivity() for the clone Activity.
                VAInstrumentation.injectTargetApplication(packageName, cloneUserId);

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
        private static String sTargetPackage = null;

        public VAInstrumentation(Instrumentation base) {
            this.base = base;
        }

        /**
         * Create and inject the clone's Application into all ActivityThread
         * references BEFORE the clone Activity is launched.  This is critical
         * for Hilt/Dagger apps whose Activities call getApplicationContext()
         * in attachBaseContext() and expect GeneratedComponentManager.
         */
        @SuppressWarnings("unchecked")
        public static void injectTargetApplication(String targetPackage, int userId) {
            if (sTargetAppCreated) return;

            try {
                // Build the DexClassLoader for the target package
                File apkFile = VirtualCore.get().getStorage().getCachedApk(targetPackage);
                if (!apkFile.exists()) {
                    Log.w(TAG, "Target APK not found for early app injection: " + targetPackage);
                    return;
                }

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
                String nativeLibPath = nativeLibDir.getAbsolutePath();
                try {
                    ApplicationInfo srcInfo = VirtualCore.get().getContext()
                            .getPackageManager().getApplicationInfo(targetPackage, 0);
                    if (srcInfo.nativeLibraryDir != null) {
                        nativeLibPath += File.pathSeparator + srcInfo.nativeLibraryDir;
                    }
                } catch (Exception ignore) {}

                final ClassLoader customLoader = new dalvik.system.DelegateLastClassLoader(
                        dexPath.toString(),
                        nativeLibPath,
                        StubActivity.class.getClassLoader()
                );

                // Get the target Application class name from the installed package
                ApplicationInfo appInfo = VirtualCore.get().getContext()
                        .getPackageManager().getApplicationInfo(targetPackage,
                                android.content.pm.PackageManager.GET_META_DATA);
                String appClassName = appInfo.className;
                if (appClassName == null) appClassName = "android.app.Application";

                final Resources targetRes = VirtualCore.get().getContext()
                        .getPackageManager().getResourcesForApplication(targetPackage);

                // Create the target Application
                Class<?> appClass = customLoader.loadClass(appClassName);

                // Build an ApplicationInfo that points at the target APK and its
                // native library directory so that frameworks like Xamarin/Mono can
                // locate libmonodroid.so (and friends) during attachBaseContext().
                final ApplicationInfo targetAppInfo = new ApplicationInfo(appInfo);
                targetAppInfo.packageName = targetPackage;
                targetAppInfo.sourceDir = apkFile.getAbsolutePath();
                targetAppInfo.publicSourceDir = apkFile.getAbsolutePath();
                // Preserve split APK paths — needed for App Bundle resources
                if (splitApks != null && splitApks.length > 0) {
                    String[] splitDirs = new String[splitApks.length];
                    for (int i = 0; i < splitApks.length; i++) {
                        splitDirs[i] = splitApks[i].getAbsolutePath();
                    }
                    targetAppInfo.splitSourceDirs = splitDirs;
                    targetAppInfo.splitPublicSourceDirs = splitDirs;
                }
                // Prefer the original app's nativeLibraryDir (system-extracted),
                // fall back to the clone's extracted lib dir.
                if (appInfo.nativeLibraryDir != null && new File(appInfo.nativeLibraryDir).isDirectory()) {
                    targetAppInfo.nativeLibraryDir = appInfo.nativeLibraryDir;
                } else {
                    targetAppInfo.nativeLibraryDir = nativeLibDir.getAbsolutePath();
                }
                // Also set the data dir to the clone's sandbox
                File dataDir = VirtualCore.get().getStorage().getCloneDataDir(targetPackage, 0);
                if (dataDir != null) {
                    targetAppInfo.dataDir = dataDir.getAbsolutePath();
                }
                final String tgtPkg = targetPackage;
                final String apkPath = apkFile.getAbsolutePath();

                // Pre-load native libraries using Runtime.nativeLoad() with the
                // TARGET classloader.  Unlike System.load() (which associates
                // libraries with the calling class's classloader — the HOST),
                // nativeLoad() explicitly binds the library to customLoader so
                // that JNI_OnLoad's FindClass() resolves target-app classes.
                preloadNativeLibraries(nativeLibDir, nativeLibPath, customLoader);

                // ContextWrapper that presents the target app's identity so that
                // Application.attachBaseContext() sees the correct package name,
                // ApplicationInfo (with nativeLibraryDir), and APK paths.
                // Also redirect file operations to the clone's sandboxed data dir.
                final File cloneDataDir = dataDir;
                Context targetContext = new android.content.ContextWrapper(
                        VirtualCore.get().getContext()) {
                    @Override public Resources getResources() { return targetRes; }
                    @Override public ClassLoader getClassLoader() { return customLoader; }
                    @Override public Context getApplicationContext() {
                        return sTargetApp != null ? sTargetApp : this;
                    }
                    @Override public ApplicationInfo getApplicationInfo() { return targetAppInfo; }
                    @Override public String getPackageName() { return tgtPkg; }
                    @Override public String getPackageCodePath() { return apkPath; }
                    @Override public String getPackageResourcePath() { return apkPath; }
                    @Override public File getFilesDir() {
                        File d = new File(cloneDataDir, "files");
                        d.mkdirs();
                        return d;
                    }
                    @Override public File getCacheDir() {
                        File d = new File(cloneDataDir, "cache");
                        d.mkdirs();
                        return d;
                    }
                    @Override public File getDataDir() { return cloneDataDir; }
                    @Override public java.io.File getDatabasePath(String name) {
                        File d = new File(cloneDataDir, "databases");
                        d.mkdirs();
                        return new File(d, name);
                    }
                    @Override public File getDir(String name, int mode) {
                        File d = new File(cloneDataDir, "app_" + name);
                        d.mkdirs();
                        return d;
                    }
                    @Override public android.content.SharedPreferences getSharedPreferences(
                            String name, int mode) {
                        File prefsDir = new File(cloneDataDir, "shared_prefs");
                        prefsDir.mkdirs();
                        return super.getSharedPreferences(name, mode);
                    }
                };

                sTargetApp = android.app.Instrumentation.newApplication(appClass, targetContext);
                sTargetAppCreated = true;
                sTargetPackage = targetPackage;

                // Collect split APK paths for LoadedApk patching
                String[] splitPaths = null;
                if (splitApks != null && splitApks.length > 0) {
                    splitPaths = new String[splitApks.length];
                    for (int i = 0; i < splitApks.length; i++) {
                        splitPaths[i] = splitApks[i].getAbsolutePath();
                    }
                }
                // Also include original installed app's split APKs
                try {
                    if (appInfo.splitSourceDirs != null) {
                        java.util.List<String> allSplits = new java.util.ArrayList<>();
                        if (splitPaths != null) {
                            java.util.Collections.addAll(allSplits, splitPaths);
                        }
                        for (String origSplit : appInfo.splitSourceDirs) {
                            if (!allSplits.contains(origSplit)) {
                                allSplits.add(origSplit);
                            }
                        }
                        splitPaths = allSplits.toArray(new String[0]);
                    }
                } catch (Exception ignore) {}

                // Inject into ActivityThread and ALL LoadedApk references
                Class<?> atClass = Class.forName("android.app.ActivityThread");
                Method currentAT = atClass.getDeclaredMethod("currentActivityThread");
                currentAT.setAccessible(true);
                Object activityThread = currentAT.invoke(null);

                // 1. mInitialApplication
                Field mInitApp = atClass.getDeclaredField("mInitialApplication");
                mInitApp.setAccessible(true);
                mInitApp.set(activityThread, sTargetApp);

                // 2. mBoundApplication.info (LoadedApk) — full identity patch
                Field mBoundApp = atClass.getDeclaredField("mBoundApplication");
                mBoundApp.setAccessible(true);
                Object boundApp = mBoundApp.get(activityThread);
                if (boundApp != null) {
                    Field infoField = boundApp.getClass().getDeclaredField("info");
                    infoField.setAccessible(true);
                    Object loadedApk = infoField.get(boundApp);
                    if (loadedApk != null) {
                        setLoadedApkFields(loadedApk, sTargetApp, customLoader, targetRes);
                        patchLoadedApkIdentity(loadedApk, targetPackage,
                                targetAppInfo, apkFile.getAbsolutePath(), splitPaths);
                    }
                }

                // 3. Walk mPackages cache to update any other LoadedApk instances
                try {
                    Field mPkgsField = atClass.getDeclaredField("mPackages");
                    mPkgsField.setAccessible(true);
                    Object mPkgs = mPkgsField.get(activityThread);
                    if (mPkgs instanceof java.util.Map) {
                        for (Object val : ((java.util.Map<?, ?>) mPkgs).values()) {
                            Object ref = val;
                            if (ref instanceof java.lang.ref.WeakReference) {
                                ref = ((java.lang.ref.WeakReference<?>) ref).get();
                            }
                            if (ref != null) {
                                setLoadedApkFields(ref, sTargetApp, customLoader, targetRes);
                                patchLoadedApkIdentity(ref, targetPackage,
                                        targetAppInfo, apkFile.getAbsolutePath(), splitPaths);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to walk mPackages cache", e);
                }

                // 4. Also update mAllApplications
                try {
                    Field mAllApps = atClass.getDeclaredField("mAllApplications");
                    mAllApps.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.ArrayList<android.app.Application> allApps =
                            (java.util.ArrayList<android.app.Application>) mAllApps.get(activityThread);
                    if (allApps != null && !allApps.contains(sTargetApp)) {
                        allApps.add(sTargetApp);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to add to mAllApplications", e);
                }

                // 5. Hook IPackageManager — redirect host package queries
                //    to the target so apps see their own identity
                String hostPkg = VirtualCore.get().getContext().getPackageName();
                injectPackageManagerProxy(hostPkg, targetPackage);

                // Call onCreate() in its own try-catch so that apps whose
                // onCreate() requires a native runtime (Xamarin, React Native,
                // etc.) don't crash the whole clone process if loading fails.
                try {
                    sTargetApp.onCreate();
                } catch (Throwable onCreateErr) {
                    Log.w(TAG, "Target Application.onCreate() failed (non-fatal): "
                            + onCreateErr.getMessage());
                }

                Log.i(TAG, "Early Application injection succeeded: " + appClassName);

            } catch (Throwable e) {
                Log.e(TAG, "Early Application injection failed for " + targetPackage, e);
            }
        }

        private static void setLoadedApkFields(Object loadedApk,
                android.app.Application app, ClassLoader cl, Resources res) {
            try {
                Field mApp = loadedApk.getClass().getDeclaredField("mApplication");
                mApp.setAccessible(true);
                mApp.set(loadedApk, app);
            } catch (Exception e) {
                Log.w(TAG, "setLoadedApk mApplication failed", e);
            }
            try {
                Field mCl = loadedApk.getClass().getDeclaredField("mClassLoader");
                mCl.setAccessible(true);
                mCl.set(loadedApk, cl);
            } catch (Exception e) {
                Log.w(TAG, "setLoadedApk mClassLoader failed", e);
            }
            try {
                Field mRes = loadedApk.getClass().getDeclaredField("mResources");
                mRes.setAccessible(true);
                mRes.set(loadedApk, res);
            } catch (Exception e) {
                Log.w(TAG, "setLoadedApk mResources failed", e);
            }
        }

        /**
         * Deep LoadedApk patching: replace the package name, resource
         * directories, and ApplicationInfo so the system creates
         * Resources from the TARGET APK instead of the host.
         * This prevents resource ID collisions between host and target.
         */
        private static void patchLoadedApkIdentity(Object loadedApk,
                String targetPackage, ApplicationInfo targetAppInfo,
                String baseApkPath, String[] splitApkPaths) {
            Class<?> cls = loadedApk.getClass();

            // Package name — makes ContextImpl.getPackageName() return target
            setField(cls, loadedApk, "mPackageName", targetPackage);

            // Resource directory — makes system create Resources from target APK
            setField(cls, loadedApk, "mResDir", baseApkPath);

            // Split resource directories
            setField(cls, loadedApk, "mSplitResDirs", splitApkPaths);

            // ApplicationInfo — used by ContextImpl.getApplicationInfo()
            setField(cls, loadedApk, "mApplicationInfo", targetAppInfo);

            // Data directory
            if (targetAppInfo.dataDir != null) {
                setField(cls, loadedApk, "mDataDir", targetAppInfo.dataDir);
                setField(cls, loadedApk, "mDataDirFile",
                        new File(targetAppInfo.dataDir));
            }

            Log.d(TAG, "Patched LoadedApk identity → " + targetPackage);
        }

        private static void setField(Class<?> cls, Object obj,
                String name, Object value) {
            try {
                Field f = cls.getDeclaredField(name);
                f.setAccessible(true);
                f.set(obj, value);
            } catch (Exception e) {
                Log.w(TAG, "setField " + name + " failed: " + e.getMessage());
            }
        }

        /**
         * Hook ActivityThread.sPackageManager with a dynamic proxy so that
         * IPackageManager calls in this process return the target app's
         * info when queried for the host package.
         */
        private static void injectPackageManagerProxy(
                String hostPackage, String targetPackage) {
            try {
                Class<?> atClass = Class.forName("android.app.ActivityThread");
                Field spmField = atClass.getDeclaredField("sPackageManager");
                spmField.setAccessible(true);
                final Object originalPM = spmField.get(null);
                if (originalPM == null) return;

                Class<?> ipmClass = Class.forName(
                        "android.content.pm.IPackageManager");

                Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                        ipmClass.getClassLoader(),
                        new Class<?>[]{ ipmClass },
                        new java.lang.reflect.InvocationHandler() {
                            @Override
                            public Object invoke(Object p, Method method,
                                    Object[] args) throws Throwable {
                                // Redirect host package queries to target
                                if (args != null && args.length > 0
                                        && args[0] instanceof String) {
                                    String pkg = (String) args[0];
                                    if (hostPackage.equals(pkg)) {
                                        args[0] = targetPackage;
                                    }
                                }

                                // checkPermission(permName, pkgName)
                                if ("checkPermission".equals(method.getName())
                                        && args != null && args.length >= 2
                                        && args[1] instanceof String) {
                                    String pkg = (String) args[1];
                                    if (hostPackage.equals(pkg)) {
                                        args[1] = targetPackage;
                                    }
                                }

                                try {
                                    return method.invoke(originalPM, args);
                                } catch (java.lang.reflect.InvocationTargetException e) {
                                    throw e.getTargetException();
                                }
                            }
                        });

                spmField.set(null, proxy);
                Log.i(TAG, "IPackageManager proxy injected: "
                        + hostPackage + " → " + targetPackage);

                // Also replace mPM in any cached ApplicationPackageManager
                try {
                    Method currentAT = atClass.getDeclaredMethod(
                            "currentActivityThread");
                    currentAT.setAccessible(true);
                    Object at = currentAT.invoke(null);
                    Field mBoundApp = atClass.getDeclaredField(
                            "mBoundApplication");
                    mBoundApp.setAccessible(true);
                    Object boundApp = mBoundApp.get(at);
                    if (boundApp != null) {
                        Field infoField = boundApp.getClass()
                                .getDeclaredField("info");
                        infoField.setAccessible(true);
                        Object loadedApk = infoField.get(boundApp);
                        if (loadedApk != null) {
                            Field mPkgMgr = loadedApk.getClass()
                                    .getDeclaredField("mPackageManager");
                            mPkgMgr.setAccessible(true);
                            mPkgMgr.set(loadedApk, null);
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to clear cached PM", e);
                }
            } catch (Exception e) {
                Log.w(TAG, "IPackageManager proxy injection failed", e);
            }
        }

        /**
         * Add the target APK's asset paths to every reachable AssetManager
         * so that clone resource IDs are resolvable regardless of which
         * Resources / Context path the app code uses.
         */
        private static void addTargetAssetPaths(Activity activity, String packageName) {
            try {
                Method addAssetPath = android.content.res.AssetManager.class
                        .getDeclaredMethod("addAssetPath", String.class);
                addAssetPath.setAccessible(true);

                File pkgDir = VirtualCore.get().getStorage().getPackageApkDir(packageName);
                java.util.List<String> apkPaths = new java.util.ArrayList<>();

                // Collect clone APK paths (base + splits)
                File baseApk = new File(pkgDir, "base.apk");
                if (baseApk.exists()) apkPaths.add(baseApk.getAbsolutePath());
                File[] splits = pkgDir.listFiles((d, name) ->
                        name.endsWith(".apk") && !name.equals("base.apk"));
                if (splits != null) {
                    for (File s : splits) apkPaths.add(s.getAbsolutePath());
                }

                // Also add original installed app's APK paths as fallback
                try {
                    ApplicationInfo srcInfo = VirtualCore.get().getContext()
                            .getPackageManager().getApplicationInfo(packageName, 0);
                    if (srcInfo.sourceDir != null && !apkPaths.contains(srcInfo.sourceDir)) {
                        apkPaths.add(srcInfo.sourceDir);
                    }
                    if (srcInfo.splitSourceDirs != null) {
                        for (String split : srcInfo.splitSourceDirs) {
                            if (!apkPaths.contains(split)) apkPaths.add(split);
                        }
                    }
                } catch (PackageManager.NameNotFoundException ignore) {}

                // Inject into all reachable AssetManagers
                java.util.Set<android.content.res.AssetManager> injected = new java.util.HashSet<>();

                // Activity's own resources
                android.content.res.AssetManager actAm = activity.getResources().getAssets();
                for (String p : apkPaths) addAssetPath.invoke(actAm, p);
                injected.add(actAm);

                // Base context resources
                try {
                    android.content.res.AssetManager baseAm = activity.getBaseContext().getResources().getAssets();
                    if (!injected.contains(baseAm)) {
                        for (String p : apkPaths) addAssetPath.invoke(baseAm, p);
                        injected.add(baseAm);
                    }
                } catch (Exception ignore) {}

                // Application context resources
                try {
                    android.content.res.AssetManager appAm = activity.getApplicationContext().getResources().getAssets();
                    if (!injected.contains(appAm)) {
                        for (String p : apkPaths) addAssetPath.invoke(appAm, p);
                        injected.add(appAm);
                    }
                } catch (Exception ignore) {}

                // Target Application resources
                if (sTargetApp != null) {
                    try {
                        android.content.res.AssetManager tgtAm = sTargetApp.getResources().getAssets();
                        if (!injected.contains(tgtAm)) {
                            for (String p : apkPaths) addAssetPath.invoke(tgtAm, p);
                            injected.add(tgtAm);
                        }
                    } catch (Exception ignore) {}
                }

                Log.d(TAG, "Injected " + apkPaths.size() + " asset paths into "
                        + injected.size() + " AssetManagers");
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "AssetManager.addAssetPath not available");
            } catch (Exception e) {
                Log.w(TAG, "Asset path injection failed", e);
            }
        }

        /**
         * Pre-load native libraries using Runtime.nativeLoad() which accepts
         * a ClassLoader parameter.  This binds each library to the TARGET
         * classloader so that JNI_OnLoad() resolves classes from the clone
         * app, not the host app.
         *
         * This is fundamentally different from System.load() which uses
         * VMStack.getCallingClassLoader() — always the HOST classloader
         * when called from our code — causing JNI_OnLoad to fail because
         * it can't find target-app classes via FindClass().
         */
        private static void preloadNativeLibraries(File libDir, String nativeLibPath,
                ClassLoader targetLoader) {
            Method nativeLoad;
            try {
                nativeLoad = Runtime.class.getDeclaredMethod("nativeLoad",
                        String.class, ClassLoader.class);
                nativeLoad.setAccessible(true);
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "Runtime.nativeLoad not available, skipping pre-load");
                return;
            }

            // Collect directories to scan
            java.util.List<File> libDirs = new java.util.ArrayList<>();
            if (libDir.isDirectory()) libDirs.add(libDir);
            // Also check subdirectories (e.g. lib/arm64-v8a/)
            File[] subDirs = libDir.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File sub : subDirs) libDirs.add(sub);
            }
            if (nativeLibPath != null) {
                for (String path : nativeLibPath.split(File.pathSeparator)) {
                    File dir = new File(path);
                    if (dir.isDirectory() && !libDirs.contains(dir)) {
                        libDirs.add(dir);
                    }
                }
            }

            // Collect all .so files
            java.util.List<File> allSoFiles = new java.util.ArrayList<>();
            for (File dir : libDirs) {
                File[] soFiles = dir.listFiles((d, name) -> name.endsWith(".so"));
                if (soFiles != null) {
                    java.util.Collections.addAll(allSoFiles, soFiles);
                }
            }

            // Multiple passes for dependency ordering
            int loaded = 0;
            for (int pass = 0; pass < 3 && !allSoFiles.isEmpty(); pass++) {
                java.util.Iterator<File> it = allSoFiles.iterator();
                while (it.hasNext()) {
                    File so = it.next();
                    try {
                        String error = (String) nativeLoad.invoke(
                                Runtime.getRuntime(), so.getAbsolutePath(), targetLoader);
                        if (error == null) {
                            loaded++;
                            it.remove();
                        } else {
                            // Non-null = error message; may succeed on next pass
                            // after dependencies are loaded
                        }
                    } catch (Throwable e) {
                        it.remove(); // Won't succeed on retry
                    }
                }
            }

            if (loaded > 0) {
                Log.i(TAG, "Pre-loaded " + loaded + " native libraries (target classloader)");
            }
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
                        // Target Application should already be created by
                        // injectTargetApplication() in StubActivity.onCreate().
                        // If not, create it now as fallback.
                        if (!sTargetAppCreated) {
                            injectTargetApplication(targetPackage, userId);
                        }

                        // Use the classloader from the target Application if available,
                        // otherwise build one from scratch
                        ClassLoader customLoader;
                        if (sTargetApp != null) {
                            customLoader = sTargetApp.getClassLoader();
                        } else {
                            File apkFile = VirtualCore.get().getStorage().getCachedApk(targetPackage);
                            if (!apkFile.exists()) {
                                Log.e(TAG, "Target APK missing: " + apkFile.getAbsolutePath());
                                return super.newActivity(cl, className, intent);
                            }
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
                            String nativeLibPath = nativeLibDir.getAbsolutePath();
                            try {
                                ApplicationInfo srcInfo = VirtualCore.get().getContext()
                                        .getPackageManager().getApplicationInfo(targetPackage, 0);
                                if (srcInfo.nativeLibraryDir != null) {
                                    nativeLibPath += File.pathSeparator + srcInfo.nativeLibraryDir;
                                }
                            } catch (Exception ignore) {}
                            customLoader = new dalvik.system.DelegateLastClassLoader(
                                    dexPath.toString(), nativeLibPath, cl);
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
                // Use stored target package — intent component may contain
                // the host package name when the clone navigates internally.
                String packageName = sTargetPackage;
                if (packageName == null) {
                    Intent intent = activity.getIntent();
                    Intent realIntent = intent != null ? intent.getParcelableExtra(EXTRA_REAL_INTENT) : null;
                    if (realIntent != null && realIntent.getComponent() != null) {
                        packageName = realIntent.getComponent().getPackageName();
                    }
                }

                // Prefer resources from the already-injected target Application
                Resources targetRes;
                if (sTargetApp != null) {
                    targetRes = sTargetApp.getResources();
                } else if (packageName != null) {
                    Context appContext = activity.getApplicationContext();
                    targetRes = appContext.getPackageManager().getResourcesForApplication(packageName);
                } else {
                    base.callActivityOnCreate(activity, icicle);
                    return;
                }

                // --- Deep resource injection ---
                // 1. Add target APK asset paths to the activity's own
                //    AssetManager so that resource IDs from the clone APK
                //    are resolvable regardless of which Resources object
                //    the app code uses (ContextThemeWrapper, base context,
                //    ApplicationContext, custom wrappers, etc.).
                if (packageName != null) {
                    addTargetAssetPaths(activity, packageName);
                }

                // 2. Replace the Resources object in every layer
                try {
                    Field resField = Activity.class.getDeclaredField("mResources");
                    resField.setAccessible(true);
                    resField.set(activity, targetRes);
                } catch (NoSuchFieldException ignore) {}

                try {
                    Field themeResField = android.view.ContextThemeWrapper.class.getDeclaredField("mResources");
                    themeResField.setAccessible(true);
                    themeResField.set(activity, targetRes);
                } catch (NoSuchFieldException ignore) {}

                Context baseCtx = activity.getBaseContext();
                try {
                    Field baseResField = baseCtx.getClass().getDeclaredField("mResources");
                    baseResField.setAccessible(true);
                    baseResField.set(baseCtx, targetRes);
                } catch (Exception ex) {
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

                // Apply the target activity's theme from the original manifest.
                // Without this, AppCompatActivity crashes because StubActivity's
                // manifest entry uses a platform theme, not Theme.AppCompat.
                try {
                    android.content.ComponentName comp = new android.content.ComponentName(
                            packageName, activity.getClass().getName());
                    ActivityInfo actInfo = VirtualCore.get().getContext()
                            .getPackageManager().getActivityInfo(comp, 0);
                    int themeResId = actInfo.theme;
                    if (themeResId == 0) {
                        themeResId = actInfo.applicationInfo.theme;
                    }
                    if (themeResId != 0) {
                        activity.setTheme(themeResId);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // Activity not in original manifest — try application-level theme
                    try {
                        ApplicationInfo appInfo = VirtualCore.get().getContext()
                                .getPackageManager().getApplicationInfo(packageName, 0);
                        if (appInfo.theme != 0) {
                            activity.setTheme(appInfo.theme);
                        }
                    } catch (PackageManager.NameNotFoundException ignore) {}
                }

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

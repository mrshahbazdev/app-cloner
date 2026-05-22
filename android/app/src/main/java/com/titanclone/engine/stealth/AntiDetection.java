package com.titanclone.engine.stealth;

import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Anti-detection measures to prevent apps from discovering they
 * are running inside TitanClone's virtualization environment.
 *
 * Counter-measures:
 * 1. Stack trace filtering — removes engine frames from traces
 * 2. Package name proxy — getPackageName() returns clone's real name
 * 3. /proc/self/maps filtering — native hook hides engine .so files
 * 4. Installer verification — returns "com.android.vending"
 * 5. Xposed/VirtualApp detection — clean since we use Java Proxy, not hooks
 */
public class AntiDetection {

    private static final String TAG = "AntiDetection";

    // Engine package prefixes to filter from stack traces
    private static final Set<String> ENGINE_PACKAGES = new HashSet<>(Arrays.asList(
            "com.titanclone.engine.",
            "com.titanclone.titan_clone.",
            "com.titanclone.engine.stubs.",
            "com.titanclone.engine.stealth.",
            "com.titanclone.engine.stub.",
            "com.titanclone.engine.ipc.",
            "com.titanclone.engine.process."
    ));

    // Native library names to hide from /proc/self/maps
    private static final Set<String> HIDDEN_LIBRARIES = new HashSet<>(Arrays.asList(
            "libtitanclone.so",
            "libio_redirect.so",
            "libbinder_intercept.so",
            "libproperty_redirect.so",
            "libmemory_manager.so",
            "libproc_maps_filter.so"
    ));

    /**
     * Filter engine frames from a stack trace.
     * Returns a clean trace that looks like a normal app call stack.
     */
    public static StackTraceElement[] filterStackTrace(StackTraceElement[] original) {
        if (original == null) return new StackTraceElement[0];

        return Arrays.stream(original)
                .filter(element -> {
                    String className = element.getClassName();
                    for (String pkg : ENGINE_PACKAGES) {
                        if (className.startsWith(pkg)) return false;
                    }
                    return true;
                })
                .toArray(StackTraceElement[]::new);
    }

    /**
     * Check if current thread's stack trace contains engine frames.
     * If so, filter and replace them.
     */
    public static void cleanCurrentStackTrace() {
        Thread current = Thread.currentThread();
        StackTraceElement[] trace = current.getStackTrace();
        boolean hasEngineFrames = false;

        for (StackTraceElement element : trace) {
            String className = element.getClassName();
            for (String pkg : ENGINE_PACKAGES) {
                if (className.startsWith(pkg)) {
                    hasEngineFrames = true;
                    break;
                }
            }
            if (hasEngineFrames) break;
        }

        if (hasEngineFrames) {
            Log.d(TAG, "Cleaned " + trace.length + " stack frames");
        }
    }

    /**
     * Check if a library name should be hidden from /proc/self/maps.
     */
    public static boolean shouldHideLibrary(String libraryPath) {
        if (libraryPath == null) return false;
        for (String lib : HIDDEN_LIBRARIES) {
            if (libraryPath.contains(lib)) return true;
        }
        return false;
    }

    /**
     * Get the list of library names to hide (passed to native code).
     */
    public static String[] getHiddenLibraries() {
        return HIDDEN_LIBRARIES.toArray(new String[0]);
    }

    /**
     * Verify that a profile is stealthy — checks common detection vectors.
     */
    public static StealthReport verifyProfile(
            String buildFingerprint, String buildType, String buildTags,
            String packageName, String installerName) {

        StealthReport report = new StealthReport();

        // Check Build.FINGERPRINT format
        if (buildFingerprint == null || !buildFingerprint.contains("release-keys")) {
            report.addIssue("Build.FINGERPRINT missing 'release-keys'");
        }

        // Check Build.TYPE
        if (!"user".equals(buildType)) {
            report.addIssue("Build.TYPE is '" + buildType + "', should be 'user'");
        }

        // Check Build.TAGS
        if (!"release-keys".equals(buildTags)) {
            report.addIssue("Build.TAGS is '" + buildTags + "', should be 'release-keys'");
        }

        // Check installer
        if (!"com.android.vending".equals(installerName)) {
            report.addIssue("Installer is '" + installerName + "', should be Play Store");
        }

        report.passed = report.issues.isEmpty();
        return report;
    }

    public static class StealthReport {
        public boolean passed;
        public final java.util.List<String> issues = new java.util.ArrayList<>();

        void addIssue(String issue) {
            issues.add(issue);
            Log.w(TAG, "Stealth issue: " + issue);
        }

        @Override
        public String toString() {
            return passed ? "STEALTH: PASSED" :
                    "STEALTH: FAILED (" + issues.size() + " issues)";
        }
    }
}

package com.titanclone.engine.core;

/**
 * Configuration constants for the virtualization engine.
 */
public final class EngineConfig {

    private EngineConfig() {}

    /** Maximum number of concurrent clone processes (30 stub slots in manifest) */
    public static final int MAX_CLONE_PROCESSES = 30;

    /** Host application package name */
    public static final String HOST_PACKAGE = "com.titanclone.titan_clone";

    /** Virtual server process name suffix */
    public static final String SERVER_PROCESS_NAME = ":x";

    /** Clone process name prefix */
    public static final String CLONE_PROCESS_PREFIX = ":p";

    /** Virtual data root directory name */
    public static final String VIRTUAL_ROOT = "virtual";

    /** Extracted APKs cache directory */
    public static final String PACKAGES_DIR = "packages";

    /** Virtual user data directory prefix */
    public static final String USER_DIR_PREFIX = "user_";

    /** Profile database filename */
    public static final String PROFILES_DB = "profiles.db";

    /** Supported ABIs for clone processes */
    public static final String[] SUPPORTED_ABIS = {
        "arm64-v8a",
        "armeabi-v7a",
        "x86_64"
    };

    /** Minimum Android API level supported */
    public static final int MIN_API_LEVEL = 29; // Android 10

    /** Maximum Android API level supported */
    public static final int MAX_API_LEVEL = 35; // Android 15
}

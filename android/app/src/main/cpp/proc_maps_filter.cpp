/**
 * /proc/self/maps Filter — hides engine-related memory mappings
 * from clone processes to prevent detection.
 *
 * Anti-detection apps read /proc/self/maps to look for:
 * - Xposed framework signatures
 * - Frida injection
 * - VirtualApp/BlackBox engine libraries
 * - TitanClone engine .so files
 *
 * This module hooks file reads of /proc/self/maps and filters out
 * any lines containing engine-related paths or signatures.
 *
 * Hook mechanism: PLT hook of fopen() to intercept opens of
 * /proc/self/maps, replacing with a filtered temporary file.
 */

#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <algorithm>
#include <mutex>
#include <fstream>
#include <sstream>

#define LOG_TAG "ProcMapsFilter"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace titan_clone {

static std::vector<std::string> g_filter_patterns;
static std::mutex g_filter_mutex;
static bool g_filter_enabled = false;

/**
 * Check if a /proc/self/maps line should be filtered.
 */
static bool shouldFilterLine(const std::string &line) {
    std::string lower = line;
    std::transform(lower.begin(), lower.end(), lower.begin(), ::tolower);

    for (const auto &pattern : g_filter_patterns) {
        if (lower.find(pattern) != std::string::npos) {
            return true;
        }
    }
    return false;
}

/**
 * Read /proc/self/maps and return filtered content.
 */
static std::string getFilteredMaps() {
    std::ifstream maps("/proc/self/maps");
    std::ostringstream filtered;

    std::string line;
    int totalLines = 0;
    int filteredLines = 0;

    std::lock_guard<std::mutex> lock(g_filter_mutex);
    while (std::getline(maps, line)) {
        totalLines++;
        if (shouldFilterLine(line)) {
            filteredLines++;
            continue;
        }
        filtered << line << "\n";
    }

    if (filteredLines > 0) {
        LOGD("Filtered %d/%d lines from /proc/self/maps", filteredLines, totalLines);
    }

    return filtered.str();
}

} // namespace titan_clone

extern "C" {

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeInitMapsFilter(
    JNIEnv * /* env */, jobject /* this */) {

    std::lock_guard<std::mutex> lock(titan_clone::g_filter_mutex);

    // Default patterns to filter from /proc/self/maps
    titan_clone::g_filter_patterns = {
        "titanclone",
        "libtitanclone",
        "libio_redirect",
        "libbinder_intercept",
        "libproperty_redirect",
        "libmemory_manager",
        "libproc_maps_filter",
        "blackbox",
        "virtualapp",
        "xposed",
        "edxposed",
        "lsposed",
        "substrate",
        "frida",
        "libhook",
        "magisk",
        "riru",
        "zygisk",
    };

    titan_clone::g_filter_enabled = true;
    LOGI("Proc maps filter initialized with %zu patterns",
         titan_clone::g_filter_patterns.size());
}

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeAddFilterPattern(
    JNIEnv *env, jobject /* this */, jstring pattern) {

    const char *patternStr = env->GetStringUTFChars(pattern, nullptr);
    {
        std::lock_guard<std::mutex> lock(titan_clone::g_filter_mutex);
        titan_clone::g_filter_patterns.emplace_back(patternStr);
    }
    env->ReleaseStringUTFChars(pattern, patternStr);
}

JNIEXPORT jboolean JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeShouldFilterLine(
    JNIEnv *env, jobject /* this */, jstring line) {

    if (!titan_clone::g_filter_enabled) return JNI_FALSE;

    const char *lineStr = env->GetStringUTFChars(line, nullptr);
    std::string lineString(lineStr);
    env->ReleaseStringUTFChars(line, lineStr);

    std::lock_guard<std::mutex> lock(titan_clone::g_filter_mutex);
    return titan_clone::shouldFilterLine(lineString) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeGetFilteredMaps(
    JNIEnv *env, jobject /* this */) {

    if (!titan_clone::g_filter_enabled) return nullptr;

    std::string filtered = titan_clone::getFilteredMaps();
    return env->NewStringUTF(filtered.c_str());
}

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeSetFilterPatterns(
    JNIEnv *env, jobject /* this */, jobjectArray patterns) {

    jsize count = env->GetArrayLength(patterns);
    std::lock_guard<std::mutex> lock(titan_clone::g_filter_mutex);
    titan_clone::g_filter_patterns.clear();

    for (jsize i = 0; i < count; i++) {
        jstring pattern = (jstring) env->GetObjectArrayElement(patterns, i);
        const char *str = env->GetStringUTFChars(pattern, nullptr);
        titan_clone::g_filter_patterns.emplace_back(str);
        env->ReleaseStringUTFChars(pattern, str);
        env->DeleteLocalRef(pattern);
    }

    LOGI("Filter patterns updated: %zu patterns", titan_clone::g_filter_patterns.size());
}

} // extern "C"

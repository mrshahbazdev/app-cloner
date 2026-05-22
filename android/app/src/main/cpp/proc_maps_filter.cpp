/**
 * /proc/self/maps Filter — hides engine-related memory mappings
 * from clone processes to prevent detection.
 *
 * Anti-detection apps read /proc/self/maps to look for:
 * - Xposed framework signatures
 * - Frida injection
 * - VirtualApp/BlackBox engine libraries
 * - Suspicious memory regions
 *
 * This module hooks file reads of /proc/self/maps and filters out
 * any lines containing engine-related paths or signatures.
 *
 * TODO: Implement /proc/self/maps read interception.
 */

#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <algorithm>

#define LOG_TAG "ProcMapsFilter"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace titan_clone {

static std::vector<std::string> g_filter_patterns;
static bool g_filter_enabled = false;

} // namespace titan_clone

extern "C" {

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeInitMapsFilter(
    JNIEnv * /* env */, jobject /* this */) {

    // Default patterns to filter from /proc/self/maps
    titan_clone::g_filter_patterns = {
        "titanclone",
        "blackbox",
        "virtualapp",
        "xposed",
        "substrate",
        "frida",
        "libhook",
    };

    titan_clone::g_filter_enabled = true;
    LOGI("Proc maps filter initialized with %zu patterns",
         titan_clone::g_filter_patterns.size());
}

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeAddFilterPattern(
    JNIEnv *env, jobject /* this */, jstring pattern) {

    const char *patternStr = env->GetStringUTFChars(pattern, nullptr);
    titan_clone::g_filter_patterns.emplace_back(patternStr);
    env->ReleaseStringUTFChars(pattern, patternStr);

    LOGD("Filter pattern added: %s", patternStr);
}

JNIEXPORT jboolean JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeShouldFilterLine(
    JNIEnv *env, jobject /* this */, jstring line) {

    if (!titan_clone::g_filter_enabled) return JNI_FALSE;

    const char *lineStr = env->GetStringUTFChars(line, nullptr);
    std::string lineString(lineStr);
    env->ReleaseStringUTFChars(line, lineStr);

    // Convert to lowercase for case-insensitive matching
    std::transform(lineString.begin(), lineString.end(),
                   lineString.begin(), ::tolower);

    for (const auto &pattern : titan_clone::g_filter_patterns) {
        if (lineString.find(pattern) != std::string::npos) {
            return JNI_TRUE;
        }
    }

    return JNI_FALSE;
}

} // extern "C"

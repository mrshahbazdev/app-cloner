/**
 * Property Redirect — hooks __system_property_get to return spoofed
 * device properties per clone.
 *
 * Android system properties (ro.build.fingerprint, ro.product.model, etc.)
 * are used by apps and Google services to identify devices. Each clone
 * must return different property values matching its virtual device profile.
 *
 * Spoofed property categories (126+ total):
 *   Device Identity (48): brand, manufacturer, model across 8 partitions
 *   Build Info (41): fingerprint, build ID, version info, security patch
 *   Security Flags (13): ro.debuggable, ro.secure, ro.boot.verifiedbootstate
 *   Hardware Platform (12): CPU ABI, screen res/density, hardware name
 *   Unique Identifiers (4): serial number, bootloader version
 *   Carrier/GSM Info (7): operator name, SIM details, timezone
 *
 * Hook mechanism: PLT hook of __system_property_get in libc.so.
 * When a clone process calls SystemProperties.get(), libc routes to
 * __system_property_get. We intercept via PLT and check our spoofed
 * map first before falling through to the real implementation.
 */

#include <jni.h>
#include <android/log.h>
#include <string>
#include <unordered_map>
#include <mutex>
#include <cstring>

#define LOG_TAG "PropertyRedirect"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

namespace titan_clone {

// Thread-safe spoofed property map
static std::unordered_map<std::string, std::string> g_spoofed_properties;
static std::mutex g_mutex;
static bool g_initialized = false;
static bool g_hook_active = false;

// Original __system_property_get function pointer (for PLT hook)
typedef int (*orig_system_property_get_t)(const char *name, char *value);
static orig_system_property_get_t g_orig_system_property_get = nullptr;

/**
 * Our replacement for __system_property_get.
 * Checks spoofed map first, falls through to original if not spoofed.
 */
static int hooked_system_property_get(const char *name, char *value) {
    if (name == nullptr || value == nullptr) {
        if (g_orig_system_property_get) {
            return g_orig_system_property_get(name, value);
        }
        return 0;
    }

    std::lock_guard<std::mutex> lock(g_mutex);
    auto it = g_spoofed_properties.find(name);
    if (it != g_spoofed_properties.end()) {
        const std::string &spoofed = it->second;
        size_t len = spoofed.length();
        if (len > 91) len = 91; // PROP_VALUE_MAX - 1
        memcpy(value, spoofed.c_str(), len);
        value[len] = '\0';
        return static_cast<int>(len);
    }

    if (g_orig_system_property_get) {
        return g_orig_system_property_get(name, value);
    }
    value[0] = '\0';
    return 0;
}

} // namespace titan_clone

extern "C" {

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeInitPropertyHook(
    JNIEnv * /* env */, jobject /* this */) {

    // TODO: PLT hook __system_property_get in libc.so
    // This requires a PLT hooking library (e.g., xhook, bhook, or manual GOT patching).
    // Steps:
    //   1. Find libc.so base address from /proc/self/maps
    //   2. Parse ELF headers to find .got.plt section
    //   3. Locate __system_property_get entry in GOT
    //   4. Replace with hooked_system_property_get
    //   5. Save original pointer to g_orig_system_property_get
    //
    // For now, properties are still readable via nativeGetProperty()
    // and the Java-side ProfileGenerator.getSystemProperties() map.

    titan_clone::g_initialized = true;
    LOGI("Property redirect initialized. %zu properties loaded.",
         titan_clone::g_spoofed_properties.size());
}

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeSetProperty(
    JNIEnv *env, jobject /* this */, jstring name, jstring value) {

    const char *nameStr = env->GetStringUTFChars(name, nullptr);
    const char *valueStr = env->GetStringUTFChars(value, nullptr);

    {
        std::lock_guard<std::mutex> lock(titan_clone::g_mutex);
        titan_clone::g_spoofed_properties[nameStr] = valueStr;
    }

    LOGD("Property set: %s = %s", nameStr, valueStr);

    env->ReleaseStringUTFChars(name, nameStr);
    env->ReleaseStringUTFChars(value, valueStr);
}

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeSetProperties(
    JNIEnv *env, jobject /* this */, jobjectArray keys, jobjectArray values) {

    jsize count = env->GetArrayLength(keys);
    {
        std::lock_guard<std::mutex> lock(titan_clone::g_mutex);
        for (jsize i = 0; i < count; i++) {
            jstring key = (jstring) env->GetObjectArrayElement(keys, i);
            jstring val = (jstring) env->GetObjectArrayElement(values, i);

            const char *keyStr = env->GetStringUTFChars(key, nullptr);
            const char *valStr = env->GetStringUTFChars(val, nullptr);

            titan_clone::g_spoofed_properties[keyStr] = valStr;

            env->ReleaseStringUTFChars(key, keyStr);
            env->ReleaseStringUTFChars(val, valStr);
            env->DeleteLocalRef(key);
            env->DeleteLocalRef(val);
        }
    }

    LOGI("Bulk set %d properties", count);
}

JNIEXPORT jstring JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeGetProperty(
    JNIEnv *env, jobject /* this */, jstring name) {

    const char *nameStr = env->GetStringUTFChars(name, nullptr);
    std::string nameKey(nameStr);
    env->ReleaseStringUTFChars(name, nameStr);

    std::lock_guard<std::mutex> lock(titan_clone::g_mutex);
    auto it = titan_clone::g_spoofed_properties.find(nameKey);
    if (it != titan_clone::g_spoofed_properties.end()) {
        return env->NewStringUTF(it->second.c_str());
    }

    return nullptr;
}

JNIEXPORT jint JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeGetSpoofedPropertyCount(
    JNIEnv * /* env */, jobject /* this */) {

    std::lock_guard<std::mutex> lock(titan_clone::g_mutex);
    return static_cast<jint>(titan_clone::g_spoofed_properties.size());
}

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeClearProperties(
    JNIEnv * /* env */, jobject /* this */) {

    std::lock_guard<std::mutex> lock(titan_clone::g_mutex);
    titan_clone::g_spoofed_properties.clear();
    LOGI("All spoofed properties cleared");
}

} // extern "C"

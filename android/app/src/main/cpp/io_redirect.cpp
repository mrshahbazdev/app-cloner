/**
 * IO Redirect — syscall hooking for file path redirection.
 *
 * Intercepts file system operations (open, openat, stat, access, etc.)
 * and redirects paths from the real app data directory to the clone's
 * isolated storage directory.
 *
 * Example:
 *   Clone reads /data/data/com.whatsapp/databases/msgstore.db
 *   -> Redirected to /data/data/com.titanclone/virtual/user_0/com.whatsapp/databases/msgstore.db
 *
 * TODO: Implement PLT hooking or inline hooking for syscall interception.
 * Reference: BlackBox's IO redirection layer.
 */

#include <jni.h>
#include <android/log.h>
#include <string>
#include <unordered_map>

#define LOG_TAG "IORedirect"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace titan_clone {

static std::string g_virtual_root;
static std::unordered_map<std::string, std::string> g_redirect_map;

} // namespace titan_clone

extern "C" {

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeInitIORedirect(
    JNIEnv *env, jobject /* this */, jstring virtualRoot) {

    const char *root = env->GetStringUTFChars(virtualRoot, nullptr);
    titan_clone::g_virtual_root = root;
    env->ReleaseStringUTFChars(virtualRoot, root);

    LOGD("IO redirect initialized. Virtual root: %s",
         titan_clone::g_virtual_root.c_str());
}

JNIEXPORT void JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeAddRedirectRule(
    JNIEnv *env, jobject /* this */, jstring fromPath, jstring toPath) {

    const char *from = env->GetStringUTFChars(fromPath, nullptr);
    const char *to = env->GetStringUTFChars(toPath, nullptr);

    titan_clone::g_redirect_map[from] = to;

    LOGD("Redirect rule added: %s -> %s", from, to);

    env->ReleaseStringUTFChars(fromPath, from);
    env->ReleaseStringUTFChars(toPath, to);
}

JNIEXPORT jstring JNICALL
Java_com_titanclone_engine_core_VirtualCore_nativeRedirectPath(
    JNIEnv *env, jobject /* this */, jstring originalPath) {

    const char *path = env->GetStringUTFChars(originalPath, nullptr);
    std::string pathStr(path);
    env->ReleaseStringUTFChars(originalPath, path);

    // Check redirect map for exact match
    auto it = titan_clone::g_redirect_map.find(pathStr);
    if (it != titan_clone::g_redirect_map.end()) {
        return env->NewStringUTF(it->second.c_str());
    }

    // Check prefix-based redirection
    for (const auto &rule : titan_clone::g_redirect_map) {
        if (pathStr.find(rule.first) == 0) {
            std::string redirected = rule.second + pathStr.substr(rule.first.length());
            return env->NewStringUTF(redirected.c_str());
        }
    }

    return env->NewStringUTF(pathStr.c_str());
}

} // extern "C"

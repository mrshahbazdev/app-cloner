# TitanClone ProGuard rules

# Keep engine core classes (needed for reflection)
-keep class com.titanclone.engine.** { *; }

# Keep stealth classes (reflection-heavy)
-keep class com.titanclone.engine.stealth.** { *; }

# Keep stub components (manifest-declared)
-keep class com.titanclone.engine.stub.** { *; }

# Keep bridge plugin
-keep class com.titanclone.titan_clone.bridge.** { *; }

# Keep profile classes (serialized to JSON)
-keep class com.titanclone.titan_clone.profile.** { *; }
-keep class com.titanclone.titan_clone.profile.db.** { *; }

# Keep GMS classes
-keep class com.titanclone.titan_clone.gms.** { *; }

# Keep notification classes
-keep class com.titanclone.titan_clone.notification.** { *; }

# Keep discovery manager
-keep class com.titanclone.titan_clone.discovery.** { *; }

# Keep JNI native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Flutter classes
-keep class io.flutter.** { *; }
-dontwarn io.flutter.**

# Obfuscate engine internals to prevent detection
# This is critical — apps scan stack traces for known virtualization frameworks
-repackageclasses ''
-allowaccessmodification

# Specifically obfuscate class names that anti-detection checks look for
-obfuscationdictionary proguard-dict.txt
-classobfuscationdictionary proguard-dict.txt

# Don't warn about Android framework internals we access via reflection
-dontwarn android.os.Build
-dontwarn android.os.Build$VERSION
-dontwarn android.os.ServiceManager
-dontwarn android.app.ActivityThread
-dontwarn android.content.pm.IPackageManager

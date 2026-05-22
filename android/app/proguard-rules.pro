# TitanClone ProGuard rules

# Keep engine core classes
-keep class com.titanclone.engine.** { *; }

# Keep bridge plugin
-keep class com.titanclone.titan_clone.bridge.** { *; }

# Keep profile classes
-keep class com.titanclone.titan_clone.profile.** { *; }

# Keep GMS classes
-keep class com.titanclone.titan_clone.gms.** { *; }

# Keep notification classes
-keep class com.titanclone.titan_clone.notification.** { *; }

# Keep JNI native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Flutter classes
-keep class io.flutter.** { *; }
-dontwarn io.flutter.**

# Obfuscate engine internals to prevent detection
-repackageclasses ''
-allowaccessmodification

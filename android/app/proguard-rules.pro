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

# Keep service classes (foreground service, receivers)
-keep class com.titanclone.titan_clone.service.** { *; }

# Keep compat/edge case classes
-keep class com.titanclone.titan_clone.compat.** { *; }

# Keep security classes (needed for runtime checks)
-keep class com.titanclone.titan_clone.security.CodeProtection { *; }
-keep class com.titanclone.titan_clone.security.DataSecurity { *; }
-keep class com.titanclone.titan_clone.security.DataSecurity$EncryptedData { *; }

# Keep optimization classes (public API only)
-keep class com.titanclone.titan_clone.optimization.MemoryOptimizer$MemorySnapshot { *; }
-keep class com.titanclone.titan_clone.optimization.StartupOptimizer$TimingMetrics { *; }
-keep class com.titanclone.titan_clone.optimization.BatteryOptimizer$BatteryStatus { *; }

# Obfuscate optimization internals
-keepclassmembers class com.titanclone.titan_clone.optimization.** {
    public <methods>;
}

# Keep JNI native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Flutter classes
-keep class io.flutter.** { *; }
-dontwarn io.flutter.**

# Keep AndroidX classes used by the app
-keep class androidx.core.app.NotificationCompat** { *; }
-dontwarn androidx.core.**

# Obfuscate engine internals to prevent detection
# This is critical — apps scan stack traces for known virtualization frameworks
-repackageclasses ''
-allowaccessmodification

# Aggressive optimization
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Specifically obfuscate class names that anti-detection checks look for
-obfuscationdictionary proguard-dict.txt
-classobfuscationdictionary proguard-dict.txt

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Don't warn about Android framework internals we access via reflection
-dontwarn android.os.Build
-dontwarn android.os.Build$VERSION
-dontwarn android.os.ServiceManager
-dontwarn android.app.ActivityThread
-dontwarn android.content.pm.IPackageManager
-dontwarn javax.crypto.**

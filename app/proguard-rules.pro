# Aminmart Password Manager - ProGuard Rules
# Optimized for small APK size while maintaining functionality

# ===========================================
# General optimizations
# ===========================================
-dontoptimize
-dontobfuscate

# ===========================================
# Google Error Prone Annotations (missing from Tink)
# ===========================================
-dontwarn com.google.errorprone.annotations.**

# ===========================================
# Hilt (Dependency Injection)
# ===========================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-keep class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# ===========================================
# Kotlin
# ===========================================
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class com.aminmart.passwordmanager.data.repository.**$* {
    *** Companion;
}
-keepclassmembers class com.aminmart.passwordmanager.data.repository.** {
    *** Companion;
}
-keep class com.aminmart.passwordmanager.data.repository.BackupFileV1 { *; }
-keep class com.aminmart.passwordmanager.data.repository.BackupPayloadV1 { *; }
-keep class com.aminmart.passwordmanager.data.repository.PasswordBackupItem { *; }

# ===========================================
# Kotlinx Serialization
# ===========================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.json.** { *; }
-dontwarn kotlinx.serialization.json.**
-dontwarn kotlinx.serialization.internal.**

# ===========================================
# AndroidX Security Crypto
# ===========================================
-keep class androidx.security.crypto.** { *; }
-keep class androidx.security.** { *; }

# ===========================================
# Biometric
# ===========================================
-keep class androidx.biometric.** { *; }

# ===========================================
# Hilt Generated Classes
# ===========================================
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends dagger.hilt.android.AndroidEntryPoint { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# ===========================================
# Keep model classes
# ===========================================
-keep class com.aminmart.passwordmanager.domain.model.** { *; }
-keep class com.aminmart.passwordmanager.data.local.** { *; }
-keep class com.aminmart.passwordmanager.data.security.** { *; }
-keep class com.aminmart.passwordmanager.data.repository.** { *; }

# ===========================================
# Compose
# ===========================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers,allowobfuscation class * extends androidx.compose.runtime.Composer {
    void <init>(java.lang.Object...);
}

# ===========================================
# Remove logging for release builds
# ===========================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

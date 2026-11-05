# ============================================================================
# Aura Wallpapers: Production ProGuard / R8 Obfuscation & Shrinking Rules
# ============================================================================

# Retrofit 2 & OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Gson Serialization DTOs
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !static !transient <methods>;
    !private <methods>;
    <init>();
}
-keep class com.aura.wallpapers.data.remote.dto.** { *; }
-keepclassmembers class com.aura.wallpapers.data.remote.dto.** { <fields>; }

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public abstract <methods>;
}
-dontwarn androidx.room.paging.**

# AndroidX Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keepattributes *Annotation*

# Coil ImageLoader
-keep class coil.** { *; }
-dontwarn coil.**

# Hilt & Dagger
-keep class * extends com.google.dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn com.google.errorprone.annotations.**

# Native Wallpaper Service
-keep class com.aura.wallpapers.engine.AuraLiveWallpaperService { *; }
-keep class com.aura.wallpapers.engine.AuraLiveWallpaperService$* { *; }

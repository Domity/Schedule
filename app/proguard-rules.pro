# ============================================================
#  Schedule – aggressive R8 configuration for cold-start speed
# ============================================================

# --- Global optimization flags -------------------------------------------------
# Remove all logging in release (shrinks code + avoids string/IO work at runtime)
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
    public static *** println(...);
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(java.lang.Object);
    public static void checkNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
}

# Kotlin metadata/annotations are not needed at runtime for this app
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Allow R8 to merge/verticalize aggressively
-allowaccessmodification
-repackageclasses ''

# Be lenient about optional references pulled in by libraries
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlinx.**
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------
# Room – keep generated DB / DAO implementations
# ---------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
# The entity is accessed reflectively by Room-generated code
-keep class io.github.domity.schedule.data.CourseEntity { *; }

# ---------------------------------------------------------------
# DataStore – only the actual protobuf serializers need keeping.
# We dropped the broad `-keep class androidx.datastore.**` rule so R8
# can shrink/optimize the rest of the library.
# ---------------------------------------------------------------
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-dontwarn androidx.datastore.**

# ---------------------------------------------------------------
# Compose – the plugin ships its own consumer rules; only keep the
# runtime annotation used for recomposition tracking is required and
# is handled automatically. No blanket keeps.
# ---------------------------------------------------------------

# ---------------------------------------------------------------
# AndroidX startup / profiler placeholders (optional classes)
# ---------------------------------------------------------------
-dontwarn androidx.profileinstaller.**

# ---------------------------------------------------------------
# Enum / serialization safety
# ---------------------------------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

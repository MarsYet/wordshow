# Add project specific ProGuard rules here.

# Keep Compose composable methods
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep data classes used in serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

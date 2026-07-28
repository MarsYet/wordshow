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

# 讯飞 SparkChain SDK
-keep class com.iflytek.sparkchain.** {*;}
-keep class com.iflytek.sparkchain.**
# SparkChain 内部引用 Gson 注解但 AAR 未打包 Gson 库，避免 R8 报 missing class
-dontwarn com.google.gson.annotations.SerializedName

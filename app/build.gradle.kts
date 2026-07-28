import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.xiao.wordshow"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.xiao.wordshow"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystoreProps = Properties()
    val keystoreFile = rootProject.file("keystore.properties")
    if (keystoreFile.exists()) keystoreFile.inputStream().use { keystoreProps.load(it) }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProps.getProperty("storeFile", "kanjian.jks"))
            storePassword = keystoreProps.getProperty("storePassword", "")
            keyAlias = keystoreProps.getProperty("keyAlias", "kanjian")
            keyPassword = keystoreProps.getProperty("keyPassword", "")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // 从 local.properties 读取密钥（不提交到版本控制）
    val localProps = Properties()
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) localPropsFile.inputStream().use { localProps.load(it) }
    val sparkchainAppId = localProps.getProperty("SPARKCHAIN_APP_ID", "")
    val sparkchainApiKey = localProps.getProperty("SPARKCHAIN_API_KEY", "")
    val sparkchainApiSecret = localProps.getProperty("SPARKCHAIN_API_SECRET", "")

    defaultConfig {
        buildConfigField("String", "SPARKCHAIN_APP_ID", "\"$sparkchainAppId\"")
        buildConfigField("String", "SPARKCHAIN_API_KEY", "\"$sparkchainApiKey\"")
        buildConfigField("String", "SPARKCHAIN_API_SECRET", "\"$sparkchainApiSecret\"")
    }
}

dependencies {
    // 讯飞 SparkChain SDK
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
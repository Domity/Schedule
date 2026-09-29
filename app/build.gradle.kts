plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "io.github.domity.schedule"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.domity.schedule"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters.addAll(listOf("arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // R8 full mode: more aggressive tree-shaking, fewer kept classes, faster startup
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            // keep default
        }
    }
    buildFeatures {
        compose = true
        // BuildConfig is never referenced -> don't generate it
        buildConfig = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/*.version"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "DebugProbesKt.bin"
            excludes += "kotlin-tooling-metadata.json"
        }
        dex {
            // Store DEX uncompressed so it is mmap-able (no copy on startup)
            useLegacyPackaging = false
        }
    }

    // Strip dependency metadata blocks from the APK
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    // Only keep the ABI we actually ship
    splits {
        abi {
            isEnable = false
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.ui)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    constraints {
        implementation("androidx.core:core:1.13.1") {
            because("AGP 8.7.3 does not support core 1.19.1")
        }
        implementation("androidx.core:core-ktx:1.13.1") {
            because("AGP 8.7.3 does not support core-ktx 1.19.1")
        }
    }
}
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.maksimowiczm.zebra.core.data.api"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}


dependencies {
    // Result monad
    implementation(libs.kotlin.result)

    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":zebra:core:zebra-signal"))
}
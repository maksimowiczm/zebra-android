plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {

    // OkHttp
    implementation(libs.okhttp)

    // kotlin coroutines
    implementation(libs.kotlinx.coroutines.core)

    // result monad
    implementation(libs.kotlin.result)

    // serialization
    implementation(libs.kotlinx.serialization.json)
}
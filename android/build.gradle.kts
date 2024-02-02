plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(project(":reorderable"))
    implementation("androidx.compose.runtime:runtime:1.6.0")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("io.coil-kt:coil-compose:2.5.0")
}

android {

    sourceSets {
        map { it.java.srcDir("src/${it.name}/kotlin") }
    }

    val minSdkVersion: Int by rootProject.extra
    val targetSdkVersion: Int by rootProject.extra
    val compileSdkVersion: Int by rootProject.extra
    defaultConfig {
        minSdk =  minSdkVersion
        targetSdk = targetSdkVersion
        compileSdk = compileSdkVersion
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11" //""1.8"
    }
    namespace = "org.burnoutcrew.android"
    buildToolsVersion = "34.0.0"
}

plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation("org.burnoutcrew.composereorderable:reorderable:0.8.1")
    implementation("androidx.compose.runtime:runtime:1.2.0-beta01")
    implementation("androidx.compose.material:material:1.2.0-beta02")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("com.google.android.material:material:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1")
    implementation("androidx.navigation:navigation-compose:2.5.0-rc01")
    implementation("io.coil-kt:coil-compose:1.4.0")
}

android {

    sourceSets {
        map { it.java.srcDir("src/${it.name}/kotlin") }
    }

    compileSdk = rootProject.extra.get("compileSdk") as Int
    defaultConfig {
        minSdk =  rootProject.extra.get("minVersion") as Int
        targetSdk = rootProject.extra.get("targetSdk") as Int
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    namespace = "org.burnoutcrew.android"
}
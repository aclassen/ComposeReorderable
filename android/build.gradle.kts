import org.jetbrains.compose.compose

plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(project(":reorderable"))
    implementation(compose.runtime)
    implementation(compose.material)
    implementation("androidx.activity:activity-compose:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0-rc01")
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
}
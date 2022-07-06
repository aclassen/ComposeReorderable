plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(project(":reorderable"))
    implementation("androidx.compose.runtime:runtime:1.2.0-beta03")
    implementation("androidx.compose.material:material:1.3.0-alpha01")
    implementation("androidx.activity:activity-compose:1.5.0")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.0")
    implementation("androidx.navigation:navigation-compose:2.5.0")
    implementation("io.coil-kt:coil-compose:2.1.0")
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
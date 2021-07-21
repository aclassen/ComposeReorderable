plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = rootProject.extra.get("compileSdk") as Int


    defaultConfig {
        applicationId = "io.burnoutcrew.lazyreorderlist"
        minSdk =  rootProject.extra.get("minVersion") as Int
        targetSdk = rootProject.extra.get("targetSdk") as Int

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
}

dependencies {
    implementation(project(mapOf("path" to ":reorderable")))
    implementation(libs.material)
    implementation(libs.androidx.viewmodel)
    implementation(libs.bundles.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.androidx.test)
}
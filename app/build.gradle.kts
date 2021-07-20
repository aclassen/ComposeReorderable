plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = Versions.compile_sdk

    defaultConfig {
        applicationId = "io.burnoutcrew.lazyreorderlist"
        minSdk = Versions.min_sdk
        targetSdk = Versions.target_sdk
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
        kotlinCompilerExtensionVersion = Versions.compose
    }
}

dependencies {
    implementation(project(mapOf("path" to ":reorderable")))
    implementation(Deps.material)
    implementation(Deps.lifecycle_viewmodel)
    implementation(Deps.Compose.ui)
    implementation(Deps.Compose.material)
    implementation(Deps.Compose.activity)
    implementation(Deps.Compose.lifecycle_viewmodel)
    testImplementation(Deps.junit)
    androidTestImplementation(Deps.AndroidXTest.junit)
    androidTestImplementation(Deps.AndroidXTest.espresso_core)
    androidTestImplementation(Deps.AndroidXTest.ui_test)
}
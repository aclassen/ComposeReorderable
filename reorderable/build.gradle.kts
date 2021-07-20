plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

android {
    compileSdk = Versions.compile_sdk

    defaultConfig {
        minSdk = Versions.min_sdk
        targetSdk = Versions.target_sdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
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
    implementation(Deps.Compose.material)
    testImplementation(Deps.junit)
    androidTestImplementation(Deps.AndroidXTest.junit)
    androidTestImplementation(Deps.AndroidXTest.espresso_core)

}

sourceSets.create("main") {
    java.srcDir("src/main/kotlin")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.convention("sources")
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("debug") {
                groupId = "com.github.aclassen"
                version = "0.1"
                from(components["debug"])
                artifact(sourcesJar)
            }
        }
    }
}
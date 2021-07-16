plugins {
    id("com.android.library")
    id("kotlin-android")
    id ("maven-publish")
}

android {
    compileSdk = 30


    defaultConfig {
        minSdk = 23
        targetSdk = 30
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
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
    }
}

dependencies {
    implementation("androidx.compose.material:material:${rootProject.extra["compose_version"]}")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
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
plugins {
    `maven-publish`
    id("com.android.library") version "8.10.0" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.3.20" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
    id("org.jetbrains.compose") version "1.8.0" apply false
}

ext {
    extra["compileSdkVersion"] = 35
    extra["minSdkVersion"] = 21
    extra["targetSdkVersion"] = 35
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
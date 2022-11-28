plugins {
    `maven-publish`
    id("com.android.library") version "7.3.1" apply false
    id("org.jetbrains.kotlin.multiplatform") version "1.7.22" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false
    id("org.jetbrains.compose") version "1.2.1" apply false
}

ext {
    extra["compileSdkVersion"] = 33
    extra["minSdkVersion"] = 21
    extra["targetSdkVersion"] = 33
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
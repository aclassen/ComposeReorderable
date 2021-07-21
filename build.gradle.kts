buildscript {
    val minVersion by extra(21)
    val targetSdk by extra(30)
    val compileSdk by extra(30)
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0-alpha03")
        classpath(kotlin("gradle-plugin", "1.5.10"))
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
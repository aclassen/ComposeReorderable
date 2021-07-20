// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Deps.android_gradle_plugin)
        classpath(kotlin("gradle-plugin", Versions.kotlin))
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
import org.jetbrains.compose.ComposeBuildConfig.composeVersion
import java.io.FileInputStream
import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("maven-publish")
    id("signing")
}

group = "sg.com.sph.android"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(compose.foundation)
                compileOnly("org.jetbrains.compose.ui:ui-util:${composeVersion}")
            }
        }
    }
}

val javadocJar = tasks.register("javadocJar", Jar::class.java) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/SPHTech/ComposeReorderable")
                credentials {
                    val file = rootProject.file("local.properties")
                    if (file.exists()) {
                        val localProperties = Properties()
                        localProperties.load(FileInputStream(file))
                        username = localProperties.getProperty("GITHUB_USERNAME", System.getenv("GITHUB_USERNAME"))
                        password = localProperties.getProperty("GITHUB_TOKEN", System.getenv("GITHUB_TOKEN"))
                    } else {
                        username = System.getenv("GITHUB_USERNAME")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}


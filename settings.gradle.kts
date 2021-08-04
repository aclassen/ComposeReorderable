rootProject.name = "LazyReorderList"
include(":app")
include(":reorderable")
enableFeaturePreview("VERSION_CATALOGS")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            version("compose", "1.0.0")

            alias("material").to("com.google.android.material", "material").version("1.4.0")
            alias("androidx_viewmodel").to("androidx.lifecycle", "lifecycle-viewmodel-ktx").version("2.3.1")

            alias("compose_viewmodel").to("androidx.lifecycle", "lifecycle-viewmodel-compose").version("1.0.0-alpha07")
            alias("compose_ui").to("androidx.compose.ui", "ui").versionRef("compose")
            alias("compose_material").to("androidx.compose.material", "material").versionRef("compose")
            alias("compose_activity").to("androidx.activity", "activity-compose").version("1.3.0")

            alias("junit").to("junit", "junit").version("4.13.2")
            alias("androidx_test_junit").to("androidx.test.ext", "junit").version("1.1.3")
            alias("androidx_test_espresso_core").to("androidx.test.espresso", "espresso-core").version("3.4.0")
            alias("androidx_test_compose_ui").to("androidx.compose.ui", "ui-test-junit4").versionRef("compose")

            bundle("compose", listOf("compose_viewmodel", "compose_ui", "compose_material","compose_activity"))
            bundle("androidx_test", listOf("androidx_test_junit","androidx_test_espresso_core","androidx_test_compose_ui"))
        }
    }
}
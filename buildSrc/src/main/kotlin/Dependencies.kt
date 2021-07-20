object Versions {
    val min_sdk = 21
    val target_sdk = 30
    val compile_sdk = 30

    val android_gradle_plugin = "7.1.0-alpha03"
    val kotlin = "1.5.10"

    val compose = "1.0.0-rc02"
    val activity_compose = "1.3.0-rc02"
    val lifecycle_viewmodel_compose = "1.0.0-alpha07"
    val material = "1.4.0"
    val lifecycle = "2.3.1"
    val junit = "4.13.2"

    object AndroidX {
        val junit = "1.1.3"
        val espresso_core = "3.4.0"
    }
}

object Deps {
    val android_gradle_plugin = "com.android.tools.build:gradle:${Versions.android_gradle_plugin}"
    val material = "com.google.android.material:material:${Versions.material}"
    val lifecycle_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    val junit = "junit:junit:${Versions.junit}"

    object Compose {
        val lifecycle_viewmodel =
            "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.lifecycle_viewmodel_compose}"
        val ui = "androidx.compose.ui:ui:${Versions.compose}"
        val material = "androidx.compose.material:material:${Versions.compose}"
        val activity = "androidx.activity:activity-compose:${Versions.activity_compose}"
    }

    object AndroidXTest {
        val junit = "androidx.test.ext:junit:${Versions.AndroidX.junit}"
        val espresso_core =
            "androidx.test.espresso:espresso-core:${Versions.AndroidX.espresso_core}"
        val ui_test = "androidx.compose.ui:ui-test-junit4:${Versions.compose}"
    }
}
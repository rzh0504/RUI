plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "rui.ui"
        compileSdk = 37
        minSdk = 23

        withHostTest {
            // Compose's runtime logs through android.util.Log; on the JVM host
            // the stub would throw and mask the actual error, so return
            // defaults instead.
            isReturnDefaultValues = true
        }
    }

    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.ui)
            api(libs.composeunstyled)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.composeunstyled.colored.indication)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            // OkHttp works on the desktop JVM, so the desktop engine matches
            // Android's.
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

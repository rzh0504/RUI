import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

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
        wasmJsMain.dependencies {
            // In Ktor 3.x the fetch-based Js engine ships inside
            // ktor-client-core for the web targets — ktor-client-js is gone.
            implementation(libs.ktor.client.core)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
        }

        // Connect platform actuals to commonMain for the iOS compilations.
        iosMain.get().dependsOn(commonMain.get())
        iosArm64Main.get().dependsOn(iosMain.get())
        iosSimulatorArm64Main.get().dependsOn(iosMain.get())

        // Shared actuals for Skiko-rendered targets (desktop JVM and WebAssembly).
        val skikoMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain.get().dependsOn(skikoMain)
        wasmJsMain.get().dependsOn(skikoMain)
    }
}

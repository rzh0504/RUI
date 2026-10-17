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
        }

        // The default hierarchy template does not link the iOS shared source
        // set into the iOS compilations in this setup (lazy source sets under
        // AGP 9's KMP library plugin — surfaced the first time iOS CI
        // compiled): the platform actuals live in src/iosMain, so connect it
        // explicitly — to commonMain and to both iOS compilations.
        iosMain.get().dependsOn(commonMain.get())
        iosArm64Main.get().dependsOn(iosMain.get())
        iosSimulatorArm64Main.get().dependsOn(iosMain.get())

        // Shared by the Skiko-rendered non-Android targets (desktop JVM and
        // WebAssembly): actuals that need no platform-specific API.
        val skikoMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain.get().dependsOn(skikoMain)
        wasmJsMain.get().dependsOn(skikoMain)
    }
}

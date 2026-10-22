import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "rui.sample.shared"
        compileSdk = 37
        minSdk = 23
    }

    iosArm64()
    iosSimulatorArm64()
    jvm()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        // index.html loads the bundle as catalog.js from the canvas-less
        // #catalog viewport container.
        outputModuleName = "catalog"
        browser {
            commonWebpackConfig {
                outputFileName = "catalog.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":rui"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(libs.compose.ui.backhandler)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        wasmJsMain.dependencies {
            // kotlinx.browser (window/localStorage) for the sample-side
            // actuals; Rui itself never touches browser APIs.
            implementation(libs.kotlinx.browser)
        }

        // Connect shared iOS sources to both iOS compilations.
        iosMain.get().dependsOn(commonMain.get())
        iosArm64Main.get().dependsOn(iosMain.get())
        iosSimulatorArm64Main.get().dependsOn(iosMain.get())
    }

    targets.withType<KotlinNativeTarget> {
        binaries.framework {
            baseName = "SampleShared"
        }
    }
}

// One JVM target, so the Compose Desktop application plugin binds to it:
// ./gradlew :sample:shared:run launches the catalog in a desktop window.
compose.desktop {
    application {
        mainClass = "rui.sample.MainKt"
    }
}

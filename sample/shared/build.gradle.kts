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

    sourceSets {
        commonMain.dependencies {
            implementation(project(":rui-ui"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(libs.compose.ui.backhandler)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
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

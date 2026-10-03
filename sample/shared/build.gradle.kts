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

    sourceSets {
        commonMain.dependencies {
            implementation(project(":rui-ui"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
        }
    }

    targets.withType<KotlinNativeTarget> {
        binaries.framework {
            baseName = "SampleShared"
        }
    }
}

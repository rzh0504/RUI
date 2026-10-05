plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "rui.sample.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "rui.sample.android"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
        resValues = true
    }

    buildTypes {
        debug {
            // Coexists with the release build: own id and launcher label.
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Rui Debug")
        }
        release {
            resValue("string", "app_name", "Rui")
            // Test release builds on real devices without a distribution key.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(project(":sample:shared"))
    implementation(libs.androidx.activity.compose)
}

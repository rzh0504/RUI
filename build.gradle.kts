plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
}

// Empty-group projects cannot be addressed by composite-build dependency
// substitution; consumers substitute module("io.github.rzh0504:rui").
allprojects {
    group = "io.github.rzh0504"
}

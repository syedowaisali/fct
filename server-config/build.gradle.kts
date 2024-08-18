
plugins {
    alias(libs.plugins.jetbrainsKotlinJvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
}

dependencies {
    Dependencies.Compose.getAll().forEach(::implementation)
    //Dependencies.HapiFhir.getAll().forEach(::implementation)

    //implementation(Dependencies.SqlDelight.coroutineExtension)
    implementation(Dependencies.composeView)
    implementation(Dependencies.ApacheCommon.compress)
    implementation(Dependencies.gson)

    implementation(project(":common"))

}


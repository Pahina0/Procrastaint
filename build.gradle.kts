import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.ksp).apply(false)
    kotlin("plugin.serialization") version "2.1.0" apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.aboutLibraries).apply(false)
}

dependencies {
    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.formatting)
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        txt.required.set(true)
    }
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
}
tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.8"
}

val detektAll by tasks.registering(Detekt::class) {
    description = "Run detekt analysis on entire project"

    autoCorrect = true
    parallel = true
    buildUponDefaultConfig = true
    allRules = true
    config.setFrom("$projectDir/config/detekt.yml")

    setSource(files(projectDir))
    include("**/*.kt", "**/*.kts")
    exclude("resources/", "*/build/*")
}

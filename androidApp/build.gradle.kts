plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ap.panini.procrastaint"
    compileSdk = 35
    defaultConfig {
        applicationId = "ap.panini.procrastaint"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "RCOS Expo S25"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    defaultConfig {
        manifestPlaceholders["appAuthRedirectScheme"] = "ap.panini.procrastaint"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.material.icons.extended)

    // destinations
    implementation(libs.destinations.core)
    ksp(libs.destinations.ksp)
    implementation(libs.destinations.bottom.sheet)

    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    implementation(libs.appauth)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.insert.koin.koin.androidx.workmanager)

    implementation(libs.accompanist.permissions)

    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
}

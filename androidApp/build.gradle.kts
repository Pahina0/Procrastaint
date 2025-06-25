plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashalytics)
}

android {
    namespace = "ap.panini.procrastaint"
    compileSdk = 35
    defaultConfig {
        applicationId = "ap.panini.procrastaint"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "0.0.1"
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

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
        }

        debug {
            versionNameSuffix = " debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
}

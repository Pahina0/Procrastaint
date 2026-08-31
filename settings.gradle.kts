enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()

        maven {
            name = "Central Portal Snapshots"
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")

            content {
                includeGroup("io.github.pahinaa.kwhen")
                includeGroupByRegex("io\\.github\\.pahinaa.*")
            }
        }
    }
}

rootProject.name = "Procrastaint"
include(":androidApp")
include(":shared")

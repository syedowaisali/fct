rootProject.name = "fct"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":device-manager")
include(":adb")
include(":logcat")
include(":configs")
include(":database")
include(":common")
include(":package-manager")
include(":structure-map")
include(":editor")
include(":upload")
include(":aurora")
include(":json-tree")
include(":file-manager")
include(":logger")
include(":server-config")
include(":settings")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("../..")
}


includeBuild("../..")


dependencyResolutionManagement {

    repositories {
        mavenCentral()
    }

}

include(":consumer")

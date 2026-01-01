plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")


}

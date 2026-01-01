plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api(project(":libs:maia-lang"))

    api("com.fasterxml.jackson.core:jackson-annotations")


}

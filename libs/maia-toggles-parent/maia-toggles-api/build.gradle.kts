
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    implementation("com.fasterxml.jackson.core:jackson-annotations")

}

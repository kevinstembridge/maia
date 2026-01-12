
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api("tools.jackson.core:jackson-databind")
    api("org.mongodb:bson")

}

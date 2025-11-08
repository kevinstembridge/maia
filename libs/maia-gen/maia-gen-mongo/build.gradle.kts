
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-gen:maia-gen-library"))

    api("org.mongodb:bson")

}

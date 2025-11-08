
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {
    implementation(kotlin("reflect"))
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.mongodb:bson")
}

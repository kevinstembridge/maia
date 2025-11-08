plugins {
    id("maia.kotlin-library-conventions")
}

dependencies {

    api((project(":libs:maia-types")))

    implementation("com.fasterxml.uuid:java-uuid-generator")

}

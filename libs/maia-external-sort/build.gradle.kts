
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api("org.slf4j:slf4j-api")
    implementation(project(":libs:maia-metrics"))

}

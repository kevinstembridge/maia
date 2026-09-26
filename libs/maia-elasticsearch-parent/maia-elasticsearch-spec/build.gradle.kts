
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    implementation(project(":maia-gen:maia-gen-spec"))
    implementation("org.slf4j:slf4j-api")

}



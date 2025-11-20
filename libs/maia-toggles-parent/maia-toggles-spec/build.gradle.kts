
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    implementation(project(":libs:maia-gen:maia-gen-spec"))

    implementation("org.slf4j:slf4j-api")

}



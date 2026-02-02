
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":maia-gen:maia-gen-spec"))

    implementation(project(":maia-gen:maia-gen-generator"))


}

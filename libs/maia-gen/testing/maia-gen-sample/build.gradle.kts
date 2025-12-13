
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {
    implementation(project(":libs:maia-gen:maia-gen-generator"))
    api(project(":libs:maia-gen:maia-gen-spec"))
}

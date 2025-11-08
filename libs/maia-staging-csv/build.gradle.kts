plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-csv"))
    api(project(":libs:maia-id"))
    api(project(":libs:maia-jdbc"))

    implementation(project(":libs:maia-common"))

}

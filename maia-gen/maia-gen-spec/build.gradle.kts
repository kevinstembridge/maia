
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-jdbc"))
    api(project(":libs:maia-lang"))
    api(project(":libs:maia-domain"))

}

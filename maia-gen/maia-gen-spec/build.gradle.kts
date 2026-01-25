
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-lang"))
    api(project(":maia-gen:maia-gen-library"))

}

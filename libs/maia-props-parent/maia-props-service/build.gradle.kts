
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    implementation(project(":libs:maia-common"))
    api(project(":libs:maia-props-parent:maia-props-repo"))

}

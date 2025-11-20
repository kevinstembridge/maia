
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-toggles-parent:maia-toggles-domain"))

    implementation(project(":libs:maia-toggles-parent:maia-toggles-dao"))
    implementation(project(":libs:maia-toggles-parent:maia-toggles-repo"))

}




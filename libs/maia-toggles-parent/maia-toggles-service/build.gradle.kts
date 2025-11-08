

plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-toggles-parent:maia-toggles-dao"))
    api(project(":libs:maia-toggles-parent:maia-toggles-repo"))

}




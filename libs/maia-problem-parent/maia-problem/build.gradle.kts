
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-props-parent:maia-props-api"))

    api("org.springframework:spring-web")

}

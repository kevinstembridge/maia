
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api(project(":libs:maia-problem-parent:maia-problem"))
    api(project(":libs:maia-props-parent:maia-props-api"))

    implementation("org.springframework.boot:spring-boot-starter")

}



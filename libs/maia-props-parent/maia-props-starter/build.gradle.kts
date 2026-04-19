
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-props-parent:maia-props-autoconfigure"))
    api(project(":libs:maia-props-parent:maia-props-api"))
    implementation("org.springframework.boot:spring-boot-starter")

}

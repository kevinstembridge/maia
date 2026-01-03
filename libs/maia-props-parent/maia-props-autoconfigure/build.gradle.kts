
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-props-parent:maia-props-api"))
    api(project(":libs:maia-props-parent:maia-props-repo"))
    implementation("org.springframework.boot:spring-boot-starter")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:3.5.6")

}



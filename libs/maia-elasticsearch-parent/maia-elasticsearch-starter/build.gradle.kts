plugins {
    id("maia.kotlin-library-spring-conventions")
}

dependencies {

    api(project(":libs:maia-elasticsearch-parent:maia-elasticsearch"))
    api(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-autoconfigure"))
    api(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-web"))

    implementation("org.springframework.boot:spring-boot-starter")

}

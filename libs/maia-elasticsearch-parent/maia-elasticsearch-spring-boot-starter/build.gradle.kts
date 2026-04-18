plugins {
    id("maia.kotlin-library-spring-conventions")
}

dependencies {
    api(project(":libs:maia-elasticsearch-parent:maia-elasticsearch"))
    api(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-autoconfigure"))

    implementation("org.springframework.boot:spring-boot-starter")
}

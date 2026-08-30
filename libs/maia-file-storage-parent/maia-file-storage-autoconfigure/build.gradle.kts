
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-file-storage-parent:maia-file-storage"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:3.5.6")

}

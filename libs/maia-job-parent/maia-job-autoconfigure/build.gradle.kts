plugins {
    id("maia.kotlin-library-spring-conventions")
}

dependencies {
    api(project(":libs:maia-job-parent:maia-job"))
    api(project(":libs:maia-job-parent:maia-job-web"))
    implementation(project(":libs:maia-job-parent:maia-job-dao"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:3.5.6")
}

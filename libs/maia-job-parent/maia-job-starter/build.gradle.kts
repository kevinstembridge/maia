plugins {
    id("maia.kotlin-library-spring-conventions")
}

dependencies {
    api(project(":libs:maia-job-parent:maia-job"))
    api(project(":libs:maia-job-parent:maia-job-autoconfigure"))
    api(project(":libs:maia-job-parent:maia-job-web"))
    implementation("org.springframework.boot:spring-boot-starter")
}

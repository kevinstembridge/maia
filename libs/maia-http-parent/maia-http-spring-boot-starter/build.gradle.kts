
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.springframework.boot:spring-boot-starter")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")

}

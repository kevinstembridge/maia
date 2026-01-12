plugins {
    id("maia.kotlin-library-conventions")
}

dependencies {

    api(platform(project(":maia-platform")))

    implementation("tools.jackson.core:jackson-databind")
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("org.skyscreamer:jsonassert")
    implementation("org.springframework:spring-test")
    implementation("org.springframework:spring-web")

}

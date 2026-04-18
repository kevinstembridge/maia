
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    implementation(project(":libs:maia-elasticsearch-parent:maia-elasticsearch"))

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

}

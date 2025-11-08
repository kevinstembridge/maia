
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    implementation(project(":libs:maia-common"))
    implementation(project(":libs:maia-domain"))
    implementation(project(":libs:maia-props-parent:maia-props-api"))
    implementation(project(":libs:maia-webapp:maia-webapp-domain"))
    implementation(project(":libs:maia-webapp:maia-webapp-service"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")

}


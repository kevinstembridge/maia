
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    implementation(project(":libs:maia-common"))
    implementation(project(":libs:maia-domain"))
    implementation(project(":libs:maia-jwt"))
    implementation(project(":libs:maia-webapp:maia-webapp-domain"))

    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.testng:testng")

}


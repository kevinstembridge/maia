
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-props-parent:maia-props-api"))

    implementation(project(":libs:maia-common"))
    implementation(project(":libs:maia-domain"))

    implementation("io.jsonwebtoken:jjwt")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.testng:testng")

}


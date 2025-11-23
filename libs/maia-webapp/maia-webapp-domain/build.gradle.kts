
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-domain"))
    
    api("org.springframework.security:spring-security-core")

    implementation(project(":libs:maia-common"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.testng:testng")

}


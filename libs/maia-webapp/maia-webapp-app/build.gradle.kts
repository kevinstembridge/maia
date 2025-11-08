
plugins {
    id("maia.kotlin-spring-conventions")
    id("maia.kotlin-library-conventions")
}


dependencies {

    implementation(project(":libs:maia-common"))
    api(project(":libs:maia-webapp:maia-webapp-domain"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


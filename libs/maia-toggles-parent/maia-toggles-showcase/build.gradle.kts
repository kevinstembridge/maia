
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-common"))

    api("org.springframework.boot:spring-boot-starter-jdbc")
    api("org.springframework.boot:spring-boot-starter-web")

    implementation(project(":libs:maia-toggles-parent:maia-toggles-spring-boot-starter"))

    runtimeOnly("com.zaxxer:HikariCP")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation(project(":libs:maia-testing:maia-testing-domain"))
    testImplementation(project(":libs:maia-testing:maia-testing-postgresql"))
    testImplementation(project(":libs:maia-testing:maia-testing-spring-test"))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}


tasks.withType<Test> {
    systemProperty("spring.profiles.active", "test")
}


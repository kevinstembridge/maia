plugins {
    id("maia.kotlin-spring-conventions")
}



dependencies {

    implementation(platform(project(":maia-platform")))

    implementation(project(":libs:maia-props-parent:maia-props-spring-boot-starter"))
    implementation(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-spring-boot-starter"))
    implementation(project(":libs:maia-problem-parent:maia-problem-starter"))
    implementation(project(":libs:maia-webapp:maia-webapp-app"))
    implementation(project(":maia-showcase:repo"))
    implementation(project(":maia-showcase:service"))
    implementation(project(":maia-showcase:web"))

    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-hazelcast")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("tools.jackson.module:jackson-module-kotlin")


    runtimeOnly("org.postgresql:postgresql")

    testImplementation(project(":libs:maia-domain"))
    testImplementation(project(":libs:maia-jdbc"))
    testImplementation(project(":libs:maia-testing:maia-testing-domain"))
    testImplementation(project(":libs:maia-testing:maia-testing-postgresql"))
    testImplementation(project(":libs:maia-web-testing"))
    testImplementation(project(":maia-showcase:dao"))
    testImplementation(project(":maia-showcase:domain"))
    testImplementation(project(":maia-showcase:elasticsearch"))

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-hazelcast-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:elasticsearch")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

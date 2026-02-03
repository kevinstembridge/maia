plugins {
    id("maia.kotlin-spring-conventions")
}



dependencies {

    implementation(platform(project(":maia-platform")))

    implementation(project(":libs:maia-props-parent:maia-props-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-hazelcast")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation(project(":libs:maia-domain"))
    testImplementation(project(":libs:maia-jdbc"))
    testImplementation(project(":libs:maia-testing:maia-testing-domain"))
    testImplementation(project(":libs:maia-testing:maia-testing-postgresql"))

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-hazelcast-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

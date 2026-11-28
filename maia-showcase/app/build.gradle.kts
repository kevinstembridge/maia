plugins {
    id("maia.kotlin-spring-conventions")
}



dependencies {

    implementation(platform(project(":maia-platform")))

    implementation(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-starter"))
    implementation(project(":libs:maia-problem-parent:maia-problem-starter"))
    implementation(project(":libs:maia-props-parent:maia-props-starter"))
    implementation(project(":libs:maia-toggles-parent:maia-toggles-starter"))
    implementation(project(":libs:maia-job-parent:maia-job-starter"))
    implementation(project(":libs:maia-webapp:maia-webapp-app"))
    implementation(project(":maia-showcase:jobs"))
    implementation(project(":maia-showcase:repo"))
    implementation(project(":maia-showcase:service"))
    implementation(project(":maia-showcase:web"))

    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-hazelcast")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("tools.jackson.module:jackson-module-kotlin")


    runtimeOnly("org.postgresql:postgresql")

    testImplementation(project(":libs:maia-domain"))
    testImplementation(project(":libs:maia-jdbc"))
    testImplementation(project(":libs:maia-testing:maia-testing-domain"))
    testImplementation(project(":libs:maia-testing:maia-testing-elasticsearch"))
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
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}


val playwrightHeadless = providers.gradleProperty("playwright.headless").orElse("false")


// Playwright ships browser builds for specific Ubuntu releases only. On a newer Ubuntu it refuses to install
// ("does not support chromium on ubuntuNN.NN-x64"), so fall back to the 24.04 build, which runs there.
// An explicit PLAYWRIGHT_HOST_PLATFORM_OVERRIDE in the environment always wins.
val playwrightPlatformOverride: String? = run {
    val osRelease = file("/etc/os-release")
    if (System.getenv("PLAYWRIGHT_HOST_PLATFORM_OVERRIDE") != null || !osRelease.isFile) return@run null
    val fields = osRelease.readLines()
        .filter { it.contains('=') }
        .associate { it.substringBefore('=') to it.substringAfter('=').trim('"') }
    val version = fields["VERSION_ID"]?.split('.')?.mapNotNull { it.toIntOrNull() }
    if (fields["ID"] == "ubuntu" && version != null && version.size >= 2 && version[0] > 24) "ubuntu24.04-x64" else null
}


tasks.withType<Test>().configureEach {
    systemProperty("playwright.headless", playwrightHeadless.get())
    playwrightPlatformOverride?.let { environment("PLAYWRIGHT_HOST_PLATFORM_OVERRIDE", it) }
    // The PostgreSQL JDBC driver opens each session in the JVM's time zone; the filter-parity tests
    // (docs/adr/0005) expect UTC day boundaries, so run the whole suite in UTC.
    jvmArgs("-Duser.timezone=UTC")
}


// The Angular app is built into this module's static resources, so it must be finished before they are packaged.
tasks.processResources {
    dependsOn(":maia-showcase:maia-showcase-ui:buildAngularApp")
}

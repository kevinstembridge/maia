plugins {
    id("maia.kotlin-library-conventions")
}

dependencies {

    implementation(platform(project(":maia-platform")))

    implementation(project(":maia-gen:maia-gen-spec"))
    implementation(project(":maia-gen:maia-gen-generator"))

    implementation("org.postgresql:postgresql")
    implementation("tools.jackson.module:jackson-module-kotlin")

    testImplementation(platform(project(":maia-platform")))
    testImplementation(project(":libs:maia-testing:maia-testing-postgresql"))
    testImplementation(project(":maia-showcase:spec"))
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.flywaydb:flyway-core:11.14.1")
    testImplementation("org.flywaydb:flyway-database-postgresql:11.14.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.test {
    systemProperty("maiaShowcaseMigrationsDir", project(":maia-showcase:dao").projectDir.resolve("src/main/resources/db/migration").absolutePath)
}

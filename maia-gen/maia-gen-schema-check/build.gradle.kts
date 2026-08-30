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
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-spring-conventions")
}


val maiagen by configurations.creating


dependencies {

    implementation(project(":libs:maia-jdbc"))
    implementation(project(":libs:maia-gen:testing:maia-gen-sample"))
    implementation(project(":libs:maia-hazelcast"))
    implementation(project(":libs:maia-problem-parent:maia-problem-starter"))
    implementation(project(":libs:maia-props-parent:maia-props-repo"))
    implementation(project(":libs:maia-webapp:maia-webapp-domain"))

    implementation("tools.jackson.module::jackson-module-kotlin")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.security:spring-security-core")

    testImplementation(project(":libs:maia-testing:maia-testing-domain"))
    testImplementation(project(":libs:maia-testing:maia-testing-postgresql"))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.flywaydb:flyway-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    testRuntimeOnly("org.flywaydb:flyway-database-postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    maiagen(project(":libs:maia-gen:maia-gen-generator"))
    maiagen(project(":libs:maia-gen:testing:maia-gen-sample"))

}


sourceSets {
    main {
        java.srcDir("src/generated/kotlin/main")
        resources.srcDir("src/generated/resources/main")
    }
    test {
        java.srcDir("src/generated/kotlin/test")
    }
}


tasks {
    clean {
        delete("src/generated")
    }
}


tasks.register<JavaExec>("maiaGeneration") {

    inputs.files(file("../maia-gen-sample/src/main/kotlin/org/maiaframework/gen/sample/SampleJdbcSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/kotlin/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.ModelGeneratorKt")
    args("specificationClassNames=org.maiaframework.gen.sample.SamplePartyJdbcSpec,org.maiaframework.gen.sample.SampleJdbcSpec")

}



tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}


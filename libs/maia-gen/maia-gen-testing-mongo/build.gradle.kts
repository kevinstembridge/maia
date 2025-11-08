
plugins {
    id("maia.kotlin-library-spring-conventions")
    id("org.springframework.boot")
}


val maiagen by configurations.creating


dependencies {

    implementation(project(":libs:maia-dao-mongo"))
    implementation(project(":libs:maia-domain-mongo"))
    implementation(project(":libs:maia-jdbc"))
    implementation(project(":libs:maia-gen:maia-gen-sample"))
    implementation(project(":libs:maia-webapp:maia-webapp-domain"))

    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation(project(":libs:maia-testing:maia-testing-domain"))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testng:testng")

    maiagen(project(":libs:maia-gen:maia-gen-generator"))
    maiagen(project(":libs:maia-gen:maia-gen-sample"))

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


tasks.register<JavaExec>("generateModel") {

    inputs.files(file("../maia-gen-sample/src/main/kotlin/org/maiaframework/gen/sample/SampleMongoSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/kotlin/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.ModelGeneratorMain")
    args("specificationClassNames=org.maiaframework.gen.sample.SampleMongoSpec")

}


tasks.register<JavaExec>("generateFieldConverterSample") {

    inputs.files(file("../maia-gen-sample/src/main/kotlin/org/maiaframework/gen/sample/SampleFieldConverterSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/kotlin/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.ModelGeneratorMain")
    args("specificationClassNames=org.maiaframework.gen.sample.SampleFieldConverterSpec")

}

tasks.named("compileKotlin") {
    dependsOn("generateModel", "generateFieldConverterSample")
}


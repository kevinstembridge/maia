import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-spring-conventions")
    idea
}


val maiagen by configurations.creating


dependencies {

    implementation(project(":maia-showcase:elastic-service"))
    implementation(project(":maia-showcase:service"))

    implementation("org.maiaframework:maia-job")
    implementation("org.maiaframework:maia-job-domain")
    implementation("org.maiaframework:maia-props-api")

    implementation("org.springframework.boot:spring-boot-starter-quartz")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    maiagen(project(":maia-gen:maia-gen-generator"))
    maiagen(project(":maia-showcase:spec"))

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

    inputs.files(file("../spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/kotlin/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.JobModuleGeneratorKt")
    args("specificationClassNames=org.maiaframework.showcase.MaiaShowcaseSpec")

}



tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}

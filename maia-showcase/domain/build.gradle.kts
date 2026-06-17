import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-conventions")
    idea
}


val maiagen by configurations.creating


dependencies {

    api("org.maiaframework:maia-domain")

    implementation("org.maiaframework:maia-hazelcast")
    implementation("org.maiaframework:maia-jdbc")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")

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
    mainClass.set("org.maiaframework.gen.generator.DomainModuleGeneratorKt")
    args("applicationSpecClassName=org.maiaframework.showcase.MaiaShowcaseApplicationSpec")

}



tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}

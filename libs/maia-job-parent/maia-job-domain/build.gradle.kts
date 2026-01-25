import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val maiagen by configurations.creating

plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api(project(":libs:maia-common"))
    api(project(":libs:maia-domain"))
    api(project(":libs:maia-hazelcast"))
    api(project(":libs:maia-jdbc"))
    api(project(":libs:maia-metrics"))
    implementation("org.springframework:spring-context")

    maiagen(project(":libs:maia-job-parent:maia-job-spec"))
    maiagen(project(":maia-gen:maia-gen-generator"))

}


sourceSets {
    main {
        java.srcDir("src/generated/kotlin/main")
        resources.srcDir("src/generated/resources/main")
    }
    test {
        java.srcDir("src/generated/kotlin/test")
        java.srcDir("src/generated/resources/test")
    }
}


tasks {
    clean {
        delete("src/generated")
    }
}


tasks.register<JavaExec>("maiaGeneration") {

    group = BasePlugin.BUILD_GROUP
    inputs.files(file("../maia-job-spec/src/main/kotlin/org/maiaframework/job/spec/MaiaJobSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/resources/main")
    outputs.dir("src/generated/kotlin/test")
    outputs.dir("src/generated/resources/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.DomainModuleGeneratorKt")
    args("specificationClassNames=org.maiaframework.job.spec.MaiaJobSpec")

}


tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}


tasks.withType<ProcessResources>() {
    dependsOn("maiaGeneration")
}

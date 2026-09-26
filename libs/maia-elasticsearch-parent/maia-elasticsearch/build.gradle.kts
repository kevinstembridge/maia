import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-spring-conventions")
}

val maiagen by configurations.creating


dependencies {

    api(project(":libs:maia-common"))
    api(project(":libs:maia-domain"))
    api(project(":libs:maia-esdocs"))
    api(project(":libs:maia-lang"))
    api(project(":libs:maia-metrics"))
    api(project(":libs:maia-props-parent:maia-props-api"))
    api(project(":libs:maia-props-parent:maia-props-service"))

    api("co.elastic.clients:elasticsearch-java")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")

    implementation("org.springframework:spring-context")
    implementation("org.apache.logging.log4j:log4j-to-slf4j")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    maiagen(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-spec"))
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
    inputs.files(file("../maia-job-spec/src/main/kotlin/org/maiaframework/job/spec/MaiaJobApplicationSpec"), file("../maia-job-spec/src/main/kotlin/org/maiaframework/job/spec/MaiaJobSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/resources/main")
    outputs.dir("src/generated/kotlin/test")
    outputs.dir("src/generated/resources/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.DomainModuleGeneratorKt")
    args("applicationSpecClassName=org.maiaframework.elasticsearch.spec.MaiaElasticsearchApplicationSpec")

}


tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}


tasks.withType<ProcessResources>() {
    dependsOn("maiaGeneration")
}

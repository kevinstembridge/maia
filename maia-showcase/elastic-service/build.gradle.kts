import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-spring-conventions")
    idea
}


val maiagen by configurations.creating


dependencies {

    implementation(project(":maia-showcase:domain"))
    implementation(project(":maia-showcase:elasticsearch"))
    implementation(project(":maia-showcase:repo"))
    implementation(project(":libs:maia-elasticsearch-parent:maia-elasticsearch"))

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

    inputs.files(file("../spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec"), file("../spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseApplicationSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/kotlin/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.ElasticServiceModuleGeneratorKt")
    args("applicationSpecClassName=org.maiaframework.showcase.MaiaShowcaseApplicationSpec")

}



tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}

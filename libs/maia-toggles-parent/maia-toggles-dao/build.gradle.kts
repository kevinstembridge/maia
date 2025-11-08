
plugins {
    id("maia.kotlin-library-spring-conventions")
}


val maiagen by configurations.creating


dependencies {

    api(project(":libs:maia-toggles-parent:maia-toggles-domain"))

    implementation(project(":libs:maia-jdbc"))

    maiagen(project(":libs:maia-toggles-parent:maia-toggles-spec"))
    maiagen(project(":libs:maia-gen:maia-gen-generator"))

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


tasks.register<JavaExec>("generateModel") {

    group = BasePlugin.BUILD_GROUP
    inputs.files(file("../maia-toggles-spec/src/main/kotlin/org/maiaframework/toggles/spec/TogglesSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/resources/main")
    outputs.dir("src/generated/kotlin/test")
    outputs.dir("src/generated/resources/test")
    outputs.dir("src/generated/sql")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.DaoModuleGeneratorMain")
    args("specificationClassNames=org.maiaframework.toggles.spec.TogglesSpec")

}


tasks.named("compileKotlin") {
    dependsOn("generateModel")
}


tasks.named("processResources") {
    dependsOn("generateModel")
}

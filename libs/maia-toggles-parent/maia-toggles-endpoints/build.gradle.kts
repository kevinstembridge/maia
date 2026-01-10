
plugins {
    id("maia.kotlin-library-conventions")
    idea
}


val maiagen by configurations.creating


dependencies {

    implementation(project(":libs:maia-toggles-parent:maia-toggles-service"))

    implementation("org.springframework.boot:spring-boot-starter-web")

    maiagen(project(":libs:maia-toggles-parent:maia-toggles-spec"))
    maiagen(project(":libs:maia-gen:maia-gen-generator"))

}


val generatedKotlinMain = file("src/generated/kotlin/main")
val generatedKotlinTest = file("src/generated/kotlin/test")
val generatedResourcesMain = file("src/generated/resources/main")
val generatedResourcesTest = file("src/generated/resources/test")


idea {
    module {
        generatedSourceDirs.add(generatedKotlinMain)
        generatedSourceDirs.add(generatedKotlinTest)
        generatedSourceDirs.add(generatedResourcesMain)
        generatedSourceDirs.add(generatedResourcesTest)
    }
}


sourceSets {
    main {
        java.srcDir(generatedKotlinMain)
        resources.srcDir(generatedResourcesMain)
    }
    test {
        java.srcDir(generatedKotlinTest)
        resources.srcDir(generatedResourcesTest)
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

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.EndpointsModuleGeneratorKt")
    args("specificationClassNames=org.maiaframework.toggles.spec.TogglesSpec")

}


tasks.named("compileKotlin") {
    dependsOn("generateModel")
}


tasks.named("processResources") {
    dependsOn("generateModel")
}

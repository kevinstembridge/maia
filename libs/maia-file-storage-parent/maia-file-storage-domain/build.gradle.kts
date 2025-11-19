
val maiagen by configurations.creating


plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-common"))
    api(project(":libs:maia-domain"))
    api(project(":libs:maia-hazelcast"))
    api(project(":libs:maia-jdbc"))
    api(project(":libs:maia-webapp:maia-webapp-domain"))
    implementation("org.springframework:spring-context")

    maiagen(project(":libs:maia-file-storage-parent:maia-file-storage-spec"))
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
    inputs.files(file("../maia-file-storage-spec/src/main/kotlin/org/maiaframework/storage/spec/StorageSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/resources/main")
    outputs.dir("src/generated/kotlin/test")
    outputs.dir("src/generated/resources/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.DomainModuleGeneratorKt")
    args("specificationClassNames=org.maiaframework.storage.spec.StorageSpec")

}


tasks.named("compileKotlin") {
    dependsOn("generateModel")
}


tasks.named("processResources") {
    dependsOn("generateModel")
}

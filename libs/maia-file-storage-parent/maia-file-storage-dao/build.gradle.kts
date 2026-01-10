import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-spring-conventions")
}


val maiagen by configurations.creating


dependencies {

    api(project(":libs:maia-file-storage-parent:maia-file-storage-domain"))

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


tasks.register<JavaExec>("maiaGeneration") {

    group = BasePlugin.BUILD_GROUP
    inputs.files(file("../maia-file-storage-spec/src/main/kotlin/org/maiaframework/storage/spec/StorageSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/resources/main")
    outputs.dir("src/generated/kotlin/test")
    outputs.dir("src/generated/resources/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.DaoLayerModuleGeneratorKt")
    args("specificationClassNames=org.maiaframework.storage.spec.StorageSpec")

}


tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}


tasks.withType<ProcessResources>() {
    dependsOn("maiaGeneration")
}

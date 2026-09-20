import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-spring-conventions")
    idea
}


val maiagen by configurations.creating


dependencies {

    implementation(project(":libs:maia-common"))
    api(project(":libs:maia-props-parent:maia-props-repo"))

    implementation(project(":libs:maia-problem-parent:maia-problem-starter"))
    implementation(project(":libs:maia-props-parent:maia-props-dao"))
    implementation(project(":libs:maia-webapp:maia-webapp-domain"))

    implementation("org.springframework:spring-tx")

    maiagen(project(":libs:maia-props-parent:maia-props-spec"))
    maiagen(project(":maia-gen:maia-gen-generator"))

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


tasks.register<JavaExec>("maiaGeneration") {

    group = BasePlugin.BUILD_GROUP
    inputs.files(file("../maia-props-spec/src/main/kotlin/org/maiaframework/props/spec/PropsApplicationSpec"), file("../maia-props-spec/src/main/kotlin/org/maiaframework/props/spec/PropsSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/resources/main")
    outputs.dir("src/generated/kotlin/test")
    outputs.dir("src/generated/resources/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.ServiceLayerModuleGeneratorKt")
    args("applicationSpecClassName=org.maiaframework.props.spec.PropsApplicationSpec")

}


tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}


tasks.withType<ProcessResources>() {
    dependsOn("maiaGeneration")
}

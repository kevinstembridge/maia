import com.github.gradle.node.npm.task.NpxTask

plugins {
    id("java-base")
    id("com.github.node-gradle.node") version "7.0.2"
}

repositories {
    mavenCentral()
}

node {
    download.set(true)
    version.set("24.10.0")
}

val maiagenElasticsearch by configurations.creating
val maiagenProps by configurations.creating
val maiagenJob by configurations.creating
val maiagenToggles by configurations.creating

dependencies {

    maiagenElasticsearch(project(":maia-gen:maia-gen-generator"))
    maiagenElasticsearch(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-spec"))

    maiagenProps(project(":maia-gen:maia-gen-generator"))
    maiagenProps(project(":libs:maia-props-parent:maia-props-spec"))

    maiagenJob(project(":maia-gen:maia-gen-generator"))
    maiagenJob(project(":libs:maia-job-parent:maia-job-spec"))

    maiagenToggles(project(":maia-gen:maia-gen-generator"))
    maiagenToggles(project(":libs:maia-toggles-parent:maia-toggles-spec"))

}

tasks.register<JavaExec>("maiaGenerationElasticsearch") {
    group = "maia generation"
    inputs.dir("../maia-elasticsearch-parent/maia-elasticsearch-spec/src/main/kotlin")
    outputs.dir("projects/maia-elasticsearch/src/generated")
    classpath = configurations["maiagenElasticsearch"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.AngularUiModuleGeneratorKt")
    args(
        "applicationSpecClassName=org.maiaframework.elasticsearch.spec.MaiaElasticsearchApplicationSpec",
        "generatedSourceDir=projects/maia-elasticsearch/src/generated"
    )
}

tasks.register<JavaExec>("maiaGenerationProps") {
    group = "maia generation"
    inputs.dir("../maia-props-parent/maia-props-spec/src/main/kotlin")
    outputs.dir("projects/maia-props/src/generated")
    classpath = configurations["maiagenProps"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.AngularUiModuleGeneratorKt")
    args(
        "applicationSpecClassName=org.maiaframework.props.spec.PropsApplicationSpec",
        "generatedSourceDir=projects/maia-props/src/generated"
    )
}

tasks.register<JavaExec>("maiaGenerationJob") {
    group = "maia generation"
    inputs.dir("../maia-job-parent/maia-job-spec/src/main/kotlin")
    outputs.dir("projects/maia-jobs/src/generated")
    classpath = configurations["maiagenJob"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.AngularUiModuleGeneratorKt")
    args(
        "applicationSpecClassName=org.maiaframework.job.spec.MaiaJobApplicationSpec",
        "generatedSourceDir=projects/maia-jobs/src/generated"
    )
}

tasks.register<JavaExec>("maiaGenerationToggles") {
    group = "maia generation"
    inputs.dir("../maia-toggles-parent/maia-toggles-spec/src/main/kotlin")
    outputs.dir("projects/maia-toggles/src/generated")
    classpath = configurations["maiagenToggles"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.AngularUiModuleGeneratorKt")
    args(
        "applicationSpecClassName=org.maiaframework.toggles.spec.TogglesApplicationSpec",
        "generatedSourceDir=projects/maia-toggles/src/generated"
    )
}

tasks.register("maiaGeneration") {
    group = "maia generation"
    dependsOn("maiaGenerationElasticsearch", "maiaGenerationProps", "maiaGenerationJob", "maiaGenerationToggles")
}

tasks.register<NpxTask>("buildAngularElasticsearch") {
    dependsOn(tasks.npmInstall, "maiaGenerationElasticsearch")
    command.set("ng")
    args.set(listOf("build", "maia-elasticsearch"))
    inputs.files("package.json", "package-lock.json", "angular.json")
    inputs.dir("projects/maia-elasticsearch/src")
    inputs.dir(fileTree("node_modules").exclude(".cache"))
    outputs.dir("dist/maia-elasticsearch")
}

tasks.register<NpxTask>("buildAngularProps") {
    dependsOn(tasks.npmInstall, "maiaGenerationProps")
    command.set("ng")
    args.set(listOf("build", "maia-props"))
    inputs.files("package.json", "package-lock.json", "angular.json")
    inputs.dir("projects/maia-props/src")
    inputs.dir(fileTree("node_modules").exclude(".cache"))
    outputs.dir("dist/maia-props")
}

tasks.register<NpxTask>("buildAngularJob") {
    dependsOn(tasks.npmInstall, "maiaGenerationJob")
    command.set("ng")
    args.set(listOf("build", "maia-jobs"))
    inputs.files("package.json", "package-lock.json", "angular.json")
    inputs.dir("projects/maia-jobs/src")
    inputs.dir(fileTree("node_modules").exclude(".cache"))
    outputs.dir("dist/maia-jobs")
}

tasks.register<NpxTask>("buildAngularToggles") {
    dependsOn(tasks.npmInstall, "maiaGenerationToggles")
    command.set("ng")
    args.set(listOf("build", "maia-toggles"))
    inputs.files("package.json", "package-lock.json", "angular.json")
    inputs.dir("projects/maia-toggles/src")
    inputs.dir(fileTree("node_modules").exclude(".cache"))
    outputs.dir("dist/maia-toggles")
}

tasks.register("buildAngularLibs") {
    group = "maia generation"
    dependsOn("buildAngularElasticsearch", "buildAngularProps", "buildAngularJob", "buildAngularToggles")
}

tasks.named("assemble") {
    dependsOn("buildAngularLibs")
}

tasks {
    clean {
        delete("projects/maia-elasticsearch/src/generated")
        delete("projects/maia-props/src/generated")
        delete("projects/maia-jobs/src/generated")
        delete("projects/maia-toggles/src/generated")
    }
}

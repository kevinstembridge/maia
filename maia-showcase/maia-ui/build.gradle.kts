import com.github.gradle.node.npm.task.NpxTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-conventions")
    id("com.github.node-gradle.node") version "7.0.2"
    idea
}


val maiagen by configurations.creating


dependencies {

    maiagen(project(":maia-gen:maia-gen-generator"))
    maiagen(project(":maia-showcase:spec"))

}



node {
    download.set(true)
    version.set("24.10.0")
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


tasks.register<NpxTask>("buildAngularApp") {
    dependsOn(tasks.npmInstall, "maiaGeneration")
    command.set("ng")
    args.set(listOf("build", "--configuration", "production"))
    inputs.files("package.json", "package-lock.json", "angular.json", "tsconfig.json", "tsconfig.app.json")
    inputs.dir("src")
    inputs.dir(fileTree("node_modules").exclude(".cache"))
    outputs.dir("../la-app/src/main/resources/static/browser")
}



tasks.register<JavaExec>("maiaGeneration") {

    inputs.files(file("../spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/kotlin/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.AngularUiModuleGeneratorKt")
    args("specificationClassNames=org.maiaframework.showcase.MaiaShowcaseSpec,org.maiaframework.showcase.MaiaShowcasePartySpec")

}



tasks.withType<KotlinCompile>() {
    dependsOn("buildAngularApp", "maiaGeneration")
}


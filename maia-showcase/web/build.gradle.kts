import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-spring-conventions")
    idea
}


val maiagen by configurations.creating


dependencies {

    implementation(kotlin("reflect"))

    implementation(project(":libs:maia-webapp:maia-webapp-web"))
    implementation(project(":maia-showcase:service"))

    implementation("org.maiaframework:maia-common")
    implementation("org.maiaframework:maia-domain")
    implementation("org.maiaframework:maia-lang")
    implementation("org.maiaframework:maia-webapp-domain")

    implementation("org.maiaframework:maia-jdbc")

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

    inputs.files(file("../spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec"))
    outputs.dir("src/generated/kotlin/main")
    outputs.dir("src/generated/kotlin/test")

    classpath = configurations["maiagen"].asFileTree
    mainClass.set("org.maiaframework.gen.generator.WebLayerModuleGeneratorKt")
    args("specificationClassNames=org.maiaframework.showcase.MaiaShowcaseSpec,org.maiaframework.showcase.MaiaShowcasePartySpec")

}



tasks.withType<KotlinCompile>() {
    dependsOn("maiaGeneration")
}

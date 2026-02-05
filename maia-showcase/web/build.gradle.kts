import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maia.kotlin-library-spring-conventions")
    idea
}


val maiagen by configurations.creating


dependencies {

    implementation(kotlin("reflect"))

    implementation(project(":libs:maia-webapp:maia-webapp-endpoints"))
    implementation(project(":maia-showcase:service"))

    api("org.maiaframework:maia-common")
//    api("org.maiaframework:maia-csv")
    api("org.maiaframework:maia-domain")
    api("org.maiaframework:maia-lang")
//    api("org.maiaframework:maia-staging-csv")
//    api("org.maiaframework:maia-webapp-domain")

//    implementation("org.maiaframework:maia-hazelcast")
    implementation("org.maiaframework:maia-jdbc")

//    implementation("com.hazelcast:hazelcast")
//    implementation("org.hibernate.validator:hibernate-validator")

//    testImplementation("org.junit.jupiter:junit-jupiter")
//    testImplementation("org.assertj:assertj-core")

//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

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

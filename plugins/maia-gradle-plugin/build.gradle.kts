plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("maia.kotlin-conventions")
}


repositories {
    mavenCentral()
    gradlePluginPortal()
}


gradlePlugin {
    plugins {
        create("maiaGenerationPlugin") {
            id = "org.maiaframework.maia-generation"
            implementationClass = "org.maiaframework.gen.plugin.MaiaGenerationPlugin"
        }
    }
}


dependencies {

    implementation(project(":libs:maia-gen:maia-gen-generator"))
    implementation(project(":libs:maia-gen:maia-gen-spec"))

    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.3.0")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

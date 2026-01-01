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
        create("maiaGenPlugin") {
            id = "org.maiaframework.maia-gen"
            implementationClass = "org.maiaframework.gen.plugin.MaiaGenPlugin"
        }
    }
}


dependencies {

    implementation(project(":libs:maia-gen:maia-gen-generator"))
    implementation(project(":libs:maia-gen:maia-gen-spec"))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

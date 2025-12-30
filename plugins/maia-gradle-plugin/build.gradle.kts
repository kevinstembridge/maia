plugins {
//    `kotlin-dsl`
    `java-gradle-plugin`
//    id("maia.kotlin-conventions")
//    `maven-publish`
}


gradlePlugin {
    plugins {
        create("maiaPlugin") {
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

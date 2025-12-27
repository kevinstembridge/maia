plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("maia.kotlin-conventions")
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

}

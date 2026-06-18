
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-jdbc"))
    api(project(":libs:maia-lang"))
    api(project(":libs:maia-domain"))

    testImplementation(platform(project(":maia-platform")))
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

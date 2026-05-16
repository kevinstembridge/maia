plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

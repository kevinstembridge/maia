plugins {
    id("maia.kotlin-library-conventions")
}

dependencies {

    implementation(platform(project(":maia-platform")))

    implementation(project(":maia-gen:maia-gen-spec"))
    implementation(project(":maia-gen:maia-gen-generator"))

    testImplementation(platform(project(":maia-platform")))
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}


plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    implementation(project(":maia-gen:maia-gen-spec"))

    testImplementation(platform(project(":maia-platform")))
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}


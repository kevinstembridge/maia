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
    testImplementation(project(":maia-showcase:spec"))
    testImplementation(project(":libs:maia-job-parent:maia-job-spec"))
    testImplementation(project(":libs:maia-toggles-parent:maia-toggles-spec"))
    testImplementation(project(":libs:maia-props-parent:maia-props-spec"))
    testImplementation(project(":libs:maia-file-storage-parent:maia-file-storage-spec"))
    testImplementation(project(":libs:maia-elasticsearch-parent:maia-elasticsearch-spec"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

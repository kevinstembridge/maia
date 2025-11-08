
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api("org.slf4j:slf4j-api")

    api(project(":libs:maia-dynamic-regex"))

    testImplementation(project(":libs:maia-testing:maia-testing-domain"))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

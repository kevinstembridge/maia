
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-common"))
    api(project(":libs:maia-domain"))
    api(project(":libs:maia-esdocs"))
    api(project(":libs:maia-lang"))
    api(project(":libs:maia-metrics"))
    api(project(":libs:maia-props-parent:maia-props-api"))

    api("co.elastic.clients:elasticsearch-java")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")

    implementation("org.springframework:spring-context")
    implementation("org.apache.logging.log4j:log4j-to-slf4j")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

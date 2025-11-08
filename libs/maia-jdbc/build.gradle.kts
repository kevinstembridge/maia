
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-common"))
    api(project(":libs:maia-domain"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")
    api("org.slf4j:slf4j-api")
    api("org.springframework:spring-jdbc")

    implementation("io.micrometer:micrometer-core")
    implementation("org.springframework:spring-context")

}

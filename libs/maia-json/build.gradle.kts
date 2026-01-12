
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api("tools.jackson.core:jackson-databind")
    api("tools.jackson.module:jackson-module-kotlin")
    api("org.springframework:spring-context")

//    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
//    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

}


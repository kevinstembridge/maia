
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-hazelcast"))
    api(project(":libs:maia-toggles-parent:maia-toggles-api"))
    api(project(":libs:maia-toggles-parent:maia-toggles-domain"))

    implementation(project(":libs:maia-toggles-parent:maia-toggles-dao"))
    implementation(project(":libs:maia-toggles-parent:maia-toggles-repo"))
    implementation(project(":libs:maia-toggles-parent:maia-toggles-service"))

    implementation(project(":libs:maia-jdbc"))

    implementation("org.springframework.boot:spring-boot-starter")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")

}



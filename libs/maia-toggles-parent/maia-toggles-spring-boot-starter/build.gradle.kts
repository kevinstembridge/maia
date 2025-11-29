
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-toggles-parent:maia-toggles-api"))
    api(project(":libs:maia-toggles-parent:maia-toggles-autoconfigure"))
    api(project(":libs:maia-toggles-parent:maia-toggles-endpoints"))
    api(project(":libs:maia-toggles-parent:maia-toggles-repo"))

    implementation("org.springframework.boot:spring-boot-starter")

}

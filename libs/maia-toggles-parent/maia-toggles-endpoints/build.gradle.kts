
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    implementation(project(":libs:maia-toggles-parent:maia-toggles-service"))

    implementation("org.springframework.boot:spring-boot-starter-web")

}


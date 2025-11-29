
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-toggles-parent:maia-toggles-domain"))

    implementation("org.springframework.security:spring-security-core")

}

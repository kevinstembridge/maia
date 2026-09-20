
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-common"))
    api(project(":libs:maia-props-parent:maia-props-service"))
    api(project(":libs:maia-props-parent:maia-props-domain"))
    api(project(":libs:maia-webapp:maia-webapp-domain"))

    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-web")

}

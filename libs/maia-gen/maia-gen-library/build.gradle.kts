
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api(project(":libs:maia-common"))
    api(project(":libs:maia-domain"))
    api(project(":libs:maia-jdbc"))
    api(project(":libs:maia-json"))

    api("org.hibernate.validator:hibernate-validator")
    api("org.springframework:spring-context")
    api("org.springframework.boot:spring-boot")
    api("org.springframework.data:spring-data-commons")

}

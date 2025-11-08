
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-common"))
    api(project(":libs:maia-id"))
    api(project(":libs:maia-json"))
    api(project(":libs:maia-lang"))
    api(project(":libs:maia-types"))

    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.hibernate.validator:hibernate-validator")
    api("org.springframework.data:spring-data-commons")

}


plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-common"))
    api(project(":libs:maia-domain"))
    api(project(":libs:maia-jdbc"))
    api(project(":libs:maia-json"))

    api("org.hibernate.validator:hibernate-validator:9.1.0.Final")
    api("org.springframework:spring-context")
    api("org.springframework.boot:spring-boot")
    api("org.springframework.data:spring-data-commons")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.testng:testng")

}

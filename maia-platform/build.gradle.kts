plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}


dependencies {

    api(platform("dev.forkhandles:forkhandles-bom:2.20.0.0"))
    api(platform("io.dropwizard.metrics:metrics-bom:4.2.30"))
    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.1"))

    constraints {
        api("com.fasterxml.uuid:java-uuid-generator:5.1.0")
        api("com.microsoft.playwright:playwright:1.51.0")
        api("com.ninja-squad:springmockk:4.0.2")
        api("com.opencsv:opencsv:5.10")
        api("commons-io:commons-io:2.18.0")
        api("io.jsonwebtoken:jjwt:0.7.0")
        api("io.mockk:mockk:1.14.7")
        api("net.sf.supercsv:super-csv-java8:2.4.0") // TODO Do we still need this? We changed to opencsv due to its flexibility
        api("org.apache.commons:commons-csv:1.10.0") // TODO Do we still need this? We changed to opencsv due to its flexibility
        api("org.springdoc:springdoc-openapi-ui:1.8.0")
        api("org.testcontainers:junit-jupiter:1.21.4")
        api("org.testcontainers:postgresql:1.21.4")
        api("org.testng:testng:7.11.0")

    }

}

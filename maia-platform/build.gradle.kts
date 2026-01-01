plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}


dependencies {

    api(platform("io.dropwizard.metrics:metrics-bom:4.2.30"))
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.6"))

    constraints {
        api("com.fasterxml.uuid:java-uuid-generator:5.1.0")
        api("com.microsoft.playwright:playwright:1.51.0")
        api("com.opencsv:opencsv:5.10")
        api("commons-io:commons-io:2.18.0")
        api("io.jsonwebtoken:jjwt:0.7.0")
        api("net.sf.supercsv:super-csv-java8:2.4.0") // TODO Do we still need this? We changed to opencsv due to its flexibility
        api("org.apache.commons:commons-csv:1.10.0") // TODO Do we still need this? We changed to opencsv due to its flexibility
        api("org.testng:testng:7.11.0")
    }

}

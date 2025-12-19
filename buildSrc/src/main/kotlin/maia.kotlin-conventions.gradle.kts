import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
}

// NOTE: We use the Spring Dependency Management plugin to manage dependencies across the multi-project build
// https://docs.spring.io/dependency-management-plugin/docs/current/reference/html

dependencyManagement {

    configurations {
        all {
            exclude(group = "commons-logging", module = "commons-logging")
        }
    }

    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) {
            bomProperty("kotlin.version", "2.3.0")
        }
        mavenBom("dev.forkhandles:forkhandles-bom:2.20.0.0")
        mavenBom("io.dropwizard.metrics:metrics-bom:4.2.30")
        mavenBom("io.micrometer:micrometer-bom:1.15.1")
    }

    dependencies {
        dependency("com.auth0:java-jwt:3.3.0")
        dependency("com.fasterxml.uuid:java-uuid-generator:5.1.0")
        dependency("com.hazelcast:hazelcast:5.5.0")
        dependency("com.microsoft.playwright:playwright:1.51.0")
        dependency("com.ninja-squad:springmockk:4.0.2")
        dependency("com.opencsv:opencsv:5.10")
        dependency("commons-io:commons-io:2.18.0")
        dependency("io.fluidsonic.mirror:quartz-mongodb:2.2.0-rc2")
        dependency("io.jsonwebtoken:jjwt:0.7.0")
        dependency("io.mockk:mockk:1.13.17")
        dependency("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
        dependency("net.sf.supercsv:super-csv-java8:2.4.0") // TODO Do we still need this? We changed to opencsv due to its flexibility
        dependency("org.apache.commons:commons-csv:1.10.0") // TODO Do we still need this? We changed to opencsv due to its flexibility
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")
        dependency("org.springdoc:springdoc-openapi-ui:1.8.0")
        dependency("org.testng:testng:7.11.0")
    }

}

kotlin {
    compilerOptions {
        suppressWarnings.set(true)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}


tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}


tasks.withType<Test>{
    useJUnitPlatform()
}


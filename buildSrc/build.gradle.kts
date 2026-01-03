
plugins {
    `kotlin-dsl`
}


repositories {
    // for the kotlin-dsl plugin
    gradlePluginPortal()
}


dependencies {

    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.3.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.5.6")

}

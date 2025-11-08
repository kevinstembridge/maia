
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    implementation(project(":libs:maia-common"))
    api(project(":libs:maia-domain"))
    api("com.sendgrid:sendgrid-java")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot")

}


plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api("org.slf4j:slf4j-api")
    api(project(":libs:maia-domain"))
    api(project(":libs:maia-mail-parent:maia-sendgrid"))
    api("org.springframework.boot:spring-boot-starter-thymeleaf")

}

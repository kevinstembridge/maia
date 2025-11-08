
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-common"))
    api(project(":libs:maia-domain"))
    api(project(":libs:maia-http"))

    api("com.microsoft.playwright:playwright")
    api("org.assertj:assertj-core")
    api("org.seleniumhq.selenium:selenium-support")
    api("org.seleniumhq.selenium:selenium-chrome-driver")
    api("org.slf4j:slf4j-api")
    api("org.springframework.boot:spring-boot-starter-test")
    api("org.springframework.security:spring-security-test")

}

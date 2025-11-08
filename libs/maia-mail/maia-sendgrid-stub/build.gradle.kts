
plugins {
    id("maia.kotlin-library-spring-conventions")
}



dependencies {

    implementation(project(":libs:maia-domain"))
    api(project(":libs:maia-mail:maia-sendgrid"))
    api(project(":libs:maia-mail:maia-mail"))
    implementation("org.assertj:assertj-core")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.retry:spring-retry")

}

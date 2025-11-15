
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-mail-parent:maia-sendgrid"))
    implementation("org.springframework.boot:spring-boot-starter")

}



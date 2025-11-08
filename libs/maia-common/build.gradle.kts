plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.slf4j:slf4j-api")

    implementation("org.hibernate.validator:hibernate-validator") // TODO KNS use jakarta.validation directly instead of hibernate

}

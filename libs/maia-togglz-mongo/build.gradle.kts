
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {


    api("org.togglz:togglz-kotlin")

    implementation(project(":libs:maia-dao-mongo"))

    implementation("org.mongodb:mongodb-driver-sync")
    implementation("org.springframework:spring-context")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.testng:testng")

}

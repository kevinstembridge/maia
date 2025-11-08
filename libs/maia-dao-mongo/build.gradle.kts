
plugins {
    id("maia.kotlin-library-spring-conventions")
}



dependencies {

    api(project(":libs:maia-common"))
    api(project(":libs:maia-domain-mongo"))
    api(project(":libs:maia-json"))
    api(project(":libs:maia-lang"))

    api("org.mongodb:mongodb-driver-sync")
    api("org.springframework.boot:spring-boot")
    api("org.springframework:spring-context")
    api("org.springframework.data:spring-data-commons")

    testImplementation("org.testng:testng")
    testImplementation("org.assertj:assertj-core")

}


tasks.withType<Test>{
    useTestNG()
}


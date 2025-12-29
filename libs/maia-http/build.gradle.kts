
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-json"))

    implementation(project(":libs:maia-common"))
    implementation(project(":libs:maia-domain"))
    implementation(project(":libs:maia-csv"))

    implementation("net.sf.supercsv:super-csv-java8")
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.apache.httpcomponents.client5:httpclient5-fluent")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-webmvc")

}


plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    implementation(project(":libs:maia-common"))
    implementation(project(":libs:maia-domain"))
    // TODO do we need this or not?
//    implementation(project(":libs:maia-csv"))
    api(project(":libs:maia-json"))
    
    implementation("net.sf.supercsv:super-csv-java8")
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.apache.httpcomponents.client5:httpclient5-fluent")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-webmvc")

}

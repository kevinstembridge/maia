
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-json"))

    implementation(project(":libs:maia-common"))

    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.slf4j:slf4j-api")

}

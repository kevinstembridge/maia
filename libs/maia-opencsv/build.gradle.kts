plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(project(":libs:maia-csv"))

    api("com.opencsv:opencsv")

    implementation(project(":libs:maia-common"))

    implementation("org.slf4j:slf4j-api")

}

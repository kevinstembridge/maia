plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api("org.apache.commons:commons-csv")

    implementation("net.sf.supercsv:super-csv-java8")
    implementation("org.slf4j:slf4j-api")

}

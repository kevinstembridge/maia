
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api(project(":libs:maia-data-row"))
    api(project(":libs:maia-job-parent:maia-job-domain"))

    api("com.fasterxml.jackson.core:jackson-annotations")
    api("net.sf.supercsv:super-csv-java8")
    api("org.apache.commons:commons-csv")

    implementation(project(":libs:maia-job-parent:maia-job-dao"))
    implementation(project(":libs:maia-opencsv"))

    implementation("com.opencsv:opencsv")
    implementation("org.hibernate.validator:hibernate-validator")

}


plugins {
    id("maia.kotlin-library-conventions")
}



dependencies {

    implementation("org.springframework:spring-context")
    api(project(":libs:maia-file-storage-parent:maia-file-storage-dao"))
    api(project(":libs:maia-file-storage-parent:maia-file-storage-domain"))
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    implementation("commons-io:commons-io")

}

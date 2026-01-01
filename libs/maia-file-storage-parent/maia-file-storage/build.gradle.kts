
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api(project(":libs:maia-file-storage-parent:maia-file-storage-dao"))
    api(project(":libs:maia-file-storage-parent:maia-file-storage-domain"))

    implementation("commons-io:commons-io")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    implementation("org.springframework:spring-context")

}

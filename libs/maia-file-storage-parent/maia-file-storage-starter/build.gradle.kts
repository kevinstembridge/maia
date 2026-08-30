
plugins {
    id("maia.kotlin-library-spring-conventions")
}


dependencies {

    api(project(":libs:maia-file-storage-parent:maia-file-storage-autoconfigure"))
    api(project(":libs:maia-file-storage-parent:maia-file-storage"))
    implementation("org.springframework.boot:spring-boot-starter")

}

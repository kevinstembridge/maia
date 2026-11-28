
plugins {
    id("maia.kotlin-library-conventions")
}


dependencies {

    api(platform(project(":maia-platform")))

    api(project(":libs:maia-domain"))

    api("com.hazelcast:hazelcast-spring")

}

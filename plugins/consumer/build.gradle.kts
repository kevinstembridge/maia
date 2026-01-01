import org.maiaframework.gen.plugin.ModuleType

plugins {
    id("org.maiaframework.maia-gen")
}


repositories {
    mavenCentral()
}


maia {

    moduleType.set(ModuleType.DOMAIN)
    specificationClassNames.set(listOf("org.maiaframework.consumer.SampleSpec"))

    dependencies {
//        getImplementation().add(project(":sample"))
    }


}

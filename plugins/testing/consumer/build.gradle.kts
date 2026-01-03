plugins {
    id("org.maiaframework.maia-gen")
}


maia {

    specificationClassNames.set(listOf("org.maiaframework.gen.sample.SampleJdbcSpec"))
    moduleGeneratorClassName.set("org.maiaframework.gen.generator.ModelGenerator")

    dependencies {
        getImplementation().add("org.maiaframework:maia-gen-testing-jdbc")
    }

}

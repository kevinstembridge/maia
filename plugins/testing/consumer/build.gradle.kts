plugins {
    id("org.maiaframework.maia-generation")
    idea
}


maia {

    specificationClassName.set(listOf("org.maiaframework.gen.sample.SampleJdbcSpec"))
    moduleGeneratorClassName.set("org.maiaframework.gen.generator.ModelGenerator")

    dependencies {
        getImplementation().add("org.maiaframework:maia-gen-testing-jdbc")
    }

}

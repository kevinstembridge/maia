package org.maiaframework.gen.generator


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = ModelGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class ModelGenerator(
    maiaGenerationContext: MaiaGenerationContext
) : AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        AppModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)
        DaoLayerModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)
        DomainModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)
        ElasticSearchModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)
        ElasticServiceModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)
        EsDocsModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)
        JobModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)
        RepoLayerModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)
        ServiceLayerModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)
        WebLayerModuleGenerator(this.maiaGenerationContext).generateSource(this.applicationModelDef)

    }


}

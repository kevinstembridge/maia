package org.maiaframework.gen.generator


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = ModelGenerator(moduleGeneratorFixture.maiaGenerationContext)
            modelGenerator.generateSource(it)

        }

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

        AppModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)
        DaoLayerModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)
        DomainModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)
        ElasticSearchModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)
        ElasticServiceModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)
        EsDocsModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)
        JobModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)
        RepoLayerModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)
        ServiceLayerModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)
        WebLayerModuleGenerator(this.maiaGenerationContext).generateSource(this.modelDef)

    }


}

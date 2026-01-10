package org.maiaframework.gen.generator


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = ModelGenerator(moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class ModelGenerator(
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        AppModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        DaoLayerModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        DomainModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        ElasticSearchModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        ElasticServiceModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        EsDocsModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        JobModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        RepoLayerModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        ServiceLayerModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        WebLayerModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)

    }


}

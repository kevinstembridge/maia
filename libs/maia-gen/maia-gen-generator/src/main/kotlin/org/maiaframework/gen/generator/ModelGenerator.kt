package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.definition.ModelDef


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
        DaoModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        DomainModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        ElasticSearchModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        ElasticServiceModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        EndpointsModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        EsDocsModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        JobModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        RepoModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)
        ServiceModuleGenerator(this.modelGeneratorContext).generateSource(this.modelDef)

    }


}

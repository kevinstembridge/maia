package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.definition.ModelDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = ModelGenerator(it, moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource()

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class ModelGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
    modelDef,
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        AppModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()
        DaoModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()
        DomainModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()
        ElasticSearchModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()
        ElasticServiceModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()
        EndpointsModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()
        EsDocsModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()
        JobModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()
        RepoModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()
        ServiceModuleGenerator(this.modelDef, this.modelGeneratorContext).generateSource()

    }


}

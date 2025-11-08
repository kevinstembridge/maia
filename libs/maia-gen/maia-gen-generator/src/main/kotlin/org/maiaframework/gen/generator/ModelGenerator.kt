package org.maiaframework.gen.generator

import org.maiaframework.gen.spec.definition.ModelDef


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

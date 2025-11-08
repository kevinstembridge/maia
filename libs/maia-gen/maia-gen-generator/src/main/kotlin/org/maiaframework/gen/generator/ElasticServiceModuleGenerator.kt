package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EsDocRepoRenderer
import org.maiaframework.gen.renderers.TypeaheadIndexServiceRenderer
import org.maiaframework.gen.spec.definition.ModelDef


class ElasticServiceModuleGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
): AbstractModuleGenerator(
    modelDef,
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        renderTypeaheadIndexServices()
        renderTypeaheadEsDocRepos()

    }


    private fun renderTypeaheadIndexServices() {

        this.modelDef.typeaheadDefs.filter { it.entityUqcn != null }.forEach {
            TypeaheadIndexServiceRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderTypeaheadEsDocRepos() {

        this.modelDef.typeaheadDefs.filter { it.withHandCodedEsDocRepo.value == false && it.entityUqcn != null }.forEach {
            EsDocRepoRenderer(it.esDocDef).renderToDir(this.kotlinOutputDir)
        }

    }


}

package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EsDocRepoRenderer
import org.maiaframework.gen.renderers.TypeaheadIndexServiceRenderer


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = ElasticServiceModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class ElasticServiceModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
): AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        renderTypeaheadIndexServices()
        renderTypeaheadEsDocRepos()

    }


    private fun renderTypeaheadIndexServices() {

        this.applicationModelDef.typeaheadDefs.filter { it.entityUqcn != null }.forEach {
            TypeaheadIndexServiceRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderTypeaheadEsDocRepos() {

        this.applicationModelDef.typeaheadDefs
            .filter { it.withHandCodedEsDocRepo.value == false && it.entityUqcn != null }
            .forEach { EsDocRepoRenderer(it.esDocDef).renderToDir(this.kotlinOutputDir) }

    }


}

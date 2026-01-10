package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EsDocRepoRenderer
import org.maiaframework.gen.renderers.TypeaheadIndexServiceRenderer


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = ElasticServiceModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
            modelGenerator.generateSource(it)

        }

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

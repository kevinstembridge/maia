package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EsDocRepoRenderer
import org.maiaframework.gen.renderers.EsRefreshListeners
import org.maiaframework.gen.renderers.SearchableDtoEsDocRepoRenderer
import org.maiaframework.gen.renderers.SearchableDtoEsIndexServiceRenderer
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
        renderSearchableDtoEsIndexServices()
        renderSearchableDtoEsRefreshListeners()

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


    private fun renderSearchableDtoEsIndexServices() {

        this.applicationModelDef.searchableDtoDefs.forEach { searchableDtoDef ->

            searchableDtoDef.esDocDefs.forEach { esDocDef ->
                SearchableDtoEsDocRepoRenderer(searchableDtoDef, esDocDef).renderToDir(this.kotlinOutputDir)
                SearchableDtoEsIndexServiceRenderer(esDocDef).renderToDir(this.kotlinOutputDir)
            }

        }

    }


    /**
     * Rendered here, beside the index services they call, because this module also sees the DTO repos (repo
     * module), the entity CrudListener interfaces (domain module) and the EsRefreshCollector (maia-elasticsearch).
     * Only versions with `autoRefresh = true` get listeners.
     */
    private fun renderSearchableDtoEsRefreshListeners() {

        this.applicationModelDef.searchableDtoDefs.forEach { searchableDtoDef ->
            EsRefreshListeners.renderersFor(searchableDtoDef).forEach { it.renderToDir(this.kotlinOutputDir) }
        }

    }


}

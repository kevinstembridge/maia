package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.CrudNotifierRenderer
import org.maiaframework.gen.renderers.CrudServiceRenderer
import org.maiaframework.gen.renderers.ElasticSearchDtoSearchServiceRenderer
import org.maiaframework.gen.renderers.EntityDetailDtoServiceRenderer
import org.maiaframework.gen.renderers.EntityHistoryBlotterSearchServiceRenderer
import org.maiaframework.gen.renderers.TimelineBlotterSearchServiceRenderer
import org.maiaframework.gen.renderers.ForeignKeyReferenceServiceRenderer
import org.maiaframework.gen.renderers.RequestDtoHandlerRenderer
import org.maiaframework.gen.renderers.SearchDtoSearchServiceRenderer
import org.maiaframework.gen.renderers.TypeaheadCrudListenerRenderer
import org.maiaframework.gen.renderers.TypeaheadServiceRenderer
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.BlotterSearchableDtoSourceDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = ServiceLayerModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class ServiceLayerModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
) : AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        `render CrudServices`()
        `render CrudNotifiers`()
        `render CrudListeners for typeaheads`()
        `render EntityDetailDto services`()
        `render foreign key services`()
        `render typeahead services`()
        `render FormHandlers`()
        `render RequestDtoHandlers`()
        `render SearchableDtoSearchServices`()
        `render TableDtoSearchServices`()
        `render EntityHistoryBlotterServices`()
        `render TimelineBlotterServices`()

    }


    private fun `render CrudServices`() {

        this.applicationModelDef.entityHierarchies
            .filter { it.entityDef.isConcrete && it.entityDef.isHistoryEntity == false }
            .forEach { CrudServiceRenderer(it.entityDef, applicationModelDef).renderToDir(this.kotlinOutputDir) }

    }


    private fun `render CrudNotifiers`() {

        this.applicationModelDef.entityHierarchies
            .filter { it.entityDef.isConcrete && it.entityDef.isHistoryEntity == false && it.entityDef.crudDef.withCrudListener.value }
            .forEach { CrudNotifierRenderer(it.entityDef).renderToDir(this.kotlinOutputDir) }

    }


    private fun `render CrudListeners for typeaheads`() {

        this.applicationModelDef.typeaheadDefs
            .filter { it.crudListenerClassDef != null && it.entityCrudApiDef != null }
            .forEach { typeaheadDef -> TypeaheadCrudListenerRenderer(typeaheadDef).renderToDir(this.kotlinOutputDir) }

    }


    private fun `render EntityDetailDto services`() {

        this.applicationModelDef.entityDetailViewDefs.forEach {
            EntityDetailDtoServiceRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render foreign key services`() {

        this.applicationModelDef.entitiesReferencedByForeignKey.forEach { entityDef ->

            val referencingEntityDefs = this.applicationModelDef.entitiesThatReference(entityDef)
            ForeignKeyReferenceServiceRenderer(entityDef, referencingEntityDefs).renderToDir(this.kotlinOutputDir)

        }

    }


    private fun `render typeahead services`() {

        this.applicationModelDef.typeaheadDefs.forEach {
            TypeaheadServiceRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render FormHandlers`() {

        this.applicationModelDef.angularFormDefs.map { it.requestDtoDef }.filter { it.withGeneratedEndpoint.value }.forEach {
            RequestDtoHandlerRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render RequestDtoHandlers`() {

        this.applicationModelDef.requestDtoDefs.filter { it.withGeneratedEndpoint.value }.forEach {
            RequestDtoHandlerRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render SearchableDtoSearchServices`() {

        this.applicationModelDef.allSearchableDtoDefs.forEach {
            SearchDtoSearchServiceRenderer(it.searchDtoDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render TableDtoSearchServices`() {

        this.applicationModelDef.blotterDefs.forEach {

            when (it.blotterSourceDef) {
                is BlotterEsDocSourceDef -> ElasticSearchDtoSearchServiceRenderer(it.searchDtoDef, (it.blotterSourceDef as BlotterEsDocSourceDef).esDocDef)
                is BlotterSearchableDtoSourceDef -> SearchDtoSearchServiceRenderer(it.searchDtoDef).renderToDir(this.kotlinOutputDir)
            }

        }

    }


    private fun `render EntityHistoryBlotterServices`() {

        this.applicationModelDef.entityHistoryBlotterDefs.forEach { def ->
            EntityHistoryBlotterSearchServiceRenderer(def).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render TimelineBlotterServices`() {

        this.applicationModelDef.timelineBlotterDefs.forEach { def ->
            TimelineBlotterSearchServiceRenderer(def).renderToDir(this.kotlinOutputDir)
        }

    }


}

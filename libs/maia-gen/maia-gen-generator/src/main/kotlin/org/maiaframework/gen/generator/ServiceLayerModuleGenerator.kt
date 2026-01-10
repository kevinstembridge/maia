package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.*
import org.maiaframework.gen.spec.definition.DataSourceType


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = ServiceLayerModuleGenerator(moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class ServiceLayerModuleGenerator(
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
    modelGeneratorContext
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

    }


    private fun `render CrudServices`() {

        this.modelDef.entityHierarchies.filter { it.entityDef.isConcrete && it.entityDef.isHistoryEntity == false }.forEach {
            CrudServiceRenderer(it.entityDef, modelDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render CrudNotifiers`() {

        this.modelDef.entityHierarchies.filter { it.entityDef.isConcrete && it.entityDef.isHistoryEntity == false && it.entityDef.crudDef.withCrudListener.value }
            .forEach {
                CrudNotifierRenderer(it.entityDef).renderToDir(this.kotlinOutputDir)
            }

    }


    private fun `render CrudListeners for typeaheads`() {

        this.modelDef.typeaheadDefs.filter { it.crudListenerClassDef != null && it.entityCrudApiDef != null }.forEach { typeaheadDef ->
            TypeaheadCrudListenerRenderer(typeaheadDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render EntityDetailDto services`() {

        this.modelDef.entityDetailDtoDefs.forEach {
            EntityDetailDtoServiceRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render foreign key services`() {

        this.modelDef.entitiesReferencedByForeignKey.forEach { entityDef ->

            val referencingEntityDefs = this.modelDef.entitiesThatReference(entityDef)
            ForeignKeyReferenceServiceRenderer(entityDef, referencingEntityDefs).renderToDir(this.kotlinOutputDir)

        }

    }


    private fun `render typeahead services`() {

        this.modelDef.typeaheadDefs.forEach {
            TypeaheadServiceRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render FormHandlers`() {

        this.modelDef.angularFormDefs.map { it.requestDtoDef }.filter { it.withGeneratedEndpoint.value }.forEach {
            RequestDtoHandlerRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render RequestDtoHandlers`() {

        this.modelDef.requestDtoDefs.filter { it.withGeneratedEndpoint.value }.forEach {
            RequestDtoHandlerRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render SearchableDtoSearchServices`() {

        this.modelDef.allSearchableDtoDefs.forEach {
            SearchDtoSearchServiceRenderer(it.searchDtoDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render TableDtoSearchServices`() {

        this.modelDef.dtoHtmlTableDefs.forEach {

            when (it.dataSourceType) {
                DataSourceType.ELASTIC_SEARCH -> ElasticSearchDtoSearchServiceRenderer(
                    it.searchDtoDef,
                    it.dtoHtmlTableSourceDef.esDocDef!!
                ).renderToDir(this.kotlinOutputDir)

                DataSourceType.DATABASE -> SearchDtoSearchServiceRenderer(it.searchDtoDef).renderToDir(this.kotlinOutputDir)
            }

        }

    }


}

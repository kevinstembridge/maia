package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.*
import org.maiaframework.gen.renderers.RequestDtoEndpointRenderer
import org.maiaframework.gen.spec.definition.*
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = WebLayerModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class WebLayerModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
): AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        processAngularFormDefs()
        processEntityCrudApiDefs()
        processForeignKeyEntityDefs()
        renderEntityDetailDtoEndpoints()
        renderRequestDtoEndpoints()
        renderResponseDtoEndpoints()
        renderSearchableDtoEndpoints()
        renderTableDtoEndpoints()
        renderTypeaheadEndpoints()

    }


    private fun processEntityCrudApiDefs() {

        this.modelDef.entityCrudApiDefs
            .filter { it.entityDef.isConcrete }
            .forEach { processEntityCrudApiDef(it) }

    }


    private fun processForeignKeyEntityDefs() {

        this.modelDef.entitiesReferencedByForeignKey.forEach { renderCheckForeignKeyReferencesEndpoint(it) }

    }


    private fun processAngularFormDefs() {

        this.modelDef.angularFormDefs.forEach { processRequestDto(it.requestDtoDef) }

    }


    private fun renderEntityDetailDtoEndpoints() {

        this.modelDef.entityDetailDtoDefs.forEach {
            EntityDetailDtoEndpointRenderer(it).renderToDir(kotlinOutputDir)
        }

    }


    private fun renderRequestDtoEndpoints() {

        this.modelDef.requestDtoDefs.forEach { processRequestDto(it) }

    }


    private fun renderResponseDtoEndpoints() {

        this.modelDef.responseDtoDefs.forEach { processResponseDto(it) }

    }


    private fun renderSearchableDtoEndpoints() {

        this.modelDef.allSearchableDtoDefs.forEach { processSearchableDto(it) }

    }


    private fun processSearchableDto(searchableDtoDef: SearchableDtoDef) {

        val searchDtoDef = searchableDtoDef.searchDtoDef

        if (searchDtoDef.withGeneratedEndpoint == WithGeneratedEndpoint.TRUE) {
            DtoSearchEndpointRenderer(searchDtoDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderTableDtoEndpoints() {

        this.modelDef.dtoHtmlTableDefs
            .filter { it.withGeneratedDto.value }
            .forEach { processDtoHtmlDef(it) }

    }


    private fun processDtoHtmlDef(dtoHtmlTableDef: DtoHtmlTableDef) {

        renderTableDtoEndpoint(dtoHtmlTableDef)

    }


    private fun renderTableDtoEndpoint(dtoHtmlTableDef: DtoHtmlTableDef) {

        val searchDtoDef = dtoHtmlTableDef.searchDtoDef

        if (searchDtoDef.withGeneratedEndpoint == WithGeneratedEndpoint.TRUE) {
            DtoSearchEndpointRenderer(searchDtoDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderTypeaheadEndpoints() {

        this.modelDef.typeaheadDefs.forEach { processTypeaheadDef(it) }

    }


    private fun processEntityCrudApiDef(entityCrudApiDef: EntityCrudApiDef) {

        renderCrudEndpoint(entityCrudApiDef)

    }


    private fun processRequestDto(requestDtoDef: RequestDtoDef) {

        if (requestDtoDef.withGeneratedEndpoint.value) {
            RequestDtoEndpointRenderer(requestDtoDef).renderToDir(kotlinOutputDir)
        }

    }


    private fun processResponseDto(responseDtoDef: ResponseDtoDef) {

        renderResponseDtoEndpoint(responseDtoDef)

    }


    private fun processTypeaheadDef(typeaheadDef: TypeaheadDef) {

        renderTypeaheadDtoEndpoint(typeaheadDef)

    }


    private fun renderCrudEndpoint(entityCrudApiDef: EntityCrudApiDef) {

        CrudEndpointRenderer(entityCrudApiDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderEntityDetailEndpoint(entityDetailDtoDef: EntityDetailDtoDef) {

        EntityDetailDtoEndpointRenderer(entityDetailDtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderCheckForeignKeyReferencesEndpoint(entityDef: EntityDef) {

        ForeignKeyReferencesEndpointRenderer(entityDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderResponseDtoEndpoint(responseDtoDef: ResponseDtoDef) {

        ResponseDtoEndpointRenderer(responseDtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderTypeaheadDtoEndpoint(typeaheadDef: TypeaheadDef) {

        TypeaheadEndpointRenderer(typeaheadDef).renderToDir(this.kotlinOutputDir)

    }


}

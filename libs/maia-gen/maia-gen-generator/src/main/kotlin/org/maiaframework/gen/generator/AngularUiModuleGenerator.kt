package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.ui.AgGridDatasourceRenderer
import org.maiaframework.gen.renderers.ui.AngularFormServiceRenderer
import org.maiaframework.gen.renderers.ui.AsyncValidatorRenderer
import org.maiaframework.gen.renderers.ui.CheckForeignKeyReferencesDialogComponentRenderer
import org.maiaframework.gen.renderers.ui.CheckForeignKeyReferencesDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.CrudTableComponentRenderer
import org.maiaframework.gen.renderers.ui.CrudTableHtmlRenderer
import org.maiaframework.gen.renderers.ui.DtoCrudServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.DtoHtmlAgGridTableComponentRenderer
import org.maiaframework.gen.renderers.ui.DtoHtmlAgGridTableHtmlRenderer
import org.maiaframework.gen.renderers.ui.DtoHtmlTableComponentRenderer
import org.maiaframework.gen.renderers.ui.DtoHtmlTableHtmlRenderer
import org.maiaframework.gen.renderers.ui.DtoHtmlTableScssRenderer
import org.maiaframework.gen.renderers.ui.DtoHtmlTableServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateDialogScssRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateFormScssRenderer
import org.maiaframework.gen.renderers.ui.EntityDeleteDialogComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityDeleteDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailDtoComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailDtoHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailDtoServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.EntityEditDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityEditDialogScssRenderer
import org.maiaframework.gen.renderers.ui.EntityEditFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityFormComponentRenderer
import org.maiaframework.gen.renderers.ui.EnumSelectionOptionsTypescriptRenderer
import org.maiaframework.gen.renderers.ui.EnumTypescriptRenderer
import org.maiaframework.gen.renderers.ui.ForeignKeyReferenceServiceRenderer
import org.maiaframework.gen.renderers.ui.FormHtmlRenderer
import org.maiaframework.gen.renderers.ui.FormScssRenderer
import org.maiaframework.gen.renderers.ui.SearchDtoServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.TypeaheadAngularServiceRenderer
import org.maiaframework.gen.renderers.ui.TypeaheadFieldValidatorRenderer
import org.maiaframework.gen.renderers.ui.TypescriptInterfaceDtoRenderer
import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = AngularUiModuleGenerator(it, moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource()

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class AngularUiModuleGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
): AbstractModuleGenerator(
    modelDef,
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        processCrudApiDefs()
        renderAgGridDatasources()
        renderAngularForms()
        renderAsyncValidatorsForIndexes()
        renderAuthorityEnum()
        renderEntityCreateDialogComponent()
        renderEntityCreateFormComponent()
        renderEntityCreateDialogHtml()
        renderEntityCreateFormHtml()
        renderEntityEditFormHtml()
        renderCrudServices()
        renderCrudTables()
        renderEntityDeleteDialogComponent()
        renderEntityDeleteDialogHtml()
        renderDtoHtmlTables()
        renderDtoServices()
        renderDtoTableComponents()
        renderDtosForAsyncValidation()
        renderDtosForFormDefs()
        renderEntityEditDialogComponent()
        renderEntityEditDialogHtml()
        renderEntityDetailDtoComponents()
        renderEntityDetailDtoHtml()
        renderEntityDetailDtoServices()
        renderEntityDetailsDtos()
        renderEnums()
        renderEsDocs()
        renderFetchForEditDtos()
        renderForeignKeyDialogs()
        renderForeignKeyService()
        renderIdAndNameDtos()
        renderRequestDtos()
        renderSimpleResponseDtos()
        renderSearchableResponseDtos()
        renderSearchableServices()
        renderTableDto()
        renderTypeaheadDtos()
        renderTypeaheadServices()
        renderValidatorsForTypeaheadFields()

    }


    private fun renderEnums() {

        this.modelDef.enumDefs.filter { it.withTypescript }.forEach { enumDef ->

            EnumTypescriptRenderer(enumDef).renderToDir(this.typescriptOutputDir)

            if (enumDef.withEnumSelectionOptions) {
                EnumSelectionOptionsTypescriptRenderer(enumDef).renderToDir(this.typescriptOutputDir)
            }

        }

    }


    private fun renderDtosForFormDefs() {

        this.modelDef.angularFormDefs.forEach {
            renderRequestDto(it.requestDtoDef)
        }

    }


    private fun renderAgGridDatasources() {

        this.modelDef.dtoHtmlTableDefs
            .filter { it.searchDtoDef.searchModelType == SearchModelType.AG_GRID }
            .forEach {
                AgGridDatasourceRenderer(it).renderToDir(this.typescriptOutputDir)
            }

    }


    private fun renderAngularForms() {

        this.modelDef.angularFormDefs.forEach {

            FormHtmlRenderer(it).renderToDir(this.typescriptOutputDir)
            EntityFormComponentRenderer(it, it.componentNames).renderToDir(this.typescriptOutputDir)
            FormScssRenderer(it).renderToDir(this.typescriptOutputDir)
            AngularFormServiceRenderer(it).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderSearchableServices() {

        this.modelDef.searchableDtoDefs.map { it.searchDtoDef }.forEach {
            SearchDtoServiceTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderTableDto() {

        this.modelDef.dtoHtmlTableDefs.filter { it.disableRendering == false }.forEach { dtoHtmlTableDef ->

            val renderedFilePath = dtoHtmlTableDef.dtoDef.typescriptRenderedFilePath
            val className = dtoHtmlTableDef.dtoDef.uqcn
            val fields = dtoHtmlTableDef.dtoHtmlTableColumnFields.map { ClassFieldDef.aClassField(it.dtoFieldName, it.fieldType).build() }

            TypescriptInterfaceDtoRenderer(
                renderedFilePath,
                className,
                fields,
                setOf(DtoCharacteristic.RESPONSE_DTO)
            ).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun processCrudApiDefs() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach {

            it.createApiDef?.let { apiDef ->

                renderRequestDto(apiDef.requestDtoDef)

                apiDef.crudApiDef.context?.let { contextDtoDef ->
                    renderRequestDto(contextDtoDef)
                }

            }

            it.updateApiDef?.let { apiDef ->
                renderRequestDto(apiDef.requestDtoDef)
            }

        }

    }


    private fun renderRequestDto(requestDtoDef: RequestDtoDef) {

        val renderedFilePath = requestDtoDef.typescriptDtoRenderedFilePath
        val className = requestDtoDef.uqcn
        val fields = requestDtoDef.classFieldDefs

        TypescriptInterfaceDtoRenderer(
            renderedFilePath,
            className,
            fields,
            emptySet()
        ).renderToDir(this.typescriptOutputDir)

    }


    private fun renderCrudServices() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->
            DtoCrudServiceTypescriptRenderer(entityCrudApiDef).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderDtoTableComponents() {

        this.modelDef.dtoHtmlTableDefs.forEach {

            when (it.searchModelType) {
                SearchModelType.AG_GRID -> DtoHtmlAgGridTableComponentRenderer(it, this.modelDef.authoritiesDef).renderToDir(this.typescriptOutputDir)
                SearchModelType.MAIA -> DtoHtmlTableComponentRenderer(it).renderToDir(this.typescriptOutputDir)
            }

        }

    }


    private fun renderEntityDetailDtoComponents() {

        this.modelDef.entityDetailDtoDefs.forEach {

            EntityDetailDtoComponentRenderer(it).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderEntityDetailDtoHtml() {

        this.modelDef.entityDetailDtoDefs.forEach {

            EntityDetailDtoHtmlRenderer(it).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderEntityDetailDtoServices() {

        this.modelDef.entityDetailDtoDefs.forEach {

            EntityDetailDtoServiceTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderDtoHtmlTables() {

        this.modelDef.dtoHtmlTableDefs.forEach {

            when (it.searchModelType) {
                SearchModelType.AG_GRID -> DtoHtmlAgGridTableHtmlRenderer(it).renderToDir(this.typescriptOutputDir)
                SearchModelType.MAIA -> {
                    DtoHtmlTableHtmlRenderer(it).renderToDir(this.typescriptOutputDir)
                    DtoHtmlTableScssRenderer(it).renderToDir(this.typescriptOutputDir)
                }
            }

        }

    }


    private fun renderCrudTables() {

        this.modelDef.crudTableDefs.forEach { crudTableDef ->

            val entityIsReferencedByForeignKeys = this.modelDef.entityIsReferencedByForeignKeys(crudTableDef.entityCrudApiDef.entityDef)
            CrudTableHtmlRenderer(crudTableDef).renderToDir(this.typescriptOutputDir)
            CrudTableComponentRenderer(crudTableDef, entityIsReferencedByForeignKeys).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderDtoServices() {

        this.modelDef.dtoHtmlTableDefs.forEach {
            DtoHtmlTableServiceTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderEntityCreateDialogHtml() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->
            entityCrudApiDef.createApiDef?.let { apiDef ->
                EntityCreateDialogHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
            }
        }

    }


    private fun renderEntityCreateFormHtml() {

        this.modelDef.entityCrudApiDefs
            .filter { it.entityDef.isConcrete && (it.createApiDef?.crudApiDef?.withEntityForm ?: false) }
            .forEach { entityCrudApiDef ->
                entityCrudApiDef.createApiDef?.let { apiDef ->
                    EntityCreateFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
                }
           }

    }


    private fun renderEntityEditFormHtml() {

        this.modelDef.entityCrudApiDefs
            .filter { it.entityDef.isConcrete && (it.updateApiDef?.crudApiDef?.withEntityForm ?: false) }
            .forEach { entityCrudApiDef ->
                entityCrudApiDef.updateApiDef?.let { apiDef ->
                    EntityEditFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
                }
           }

    }


    private fun renderAuthorityEnum() {

        this.modelDef.authoritiesDef?.let { EnumTypescriptRenderer(it.enumDef).renderToDir(this.typescriptOutputDir) }

    }


    private fun renderEntityCreateDialogComponent() {

        this.modelDef.entityCrudApiDefs.mapNotNull { it.createApiDef }.filter { it.entityDef.isConcrete }.forEach {

            EntityFormComponentRenderer(it.angularDialogDef, it.angularDialogComponentNames).renderToDir(this.typescriptOutputDir)
            EntityCreateDialogScssRenderer(it).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderEntityCreateFormComponent() {

        this.modelDef.entityCrudApiDefs.mapNotNull { it.createApiDef }.filter { it.entityDef.isConcrete && it.crudApiDef.withEntityForm }.forEach {

            it.angularInlineFormDef?.let { formDef ->
                EntityFormComponentRenderer(formDef, it.angularFormComponentNames).renderToDir(this.typescriptOutputDir)
                EntityCreateFormScssRenderer(it).renderToDir(this.typescriptOutputDir)
            }

        }

    }


    private fun renderEntityEditDialogHtml() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->

            entityCrudApiDef.updateApiDef?.let { apiDef ->

                EntityEditDialogHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
                EntityEditDialogScssRenderer(apiDef).renderToDir(this.typescriptOutputDir)

            }

        }

    }


    private fun renderEntityEditDialogComponent() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->
            entityCrudApiDef.updateApiDef?.let { apiDef ->
                if (entityCrudApiDef.entityDef.isModifiable) {
                    EntityFormComponentRenderer(apiDef.angularDialogDef, apiDef.angularDialogComponentNames).renderToDir(this.typescriptOutputDir)
                }
            }
        }

    }


    private fun renderEntityDeleteDialogHtml() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->

            entityCrudApiDef.deleteApiDef?.let { apiDef ->

                EntityDeleteDialogHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)

            }

        }

    }


    private fun renderEntityDeleteDialogComponent() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->
            entityCrudApiDef.deleteApiDef?.let { apiDef ->
                EntityDeleteDialogComponentRenderer(apiDef).renderToDir(this.typescriptOutputDir)
            }
        }

    }


    private fun renderTypeaheadDtos() {

        this.modelDef.typeaheadDefs.forEach {
            val dtoDef = it.esDocDef.dtoDef
            TypescriptInterfaceDtoRenderer(
                dtoDef.typescriptRenderedFilePath,
                dtoDef.uqcn,
                dtoDef.allFieldsSorted,
                setOf(DtoCharacteristic.RESPONSE_DTO)
            ).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderTypeaheadServices() {

        this.modelDef.typeaheadDefs.forEach {
            TypeaheadAngularServiceRenderer(it).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderValidatorsForTypeaheadFields() {

        this.modelDef.entityHierarchies.flatMap { it.entityDefs }.flatMap { it.allTypeaheadFields }.forEach { typeaheadFieldDef ->
            TypeaheadFieldValidatorRenderer(typeaheadFieldDef).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderAsyncValidatorsForIndexes() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->

            entityCrudApiDef.entityDef.uniqueIndexDefs.filter { it.isNotIdAndVersionIndex }.forEach { databaseIndexDef ->
                AsyncValidatorRenderer(databaseIndexDef, entityCrudApiDef).renderToDir(this.typescriptOutputDir)
            }

        }

    }


    private fun renderDtosForAsyncValidation() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->

            entityCrudApiDef.entityDef.databaseIndexDefs.forEach { entityIndexDef ->

                val renderedFilePath = entityIndexDef.asyncValidator.asyncValidationDtoRenderedFilePath
                val className = entityIndexDef.asyncValidator.asyncValidationDtoUqcn
                val fields = entityIndexDef.indexDef.indexFieldDefs.map { it.entityFieldDef.classFieldDef }

                TypescriptInterfaceDtoRenderer(
                    renderedFilePath,
                    className,
                    fields,
                    emptySet()
                ).renderToDir(this.typescriptOutputDir)

            }

        }

    }


    private fun renderForeignKeyService() {

        val foreignKeyEntityDefs = this.modelDef.entitiesReferencedByForeignKey

        if (foreignKeyEntityDefs.isNotEmpty()) {
            ForeignKeyReferenceServiceRenderer(foreignKeyEntityDefs).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderForeignKeyDialogs() {

        this.modelDef.entitiesReferencedByForeignKey.forEach { entityDef ->

            CheckForeignKeyReferencesDialogComponentRenderer(entityDef).renderToDir(this.typescriptOutputDir)
            CheckForeignKeyReferencesDialogHtmlRenderer(entityDef).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderFetchForEditDtos() {

        this.modelDef.fetchForEditDtoDefs.forEach { fetchForEditDtoDef ->

            val dtoDef = fetchForEditDtoDef.dtoDef
            val renderedFilePath = dtoDef.typescriptRenderedFilePath
            val className = dtoDef.uqcn
            val fields = dtoDef.allFieldsSorted

            TypescriptInterfaceDtoRenderer(
                renderedFilePath,
                className,
                fields,
                setOf(DtoCharacteristic.RESPONSE_DTO)
            ).renderToDir(typescriptOutputDir)

        }

    }


    private fun renderEntityDetailsDtos() {

        this.modelDef.entityDetailDtoDefs.forEach { entityDetailDtoDef ->

            val dtoDef = entityDetailDtoDef.dtoDef
            val renderedFilePath = dtoDef.typescriptRenderedFilePath
            val className = dtoDef.uqcn
            val fields = dtoDef.allFieldsSorted

            TypescriptInterfaceDtoRenderer(
                renderedFilePath,
                className,
                fields,
                setOf(DtoCharacteristic.RESPONSE_DTO)
            ).renderToDir(typescriptOutputDir)

        }

    }


    private fun renderEsDocs() {

        this.modelDef.esDocsDefs.filter { it.disableRendering == false }.forEach { esDocDef ->

            renderDtoDef(esDocDef.dtoDef)

        }

    }


    private fun renderSimpleResponseDtos() {

        this.modelDef.simpleResponseDtoDefs.map { it.dtoDef }.forEach { dtoDef ->

            renderDtoDef(dtoDef)

        }

    }


    private fun renderSearchableResponseDtos() {

        this.modelDef.searchableDtoDefs.filter { it.withGeneratedDto.value }.map { it.dtoDef }.forEach { dtoDef ->

            renderDtoDef(dtoDef)

        }

    }


    private fun renderDtoDef(classDef: ClassDef) {

        val renderedFilePath = classDef.typescriptRenderedFilePath
        val className = classDef.uqcn
        val fields = classDef.allFieldsSorted

        TypescriptInterfaceDtoRenderer(
            renderedFilePath,
            className,
            fields,
            setOf(DtoCharacteristic.RESPONSE_DTO)
        ).renderToDir(this.typescriptOutputDir)

    }


    private fun renderIdAndNameDtos() {

        this.modelDef.entityHierarchies.map { it.entityDef }.filter { it.hasIdAndNameDtoDef }.forEach { entityDef ->
            val dtoDef = entityDef.entityIdAndNameDef.dtoDef
            TypescriptInterfaceDtoRenderer(
                dtoDef.typescriptRenderedFilePath,
                dtoDef.uqcn,
                dtoDef.allFieldsSorted,
                setOf(DtoCharacteristic.RESPONSE_DTO)
            ).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderRequestDtos() {

        this.modelDef.requestDtoDefs.forEach { requestDtoDef ->

            val renderedFilePath = requestDtoDef.typescriptDtoRenderedFilePath
            val className = requestDtoDef.classDef.uqcn
            val fields = requestDtoDef.classDef.allFieldsSorted

            TypescriptInterfaceDtoRenderer(
                renderedFilePath,
                className,
                fields,
                emptySet()
            ).renderToDir(this.typescriptOutputDir)

        }

    }


}

package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.ui.*
import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.Uqcn


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = AngularUiModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class AngularUiModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
): AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        processCrudApiDefs()
        renderAgGridDatasources()
        renderAngularForms()
        renderAsyncValidatorsForIndexes()
        renderAuthorityEnum()
        renderCommonModel()
        renderCrudServices()
        renderCrudTables()
        renderDtoHtmlTables()
        renderDtoServices()
        renderDtoTableComponents()
        renderDtosForAsyncValidation()
        renderDtosForFormDefs()
        renderEntityCreateDialogComponent()
        renderEntityCreateFormComponent()
        renderEntityCreateDialogHtml()
        renderEntityCreateFormHtml()
        renderEntityEditFormHtml()
        renderEntityDeleteDialogComponent()
        renderEntityDeleteDialogHtml()
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

            renderTypescriptInterface(
                renderedFilePath = dtoHtmlTableDef.dtoDef.typescriptRenderedFilePath,
                className = dtoHtmlTableDef.dtoDef.uqcn,
                fields = dtoHtmlTableDef.dtoHtmlTableColumnFields.map {
                    aClassField(it.dtoFieldName, it.fieldType).build()
                },
                dtoCharacteristics = setOf(DtoCharacteristic.RESPONSE_DTO)
            )

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

        renderTypescriptInterface(
            renderedFilePath = requestDtoDef.typescriptDtoRenderedFilePath,
            className = requestDtoDef.uqcn,
            fields = requestDtoDef.classFieldDefs,
            dtoCharacteristics = emptySet()
        )

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


    private fun renderCommonModel() {

        FormValidationResponseDtoRenderer().renderToDir(this.typescriptOutputDir)

    }


    private fun renderEntityCreateDialogComponent() {

        this.modelDef.entityCrudApiDefs
            .mapNotNull { it.createApiDef }
            .filter { it.entityDef.isConcrete }
            .forEach {

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

        this.modelDef.typeaheadDefs
            .map { it.esDocDef.dtoDef }
            .forEach { renderTypescriptInterface(it) }

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

                renderTypescriptInterface(
                    renderedFilePath = entityIndexDef.asyncValidator.asyncValidationDtoRenderedFilePath,
                    className = entityIndexDef.asyncValidator.asyncValidationDtoUqcn,
                    fields = entityIndexDef.indexDef.indexFieldDefs.map { it.entityFieldDef.classFieldDef },
                    dtoCharacteristics = emptySet()
                )

            }

        }

    }


    private fun renderForeignKeyService() {

        val foreignKeyEntityDefs = this.modelDef.entitiesReferencedByForeignKey

        if (foreignKeyEntityDefs.isNotEmpty()) {
            ForeignKeyReferenceServiceRenderer(foreignKeyEntityDefs).renderToDir(this.typescriptOutputDir)
            ForeignKeyReferencesExistResponseDtoRenderer().renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderForeignKeyDialogs() {

        this.modelDef.entitiesReferencedByForeignKey.forEach { entityDef ->

            CheckForeignKeyReferencesDialogComponentRenderer(entityDef).renderToDir(this.typescriptOutputDir)
            CheckForeignKeyReferencesDialogHtmlRenderer(entityDef).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderFetchForEditDtos() {

        modelDef.fetchForEditDtoDefs
            .map { it.dtoDef }
            .forEach { dtoDef -> renderTypescriptInterface(dtoDef) }

    }


    private fun renderEntityDetailsDtos() {

        modelDef.entityDetailDtoDefs
            .map { it.dtoDef }
            .forEach { dtoDef -> renderTypescriptInterface(dtoDef) }

    }


    private fun renderEsDocs() {

        modelDef.esDocsDefs
            .filter { it.disableRendering == false }
            .map { it.dtoDef }
            .forEach { dtoDef -> renderTypescriptInterface(dtoDef) }

    }


    private fun renderSimpleResponseDtos() {

        this.modelDef.simpleResponseDtoDefs
            .map { it.dtoDef }
            .forEach { dtoDef -> renderTypescriptInterface(dtoDef) }

    }


    private fun renderSearchableResponseDtos() {

        this.modelDef.searchableDtoDefs
            .filter { it.withGeneratedDto.value }
            .map { it.dtoDef }
            .forEach { dtoDef -> renderTypescriptInterface(dtoDef) }

    }


    private fun renderIdAndNameDtos() {

        this.modelDef.entityHierarchies
            .map { it.entityDef }
            .filter { it.hasIdAndNameDtoDef }
            .map { it.entityPkAndNameDef.dtoDef }
            .forEach { dtoDef -> renderTypescriptInterface(dtoDef) }

    }


    private fun renderRequestDtos() {

        this.modelDef.requestDtoDefs.forEach { requestDtoDef ->

            renderTypescriptInterface(
                renderedFilePath = requestDtoDef.typescriptDtoRenderedFilePath,
                className = requestDtoDef.classDef.uqcn,
                fields = requestDtoDef.classDef.allFieldsSorted,
                dtoCharacteristics = emptySet()
            )

        }

    }


    private fun renderTypescriptInterface(classDef: ClassDef) {

        renderTypescriptInterface(
            renderedFilePath = classDef.typescriptRenderedFilePath,
            className = classDef.uqcn,
            fields = classDef.allFieldsSorted,
            dtoCharacteristics = setOf(DtoCharacteristic.RESPONSE_DTO)
        )

    }


    private fun renderTypescriptInterface(
        renderedFilePath: String,
        className: Uqcn,
        fields: List<ClassFieldDef>,
        dtoCharacteristics: Set<DtoCharacteristic>
    ) {

        TypescriptInterfaceDtoRenderer(
            renderedFilePath,
            className,
            fields,
            dtoCharacteristics
        ).renderToDir(this.typescriptOutputDir)

    }


}

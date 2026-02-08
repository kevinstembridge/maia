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
import org.maiaframework.gen.renderers.ui.EntityCreateDialogReactiveFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateDialogScssRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateFormScssRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateReactiveFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityDeleteDialogComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityDeleteDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailDtoComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailDtoHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailDtoServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.EntityEditDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityEditDialogScssRenderer
import org.maiaframework.gen.renderers.ui.EntityEditFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityEditReactiveDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityEditReactiveFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityFormComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityReactiveFormComponentRenderer
import org.maiaframework.gen.renderers.ui.EnumSelectionOptionsTypescriptRenderer
import org.maiaframework.gen.renderers.ui.EnumTypescriptRenderer
import org.maiaframework.gen.renderers.ui.ForeignKeyReferenceServiceRenderer
import org.maiaframework.gen.renderers.ui.ForeignKeyReferencesExistResponseDtoRenderer
import org.maiaframework.gen.renderers.ui.FormHtmlRenderer
import org.maiaframework.gen.renderers.ui.FormScssRenderer
import org.maiaframework.gen.renderers.ui.FormValidationResponseDtoRenderer
import org.maiaframework.gen.renderers.ui.IndexSearchResultResponseDtoRenderer
import org.maiaframework.gen.renderers.ui.ProblemDetailRenderer
import org.maiaframework.gen.renderers.ui.SearchDtoServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.SearchResultPageResponseDtoRenderer
import org.maiaframework.gen.renderers.ui.TotalHitsRelationRenderer
import org.maiaframework.gen.renderers.ui.TotalHitsResponseDtoRenderer
import org.maiaframework.gen.renderers.ui.TypeaheadAngularServiceRenderer
import org.maiaframework.gen.renderers.ui.TypeaheadFieldValidatorRenderer
import org.maiaframework.gen.renderers.ui.TypescriptInterfaceDtoRenderer
import org.maiaframework.gen.spec.definition.AngularComponentNames
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.AngularFormSystem
import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.EntityCreateApiDef
import org.maiaframework.gen.spec.definition.EntityUpdateApiDef
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
            renderEntityForm(it, it.componentNames)
            FormScssRenderer(it).renderToDir(this.typescriptOutputDir)
            AngularFormServiceRenderer(it).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderEntityForm(def: AngularFormDef, angularComponentNames: AngularComponentNames) {

        when (def.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityReactiveFormComponentRenderer(def, angularComponentNames).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityFormComponentRenderer(def, angularComponentNames).renderToDir(this.typescriptOutputDir)
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
                renderEntityCreateDialogHtml(apiDef)
            }
        }

    }


    private fun renderEntityCreateDialogHtml(apiDef: EntityCreateApiDef) {

        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityCreateDialogReactiveFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityCreateDialogHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderEntityCreateFormHtml() {

        this.modelDef.entityCrudApiDefs
            .filter { it.entityDef.isConcrete && (it.createApiDef?.crudApiDef?.withEntityForm ?: false) }
            .forEach { entityCrudApiDef ->
                entityCrudApiDef.createApiDef?.let { apiDef ->
                    renderEntityCreateHtmlForm(apiDef)
                }
           }

    }


    private fun renderEntityCreateHtmlForm(apiDef: EntityCreateApiDef) {

        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityCreateReactiveFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityCreateFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
        }


    }


    private fun renderEntityEditFormHtml() {

        this.modelDef.entityCrudApiDefs
            .filter { it.entityDef.isConcrete && (it.updateApiDef?.crudApiDef?.withEntityForm ?: false) }
            .forEach { entityCrudApiDef ->
                entityCrudApiDef.updateApiDef?.let { apiDef ->
                    renderEntityEditHtmlForm(apiDef)
                }
           }

    }


    private fun renderEntityEditHtmlForm(apiDef: EntityUpdateApiDef) {

        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityEditReactiveFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityEditFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderAuthorityEnum() {

        this.modelDef.authoritiesDef?.let { EnumTypescriptRenderer(it.enumDef).renderToDir(this.typescriptOutputDir) }

    }


    private fun renderCommonModel() {

        FormValidationResponseDtoRenderer().renderToDir(this.typescriptOutputDir)
        TotalHitsRelationRenderer().renderToDir(this.typescriptOutputDir)
        TotalHitsResponseDtoRenderer().renderToDir(this.typescriptOutputDir)
        ProblemDetailRenderer().renderToDir(this.typescriptOutputDir)
        SearchResultPageResponseDtoRenderer().renderToDir(this.typescriptOutputDir)
        IndexSearchResultResponseDtoRenderer().renderToDir(this.typescriptOutputDir)

    }


    private fun renderEntityCreateDialogComponent() {

        this.modelDef.entityCrudApiDefs
            .mapNotNull { it.createApiDef }
            .filter { it.entityDef.isConcrete }
            .forEach {

                renderEntityForm(it.angularDialogDef, it.angularDialogComponentNames)
                EntityCreateDialogScssRenderer(it).renderToDir(this.typescriptOutputDir)

            }

    }


    private fun renderEntityCreateFormComponent() {

        this.modelDef.entityCrudApiDefs.mapNotNull { it.createApiDef }.filter { it.entityDef.isConcrete && it.crudApiDef.withEntityForm }.forEach {

            it.angularInlineFormDef?.let { formDef ->
                renderEntityForm(formDef, it.angularFormComponentNames)
                EntityCreateFormScssRenderer(it).renderToDir(this.typescriptOutputDir)
            }

        }

    }


    private fun renderEntityEditDialogHtml() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->

            entityCrudApiDef.updateApiDef?.let { apiDef ->

                renderEntityEditDialogHtml(apiDef)
                EntityEditDialogScssRenderer(apiDef).renderToDir(this.typescriptOutputDir)

            }

        }

    }


    private fun renderEntityEditDialogHtml(apiDef: EntityUpdateApiDef) {

        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityEditReactiveDialogHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityEditDialogHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderEntityEditDialogComponent() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->
            entityCrudApiDef.updateApiDef?.let { apiDef ->
                if (entityCrudApiDef.entityDef.isModifiable) {
                    renderEntityForm(apiDef.angularDialogDef, apiDef.angularDialogComponentNames)
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

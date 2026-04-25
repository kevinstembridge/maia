package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.ui.AgGridDatasourceRenderer
import org.maiaframework.gen.renderers.ui.AngularFormServiceRenderer
import org.maiaframework.gen.renderers.ui.AsyncValidatorRenderer
import org.maiaframework.gen.renderers.ui.AuthApiServiceRenderer
import org.maiaframework.gen.renderers.ui.AuthGuardRenderer
import org.maiaframework.gen.renderers.ui.AuthServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.CheckForeignKeyReferencesDialogComponentRenderer
import org.maiaframework.gen.renderers.ui.CheckForeignKeyReferencesDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.CrudBlotterComponentRenderer
import org.maiaframework.gen.renderers.ui.CrudBlotterHtmlRenderer
import org.maiaframework.gen.renderers.ui.CurrentUserStoreRenderer
import org.maiaframework.gen.renderers.ui.SigninRequestDtoRenderer
import org.maiaframework.gen.renderers.ui.UserSummaryDtoRenderer
import org.maiaframework.gen.renderers.ui.DtoCrudServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.AgGridBlotterComponentRenderer
import org.maiaframework.gen.renderers.ui.AgGridBlotterHtmlRenderer
import org.maiaframework.gen.renderers.ui.BlotterComponentRenderer
import org.maiaframework.gen.renderers.ui.BlotterHtmlRenderer
import org.maiaframework.gen.renderers.ui.BlotterScssRenderer
import org.maiaframework.gen.renderers.ui.BlotterServiceTypescriptRenderer
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
import org.maiaframework.gen.renderers.ui.ManyToManyChipFieldDef
import org.maiaframework.gen.renderers.ui.SearchDtoServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.TypeaheadAngularServiceRenderer
import org.maiaframework.gen.renderers.ui.TypeaheadFieldValidatorRenderer
import org.maiaframework.gen.renderers.ui.TypescriptInterfaceDtoRenderer
import org.maiaframework.gen.spec.definition.AngularComponentNames
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.AngularFormSystem
import org.maiaframework.gen.spec.definition.DtoCharacteristic
import org.maiaframework.gen.spec.definition.EntityCreateApiDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityUpdateApiDef
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
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

    private val typeaheadByEntityDef by lazy {
        this.modelDef.typeaheadDefs
            .mapNotNull { td -> td.esDocDef.entityDef?.let { it to td } }
            .toMap()
    }


    // TODO MTM: This should live in the model somewhere, not in the generator
    private fun manyToManyChipFieldsFor(
        entityDef: EntityDef,
        associations: List<ManyToManyEntityDef>
    ): List<ManyToManyChipFieldDef> {

        return associations.mapNotNull { m2m ->
            val otherSide = m2m.otherSideFrom(entityDef)
            val typeaheadDef = typeaheadByEntityDef[otherSide.entityDef] ?: return@mapNotNull null
            ManyToManyChipFieldDef(entityDef, m2m, typeaheadDef)
        }

    }


    override fun onGenerateSource() {

        processCrudApiDefs()
        renderAgGridDataSources()
        renderAngularForms()
        renderAsyncValidatorsForIndexes()
        renderAuthorityEnum()
        renderCommonModel()
        renderCrudServices()
        renderCrudBlotters()
        renderBlotterHtml()
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
        renderPkAndNameDtos()
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


    private fun renderAgGridDataSources() {

        this.modelDef.blotterDefs
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

    // TODO MTM: Why are chipFields being passed in here?
    private fun renderEntityForm(
        def: AngularFormDef,
        angularComponentNames: AngularComponentNames,
        chipFields: List<ManyToManyChipFieldDef> = emptyList()
    ) {

        when (def.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityReactiveFormComponentRenderer(def, angularComponentNames, chipFields).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityFormComponentRenderer(def, angularComponentNames).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderSearchableServices() {

        this.modelDef.searchableDtoDefs
            .filter { it.withGeneratedTypescriptService.value }
            .map { it.searchDtoDef }.forEach {
            SearchDtoServiceTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderTableDto() {

        this.modelDef.blotterDefs.filter { it.disableRendering == false }.forEach { blotterDef ->

            renderTypescriptInterface(
                renderedFilePath = blotterDef.dtoDef.typescriptRenderedFilePath,
                className = blotterDef.dtoDef.uqcn,
                fields = blotterDef.blotterColumnFields.map {
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

        this.modelDef.blotterDefs.forEach {

            when (it.searchModelType) {
                SearchModelType.AG_GRID -> AgGridBlotterComponentRenderer(it, this.modelDef.authoritiesDef).renderToDir(this.typescriptOutputDir)
                SearchModelType.MAIA -> BlotterComponentRenderer(it).renderToDir(this.typescriptOutputDir)
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


    private fun renderBlotterHtml() {

        this.modelDef.blotterDefs.forEach {

            when (it.searchModelType) {
                SearchModelType.AG_GRID -> AgGridBlotterHtmlRenderer(it).renderToDir(this.typescriptOutputDir)
                SearchModelType.MAIA -> {
                    BlotterHtmlRenderer(it).renderToDir(this.typescriptOutputDir)
                    BlotterScssRenderer(it).renderToDir(this.typescriptOutputDir)
                }
            }

        }

    }


    private fun renderCrudBlotters() {

        this.modelDef.crudBlotterDefs.forEach { crudBlotterDef ->

            val entityIsReferencedByForeignKeys = this.modelDef.entityIsReferencedByForeignKeys(crudBlotterDef.entityCrudApiDef.entityDef)
            CrudBlotterHtmlRenderer(crudBlotterDef).renderToDir(this.typescriptOutputDir)
            CrudBlotterComponentRenderer(crudBlotterDef, entityIsReferencedByForeignKeys).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderDtoServices() {

        this.modelDef.blotterDefs.forEach {
            BlotterServiceTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)
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

        // TODO MTM:
        val chipFields = manyToManyChipFieldsFor(apiDef.entityDef, apiDef.crudApiDef.manyToManyAssociations)
        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityCreateDialogReactiveFormHtmlRenderer(apiDef, chipFields).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityCreateDialogHtmlRenderer(apiDef, chipFields).renderToDir(this.typescriptOutputDir)
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

        this.modelDef.authoritiesDef?.enumDef?.let {

            EnumTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)

            if (it.withEnumSelectionOptions) {
                EnumSelectionOptionsTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)
            }

        }

    }


    private fun renderCommonModel() {

        // TODO instead of rendering these, move them into maia-ui

//        FormValidationResponseDtoRenderer().renderToDir(this.typescriptOutputDir)
//        TotalHitsRelationRenderer().renderToDir(this.typescriptOutputDir)
//        TotalHitsResponseDtoRenderer().renderToDir(this.typescriptOutputDir)
//        ProblemDetailRenderer().renderToDir(this.typescriptOutputDir)
//        SearchResultPageResponseDtoRenderer().renderToDir(this.typescriptOutputDir)
//        IndexSearchResultResponseDtoRenderer().renderToDir(this.typescriptOutputDir)

        this.modelDef.authoritiesDef?.let { authoritiesDef ->

            AuthApiServiceRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            AuthGuardRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            AuthServiceTypescriptRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            CurrentUserStoreRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            SigninRequestDtoRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            UserSummaryDtoRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderEntityCreateDialogComponent() {

        this.modelDef.entityCrudApiDefs
            .mapNotNull { it.createApiDef }
            .filter { it.entityDef.isConcrete }
            .forEach {

                // TODO MTM:
                val chipFields = manyToManyChipFieldsFor(it.entityDef, it.crudApiDef.manyToManyAssociations)
                renderEntityForm(it.angularDialogDef, it.angularDialogComponentNames, chipFields)
                EntityCreateDialogScssRenderer(it).renderToDir(this.typescriptOutputDir)

            }

    }


    private fun renderEntityCreateFormComponent() {

        this.modelDef.entityCrudApiDefs.mapNotNull { it.createApiDef }
            .filter { it.entityDef.isConcrete && it.crudApiDef.withEntityForm }
            .forEach {

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

        // TODO MTM:
        val chipFields = manyToManyChipFieldsFor(apiDef.entityDef, apiDef.crudApiDef.manyToManyAssociations)
        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityEditReactiveDialogHtmlRenderer(apiDef, chipFields).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityEditDialogHtmlRenderer(apiDef, chipFields).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderEntityEditDialogComponent() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->
            entityCrudApiDef.updateApiDef?.let { apiDef ->
                if (entityCrudApiDef.entityDef.isModifiable) {
                    // TODO MTM:
                    val chipFields = manyToManyChipFieldsFor(apiDef.entityDef, apiDef.crudApiDef.manyToManyAssociations)
                    renderEntityForm(apiDef.angularDialogDef, apiDef.angularDialogComponentNames, chipFields)
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

            entityCrudApiDef.entityDef.uniqueIndexDefs
                .filter { it.isNotIdAndVersionIndex }
                .filter { it.withExistsEndpoint }
                .forEach { databaseIndexDef ->
                    AsyncValidatorRenderer(databaseIndexDef, entityCrudApiDef).renderToDir(this.typescriptOutputDir)
                }

        }

    }


    private fun renderDtosForAsyncValidation() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->

            entityCrudApiDef.entityDef.databaseIndexDefs.filter { it.withExistsEndpoint }.forEach { entityIndexDef ->

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


    private fun renderPkAndNameDtos() {

        this.modelDef.entityHierarchies
            .map { it.entityDef }
            .filter { it.hasPkAndNameDtoDef }
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

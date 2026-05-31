package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.ui.AgGridBlotterComponentRenderer
import org.maiaframework.gen.renderers.ui.AgGridBlotterHtmlRenderer
import org.maiaframework.gen.renderers.ui.AgGridDatasourceRenderer
import org.maiaframework.gen.renderers.ui.AngularFormServiceRenderer
import org.maiaframework.gen.renderers.ui.AsyncValidatorRenderer
import org.maiaframework.gen.renderers.ui.AuthApiServiceRenderer
import org.maiaframework.gen.renderers.ui.AuthGuardRenderer
import org.maiaframework.gen.renderers.ui.AuthServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.BlotterComponentRenderer
import org.maiaframework.gen.renderers.ui.BlotterHtmlRenderer
import org.maiaframework.gen.renderers.ui.BlotterScssRenderer
import org.maiaframework.gen.renderers.ui.BlotterServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.CheckForeignKeyReferencesDialogComponentRenderer
import org.maiaframework.gen.renderers.ui.CheckForeignKeyReferencesDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.BlotterPageComponentRenderer
import org.maiaframework.gen.renderers.ui.BlotterPageHtmlRenderer
import org.maiaframework.gen.renderers.ui.CurrentUserStoreRenderer
import org.maiaframework.gen.renderers.ui.DtoCrudServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateDialogReactiveFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateDialogScssRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.AngularReactiveFormComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateFormPageComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateFormScssRenderer
import org.maiaframework.gen.renderers.ui.EntityCreatePageHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityCreateReactiveFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityDeleteDialogComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityDeleteDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailDtoServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailViewComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailViewContentHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailViewPageComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityDetailViewPageHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityEditDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityEditDialogScssRenderer
import org.maiaframework.gen.renderers.ui.EntityEditFormPageComponentRenderer
import org.maiaframework.gen.renderers.ui.EntityEditFormScssRenderer
import org.maiaframework.gen.renderers.ui.EntityEditPageHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityEditReactiveDialogHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityEditReactiveFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.EntityEditSignalFormHtmlRenderer
import org.maiaframework.gen.renderers.ui.AngularSignalFormComponentRenderer
import org.maiaframework.gen.renderers.ui.AngularReactiveFormComponentRenderer_old
import org.maiaframework.gen.renderers.ui.EnumSelectionOptionsTypescriptRenderer
import org.maiaframework.gen.renderers.ui.EnumTypescriptRenderer
import org.maiaframework.gen.renderers.ui.ForeignKeyReferenceServiceRenderer
import org.maiaframework.gen.renderers.ui.ForeignKeyReferencesExistResponseDtoRenderer
import org.maiaframework.gen.renderers.ui.FormHtmlRenderer
import org.maiaframework.gen.renderers.ui.FormScssRenderer
import org.maiaframework.gen.renderers.ui.EntityCrudRoutesRenderer
import org.maiaframework.gen.renderers.ui.ManyToManyChipFieldDef
import org.maiaframework.gen.renderers.ui.SearchDtoServiceTypescriptRenderer
import org.maiaframework.gen.renderers.ui.SigninRequestDtoRenderer
import org.maiaframework.gen.renderers.ui.TypeaheadAngularServiceRenderer
import org.maiaframework.gen.renderers.ui.TypeaheadFieldValidatorRenderer
import org.maiaframework.gen.renderers.ui.TypescriptInterfaceDtoRenderer
import org.maiaframework.gen.renderers.ui.UserSummaryDtoRenderer
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
import org.maiaframework.gen.spec.definition.flags.FormPurpose
import org.maiaframework.gen.spec.definition.flags.DelegateFormSubmission
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnError
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnSuccess
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
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
//        renderCrudBlotters() TODO delete after cooling off. 2026-05-24
        renderBlotterHtml()
        renderDtoServices()
        renderBlotterComponents()
        renderDtosForAsyncValidation()
        renderDtosForFormDefs()
//        renderEntityCreateDialogComponent()
//        renderEntityCreateFormComponent() TODO delete this after cooling off. 2026-05-21
//        renderEntityCreateDialogHtml()
        renderEntityDeleteDialogComponent()
        renderEntityDeleteDialogHtml()
//        renderEntityEditDialogComponent()
//        renderEntityEditDialogHtml()
        renderEntityDetailViews()
        renderEntityDetailDtoServices()
        renderEntityCreatePages()
        renderEntityEditPages()
        renderBlotterPages()
        renderEntityCrudRoutes()
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
        renderBlotterDto()
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

        val providerServices = def.allTypeaheadDefs.map { it.angularServiceClassName } +
                chipFields.map { it.serviceClassName }

        when (def.angularFormSystem) {

            AngularFormSystem.REACTIVE ->
                AngularReactiveFormComponentRenderer_old(
                    def,
                    angularComponentNames,
                    providerServices,
                    chipFields
                ).renderToDir(this.typescriptOutputDir)

            AngularFormSystem.SIGNAL ->
                AngularSignalFormComponentRenderer(
                    def,
                    angularComponentNames,
                    providerServices
                ).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderSearchableServices() {

        this.modelDef.searchableDtoDefs
            .filter { it.withGeneratedTypescriptService.value }
            .map { it.searchDtoDef }.forEach {
            SearchDtoServiceTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderBlotterDto() {

        this.modelDef.blotterDefs.filter { it.disableRendering == false }.forEach { blotterDef ->

            renderTypescriptInterface(
                renderedFilePath = blotterDef.dtoDef.typescriptRenderedFilePath,
                className = blotterDef.dtoDef.uqcn,
                fields = blotterDef.dtoClassFields,
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


    private fun renderBlotterComponents() {

        this.modelDef.blotterDefs.forEach {

            when (it.searchModelType) {
                SearchModelType.AG_GRID -> {

                    val entityIsReferencedByForeignKeys = it.blotterSourceDef.rootEntityDef?.let { rootEntityDef ->
                        this.modelDef.entityIsReferencedByForeignKeys(rootEntityDef)
                    } ?: false

                    AgGridBlotterComponentRenderer(
                        it,
                        it.entityDetailViewDef,
                        it.entityEditPageDef,
                        it.entityCreatePageDef,
                        entityIsReferencedByForeignKeys,
                        this.modelDef.authoritiesDef
                    ).renderToDir(this.typescriptOutputDir)
                }

                SearchModelType.MAIA -> BlotterComponentRenderer(it).renderToDir(this.typescriptOutputDir)

            }

        }

    }


    private fun renderEntityDetailViews() {

        val blotterPageByEntity = this.modelDef.blotterPageDefs
            .mapNotNull { pageDef -> pageDef.blotterDef.blotterSourceDef.rootEntityDef?.let { it to pageDef } }
            .toMap()

        this.modelDef.entityDetailViewDefs.forEach {

            EntityDetailViewComponentRenderer(it).renderToDir(this.typescriptOutputDir)
            EntityDetailViewPageComponentRenderer(it, this.modelDef.authoritiesDef, blotterPageByEntity[it.entityDef]).renderToDir(this.typescriptOutputDir)
            EntityDetailViewContentHtmlRenderer(it).renderToDir(this.typescriptOutputDir)
            EntityDetailViewPageHtmlRenderer(it, blotterPageByEntity[it.entityDef]).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderEntityDetailDtoServices() {

        this.modelDef.entityDetailViewDefs.forEach {

            EntityDetailDtoServiceTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderEntityCreatePages() {

        val blotterPageByEntity = this.modelDef.blotterPageDefs
            .mapNotNull { pageDef -> pageDef.blotterDef.blotterSourceDef.rootEntityDef?.let { it to pageDef } }
            .toMap()

        this.modelDef.entityCreatePageDefs.forEach { entityCreatePageDef ->

            // TODO account for Signal forms vs Reactive forms

            val viewPageDef = this.modelDef.findViewEntityPage(entityCreatePageDef.entityDef)

            val blotterPageDef = blotterPageByEntity[entityCreatePageDef.entityDef]

            val chipFields = manyToManyChipFieldsFor(entityCreatePageDef.entityDef, entityCreatePageDef.entityDef.manyToManyAssociations)

            val providerServices = entityCreatePageDef.entityDef.allTypeaheadDefs.map { it.angularServiceClassName } +
                chipFields.map { it.serviceClassName }

            val angularFormDef = AngularFormDef(
                componentBaseName = entityCreatePageDef.entityDef.crudAngularComponentBaseName,
                requestDtoDef = entityCreatePageDef.createApiDef.requestDtoDef,
                featureNames = sortedSetOf(),
                htmlFormFields = entityCreatePageDef.createApiDef.htmlFormFields,
                formModelFields = entityCreatePageDef.createApiDef.htmlFormFields,
                delegateFormSubmission = DelegateFormSubmission.FALSE, // TODO
                emitEventOnSuccess = EmitEventsOnSuccess.FALSE,
                emitEventOnError = EmitEventsOnError.FALSE,
                onSuccessUrl = viewPageDef?.viewPageUrl,
                submitButtonText = null,
                inlineFormOrDialog = InlineFormOrDialog.INLINE_FORM,
                formPurpose = FormPurpose.create,
                context = null,
                dialogTitle = null,
                multiFieldDatabaseIndexDefs = emptyList(),
                onSubmitServiceFunctionName = "create",
                formServiceTypescriptImport = entityCreatePageDef.entityDef.crudAngularComponentNames.serviceTypescriptImport,
                angularFormSystem = AngularFormSystem.REACTIVE,
                fetchForEditDtoDef = null,
                entityIdInjectType = "string"
            )

            AngularReactiveFormComponentRenderer(
                angularFormDef,
                entityCreatePageDef.createFormAngularComponentNames,
                providerServices,
                chipFields
            ).renderToDir(this.typescriptOutputDir)

            EntityCreateFormScssRenderer(entityCreatePageDef).renderToDir(this.typescriptOutputDir)
            EntityCreateReactiveFormHtmlRenderer(entityCreatePageDef, chipFields).renderToDir(this.typescriptOutputDir)
            EntityCreateFormPageComponentRenderer(entityCreatePageDef, blotterPageDef).renderToDir(this.typescriptOutputDir)
            EntityCreatePageHtmlRenderer(entityCreatePageDef).renderToDir(this.typescriptOutputDir)

        }

    }


    private fun renderEntityEditPages() {

        this.modelDef.entityEditPageDefs.forEach { entityEditPageDef ->

            // TODO account for Signal forms vs Reactive forms

            val viewPageDef = this.modelDef.findViewEntityPage(entityEditPageDef.entityDef)

            val fetchForEditDtoDef = entityEditPageDef.entityDef.fetchForEditDtoDef

            val chipFields = manyToManyChipFieldsFor(entityEditPageDef.entityDef, entityEditPageDef.entityDef.manyToManyAssociations)

            val angularFormDef = AngularFormDef(
                angularFormSystem = AngularFormSystem.REACTIVE,
                componentBaseName = entityEditPageDef.entityDef.crudAngularComponentBaseName,
                context = null,
                formPurpose = FormPurpose.edit,
                delegateFormSubmission = DelegateFormSubmission.FALSE, // TODO
                dialogTitle = null,
                emitEventOnError = EmitEventsOnError.FALSE,
                emitEventOnSuccess = EmitEventsOnSuccess.FALSE,
                entityIdInjectType = "string",
                featureNames = sortedSetOf(),
                fetchForEditDtoDef = fetchForEditDtoDef,
                formModelFields = entityEditPageDef.updateApiDef.formGroupFields,
                formServiceTypescriptImport = entityEditPageDef.entityDef.crudAngularComponentNames.serviceTypescriptImport,
                htmlFormFields = entityEditPageDef.updateApiDef.htmlFormFields,
                inlineFormOrDialog = InlineFormOrDialog.INLINE_FORM,
                multiFieldDatabaseIndexDefs = emptyList(),
                onSubmitServiceFunctionName = "edit",
                onSuccessUrl = viewPageDef?.viewPageUrl,
                requestDtoDef = entityEditPageDef.updateApiDef.requestDtoDef,
                submitButtonText = null
            )

            val providerServices = angularFormDef.allTypeaheadDefs.map { it.angularServiceClassName } +
                chipFields.map { it.serviceClassName }

            AngularReactiveFormComponentRenderer(
                angularFormDef,
                entityEditPageDef.editFormAngularComponentNames,
                providerServices,
                chipFields
            ).renderToDir(this.typescriptOutputDir)

//            EntityEditFormComponentRenderer(entityEditPageDef).renderToDir(this.typescriptOutputDir)
            EntityEditFormScssRenderer(entityEditPageDef).renderToDir(this.typescriptOutputDir)
            EntityEditReactiveFormHtmlRenderer(entityEditPageDef.updateApiDef, entityEditPageDef.editFormAngularComponentNames, chipFields).renderToDir(this.typescriptOutputDir)
            EntityEditFormPageComponentRenderer(entityEditPageDef).renderToDir(this.typescriptOutputDir)
            EntityEditPageHtmlRenderer(entityEditPageDef).renderToDir(this.typescriptOutputDir)

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


    private fun renderBlotterPages() {

        this.modelDef.blotterPageDefs.forEach { pageDef ->
            BlotterPageComponentRenderer(pageDef).renderToDir(this.typescriptOutputDir)
            BlotterPageHtmlRenderer(pageDef).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderEntityCrudRoutes() {

        val blotterPageByEntity = this.modelDef.blotterPageDefs
            .mapNotNull { pageDef -> pageDef.blotterDef.blotterSourceDef.rootEntityDef?.let { it to pageDef } }
            .toMap()

        val viewPageByEntity = this.modelDef.entityDetailViewDefs
            .associateBy { it.entityDef }

        val createPageByEntity = this.modelDef.entityCreatePageDefs
            .associateBy { it.entityDef }

        val editPageByEntity = this.modelDef.entityEditPageDefs
            .associateBy { it.entityDef }

        val allEntities = (blotterPageByEntity.keys + viewPageByEntity.keys + createPageByEntity.keys + editPageByEntity.keys).toSet()

        allEntities.forEach { entityDef ->
            EntityCrudRoutesRenderer(
                entityDef = entityDef,
                blotterPageDef = blotterPageByEntity[entityDef],
                entityDetailViewDef = viewPageByEntity[entityDef],
                entityCreatePageDef = createPageByEntity[entityDef],
                entityEditPageDef = editPageByEntity[entityDef],
            ).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderDtoServices() {

        this.modelDef.blotterDefs.forEach {
            BlotterServiceTypescriptRenderer(it).renderToDir(this.typescriptOutputDir)
        }

    }


    // TODO Delete after cooling off: 2026-05-23
    private fun renderEntityCreateDialogHtml() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->
            entityCrudApiDef.createApiDef?.let { apiDef ->
                renderEntityCreateDialogHtml(apiDef)
            }
        }

    }


    private fun renderEntityCreateDialogHtml(apiDef: EntityCreateApiDef) {

        // TODO MTM:
        val chipFields = manyToManyChipFieldsFor(apiDef.entityDef, apiDef.entityDef.manyToManyAssociations)
        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityCreateDialogReactiveFormHtmlRenderer(apiDef, chipFields).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityCreateDialogHtmlRenderer(apiDef, chipFields).renderToDir(this.typescriptOutputDir)
        }

    }


    private fun renderEntityCreateHtmlForm(apiDef: EntityCreateApiDef) {

        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> {} // EntityCreateReactiveFormHtmlRenderer migrated to EntityCreatePageDef
            AngularFormSystem.SIGNAL -> EntityCreateFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
        }


    }


    private fun renderEntityEditHtmlForm(apiDef: EntityUpdateApiDef) {

        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityEditReactiveFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityEditSignalFormHtmlRenderer(apiDef).renderToDir(this.typescriptOutputDir)
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

        this.modelDef.authoritiesDef?.let { authoritiesDef ->

            AuthApiServiceRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            AuthGuardRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            AuthServiceTypescriptRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            CurrentUserStoreRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            SigninRequestDtoRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)
            UserSummaryDtoRenderer(authoritiesDef).renderToDir(this.typescriptOutputDir)

        }

    }


    // TODO Delete after cooling off: 2026-05-23
    private fun renderEntityCreateDialogComponent() {

        this.modelDef.entityCrudApiDefs
            .mapNotNull { it.createApiDef }
            .filter { it.entityDef.isConcrete }
            .forEach {

                // TODO MTM:
                val chipFields = manyToManyChipFieldsFor(it.entityDef, it.entityDef.manyToManyAssociations)
                renderEntityForm(it.angularDialogDef, it.angularDialogComponentNames, chipFields)
                EntityCreateDialogScssRenderer(it).renderToDir(this.typescriptOutputDir)

            }

    }


    // TODO Delete after cooling off: 2026-05-23
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
        val chipFields = manyToManyChipFieldsFor(apiDef.entityDef, apiDef.entityDef.manyToManyAssociations)
        when (apiDef.angularDialogDef.angularFormSystem) {
            AngularFormSystem.REACTIVE -> EntityEditReactiveDialogHtmlRenderer(apiDef, chipFields).renderToDir(this.typescriptOutputDir)
            AngularFormSystem.SIGNAL -> EntityEditDialogHtmlRenderer(apiDef, chipFields).renderToDir(this.typescriptOutputDir)
        }

    }


    // TODO Delete after cooling off: 2026-05-23
    private fun renderEntityEditDialogComponent() {

        this.modelDef.entityCrudApiDefs.filter { it.entityDef.isConcrete }.forEach { entityCrudApiDef ->
            entityCrudApiDef.updateApiDef?.let { apiDef ->
                if (entityCrudApiDef.entityDef.isModifiable) {
                    // TODO MTM:
                    val chipFields = manyToManyChipFieldsFor(apiDef.entityDef, apiDef.entityDef.manyToManyAssociations)
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

        modelDef.entityDetailViewDefs
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

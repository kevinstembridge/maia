package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.CreateOrEdit
import org.maiaframework.gen.spec.definition.flags.DelegateFormSubmission
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnError
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnSuccess
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.util.TreeSet


class EntityCreateApiDef(
    entityDef: EntityDef,
    val crudApiDef: CrudApiDef,
    moduleName: ModuleName?,
    angularFormSystem: AngularFormSystem
) : AbstractEntityApiDef(entityDef) {


    private val dtoBaseName = DtoBaseName(this.entityDef.entityBaseName.withSuffix("Create").value)


    private val angularComponentBaseName = AngularComponentBaseName(this.entityDef.entityBaseName.value)


    val htmlFormFields = this.entityDef.allEntityFields
        .filter { field -> field.isCreatableByUser.value }
        .map { entityFieldDef ->

            val classFieldDef = entityFieldDef.classFieldDef

            val typeaheadRequiredValidatorFunctionName = if (classFieldDef.typeaheadDef != null) {
                entityFieldDef.typeaheadRequiredValidatorFunctionName
            } else {
                null
            }

            val typeaheadRequiredValidatorTypescriptImport = if (classFieldDef.typeaheadDef != null) {
                entityFieldDef.typeaheadRequiredValidatorTypescriptImport
            } else {
                null
            }

            AngularFormFieldDef(
                dtoBaseName,
                classFieldDef,
                classFieldDef.displayName?.let { FieldLabel(it.value) },
                true,
                classFieldDef.formPlaceholderText,
                HtmlInputType.text,
                false,
                autocomplete = null,
                typeaheadRequiredValidatorFunctionName,
                typeaheadRequiredValidatorTypescriptImport,
                asyncValidatorDef = this.entityDef.findUniqueDatabaseIndexDefFor(classFieldDef.classFieldName)?.asyncValidator
            )

        }


    private val dtoFields = this.entityDef.allEntityFields
        .asSequence()
        .filterNot { it.isDerived.value }
        .filterNot { it.isHardcoded.value }
        .map { it.classFieldDef }
        .plus(crudApiDef.context?.let {
            ClassFieldDef(
                classFieldName = ClassFieldName("context"),
                fieldType = FieldTypes.requestDto(it)
            )
        }).filterNotNull()
        .filter { field ->
            val classFieldName = field.classFieldName
            (
                classFieldName != ClassFieldName.id
                && classFieldName != ClassFieldName.createdById
                && classFieldName != ClassFieldName.createdByUsername
                && classFieldName != ClassFieldName.createdTimestampUtc
                && classFieldName != ClassFieldName.version
                && classFieldName != ClassFieldName.lastModifiedById
                && classFieldName != ClassFieldName.lastModifiedByUsername
                && classFieldName != ClassFieldName.lastModifiedTimestampUtc
                && classFieldName != ClassFieldName.lifecycleState
            )
        }.map { RequestDtoFieldDef(
            it,
            databaseIndexDef = this.entityDef.findUniqueDatabaseIndexDefFor(it.classFieldName))
        }.toList()


    val preAuthorizeExpression = this.crudApiDef.authority?.let { PreAuthorizeExpression("hasAuthority('$it')") }


    override val requestDtoDef = RequestDtoDef(
        dtoBaseName,
        packageName = entityDef.packageName,
        dtoFieldDefs = this.dtoFields,
        preAuthorizeExpression = preAuthorizeExpression,
        moduleName = moduleName
    )


    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"


    val endpointUrl = "/api/$modulePath${this.entityDef.entityBaseName.toSnakeCase()}/create"


    override val angularDialogComponentNames = AngularComponentNames(this.entityDef.packageName, "${entityDef.entityBaseName}CreateDialog")


    override val angularFormComponentNames = AngularComponentNames(this.entityDef.packageName, "${entityDef.entityBaseName}CreateForm")


    private val entityKeyKebabCase = this.entityDef.entityBaseName.toKebabCase()


    val angularDialogComponentHtmlFilePath = angularDialogComponentNames.htmlRenderedFilePath


    val angularEntityFormComponentHtmlFilePath = angularFormComponentNames.htmlRenderedFilePath


    val angularDialogScssPath = angularDialogComponentNames.componentScssRenderedFilePath


    val angularEntityFormScssPath = angularFormComponentNames.componentScssRenderedFilePath


    val angularDialogComponentImportStatement = angularDialogComponentNames.componentImportStatement


    val angularDialogDef = AngularFormDef(
        angularComponentBaseName,
        requestDtoDef,
        featureNames = TreeSet(),
        htmlFormFields,
        htmlFormFields,
        delegateFormSubmission = DelegateFormSubmission.FALSE,
        emitEventOnSuccess = EmitEventsOnSuccess.FALSE,
        emitEventOnError = EmitEventsOnError.FALSE,
        onSuccessUrl = null,
        submitButtonText = null,
        InlineFormOrDialog.DIALOG,
        CreateOrEdit.create,
        context = crudApiDef.context,
        dialogTitle = null,
        multiFieldDatabaseIndexDefs = entityDef.multiFieldUniqueIndexDefs,
        onSubmitServiceFunctionName = "create",
        entityDef.crudAngularComponentNames.serviceTypescriptImport,
        angularFormSystem
    )


    val angularInlineFormDef = if (this.crudApiDef.withEntityForm) {
        AngularFormDef(
            angularComponentBaseName,
            requestDtoDef,
            featureNames = TreeSet(),
            htmlFormFields,
            htmlFormFields,
            delegateFormSubmission = DelegateFormSubmission.FALSE,
            emitEventOnSuccess = EmitEventsOnSuccess.FALSE,
            emitEventOnError = EmitEventsOnError.FALSE,
            onSuccessUrl = null,
            submitButtonText = null,
            InlineFormOrDialog.INLINE_FORM,
            CreateOrEdit.create,
            context = crudApiDef.context,
            dialogTitle = null,
            multiFieldDatabaseIndexDefs = entityDef.databaseIndexDefs,
            onSubmitServiceFunctionName = "create",
            entityDef.crudAngularComponentNames.serviceTypescriptImport,
            angularFormSystem
        )
    } else {
        null
    }


}

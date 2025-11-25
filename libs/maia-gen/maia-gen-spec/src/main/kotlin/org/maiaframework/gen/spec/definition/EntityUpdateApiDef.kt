package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.CreateOrEdit
import org.maiaframework.gen.spec.definition.flags.DelegateFormSubmission
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnError
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnSuccess
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import java.util.TreeSet

class EntityUpdateApiDef(
    entityDef: EntityDef,
    val crudApiDef: CrudApiDef,
    private val moduleName: ModuleName?
) : AbstractEntityApiDef(entityDef) {


    private val dtoBaseName = DtoBaseName(this.entityDef.entityBaseName.withSuffix("Update").value)


    private val angularComponentBaseName = AngularComponentBaseName(this.entityDef.entityBaseName.value)


    val formGroupFields = this.entityDef.allEntityFields
        .filter { entityFieldDef ->
            val classFieldDef = entityFieldDef.classFieldDef
            val classFieldName = classFieldDef.classFieldName
            (classFieldName != ClassFieldName.createdTimestampUtc && classFieldName != ClassFieldName.lastModifiedTimestampUtc)
        }.map {

            val classFieldDef = it.classFieldDef

            val typeaheadRequiredValidatorFunctionName = if (classFieldDef.typeaheadDef != null) {
                it.typeaheadRequiredValidatorFunctionName
            } else {
                null
            }

            val typeaheadRequiredValidatorTypescriptImport = if (classFieldDef.typeaheadDef != null) {
                it.typeaheadRequiredValidatorTypescriptImport
            } else {
                null
            }

            AngularFormFieldDef(
                dtoBaseName,
                classFieldDef,
                classFieldDef.displayName?.let { name -> FieldLabel(name.value) },
                renderFieldLabel = true,
                classFieldDef.formPlaceholderText,
                HtmlInputType.text,
                autoFocus = false,
                autocomplete = null,
                typeaheadRequiredValidatorFunctionName,
                typeaheadRequiredValidatorTypescriptImport,
                asyncValidatorDef = this.entityDef.findUniqueDatabaseIndexDefFor(classFieldDef.classFieldName)?.asyncValidator
            )
        }


    val htmlFormFields: List<AngularFormFieldDef> = this.entityDef.allEntityFields
        .filter { entityFieldDef -> entityFieldDef.classFieldDef.isEditableByUser.value }
        .map {

            val classFieldDef = it.classFieldDef

            val typeaheadRequiredValidatorFunctionName = if (classFieldDef.typeaheadDef != null) {
                it.typeaheadRequiredValidatorFunctionName
            } else {
                null
            }

            val typeaheadRequiredValidatorTypescriptImport = if (classFieldDef.typeaheadDef != null) {
                it.typeaheadRequiredValidatorTypescriptImport
            } else {
                null
            }

            AngularFormFieldDef(
                dtoBaseName,
                classFieldDef,
                classFieldDef.displayName?.let { name -> FieldLabel(name.value) },
                renderFieldLabel = true,
                classFieldDef.formPlaceholderText,
                HtmlInputType.text,
                autoFocus = false,
                autocomplete = null,
                typeaheadRequiredValidatorFunctionName,
                typeaheadRequiredValidatorTypescriptImport,
                asyncValidatorDef = this.entityDef.findUniqueDatabaseIndexDefFor(classFieldDef.classFieldName)?.asyncValidator
            )

        }


    private val dtoFields: List<RequestDtoFieldDef> = this.entityDef.allEntityFields
        .filter { it.classFieldDef.isEditableByUser.value || it.isPrimaryKey.value || it.isVersionField }
        .map { it.classFieldDef }
        .map { RequestDtoFieldDef(it, this.entityDef.findUniqueDatabaseIndexDefFor(it.classFieldName)) }


    val preAuthorizeExpression = this.crudApiDef.authority?.let { PreAuthorizeExpression("hasAuthority('$it')") }


    override val requestDtoDef = RequestDtoDef(
        dtoBaseName,
        packageName = entityDef.packageName,
        dtoFieldDefs = this.dtoFields,
        preAuthorizeExpression = preAuthorizeExpression,
        moduleName = moduleName
    )


    val inlineEditDtoDefs: List<InlineEditDtoDef>
        get() = this.entityDef.allUserEditableFields
            .map { entityFieldDef ->

                val requestDtoFieldDefs = this.entityDef.primaryKeyFields
                    .plus(this.entityDef.versionField)
                    .plus(entityFieldDef)
                    .filterNotNull()
                    .map {
                        RequestDtoFieldDef(
                            it.classFieldDef,
                            this.entityDef.findUniqueDatabaseIndexDefFor(it.classFieldName)
                        )
                    }

                val inlineEditDtoBaseName = DtoBaseName(this.dtoBaseName.value + "_" + entityFieldDef.classFieldName)
                val requestDtoDef = RequestDtoDef(
                    dtoBaseName = inlineEditDtoBaseName,
                    packageName = this.entityDef.packageName,
                    dtoFieldDefs = requestDtoFieldDefs,
                    preAuthorizeExpression = this.preAuthorizeExpression,
                    moduleName = moduleName
                )
                InlineEditDtoDef(requestDtoDef, entityFieldDef)

            }


    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"


    val endpointUrl = "/api/${modulePath}${this.entityDef.entityBaseName.toSnakeCase()}/update"


    override val angularDialogComponentNames = AngularComponentNames(this.entityDef.packageName, "${entityDef.entityBaseName}EditDialog")


    override val angularFormComponentNames = AngularComponentNames(this.entityDef.packageName, "${entityDef.entityBaseName}EditForm")


    val angularDialogDef = AngularFormDef(
        angularComponentBaseName,
        requestDtoDef,
        TreeSet(),
        htmlFormFields,
        formGroupFields,
        delegateFormSubmission = DelegateFormSubmission.FALSE,
        emitEventOnSuccess = EmitEventsOnSuccess.FALSE,
        emitEventOnError = EmitEventsOnError.FALSE,
        onSuccessUrl = null,
        submitButtonText = null,
        InlineFormOrDialog.DIALOG,
        CreateOrEdit.edit,
        context = crudApiDef.context,
        dialogTitle = null,
        multiFieldDatabaseIndexDefs = entityDef.multiFieldUniqueIndexDefs,
        onSubmitServiceFunctionName = "edit",
        entityDef.crudAngularComponentNames.serviceTypescriptImport
    )


    val angularInlineFormDef = if (crudApiDef.withEntityForm) {
        AngularFormDef(
            angularComponentBaseName,
            requestDtoDef,
            TreeSet(),
            htmlFormFields,
            formGroupFields,
            delegateFormSubmission = DelegateFormSubmission.FALSE,
            emitEventOnSuccess = EmitEventsOnSuccess.FALSE,
            emitEventOnError = EmitEventsOnError.FALSE,
            onSuccessUrl = null,
            submitButtonText = null,
            InlineFormOrDialog.INLINE_FORM,
            CreateOrEdit.edit,
            context = crudApiDef.context,
            dialogTitle = null,
            multiFieldDatabaseIndexDefs = entityDef.databaseIndexDefs,
            onSubmitServiceFunctionName = "edit",
            entityDef.crudAngularComponentNames.serviceTypescriptImport
        )
    } else {
        null
    }


}

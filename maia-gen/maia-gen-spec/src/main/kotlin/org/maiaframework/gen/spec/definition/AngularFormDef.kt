package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.flags.CreateOrEdit
import org.maiaframework.gen.spec.definition.flags.DelegateFormSubmission
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnError
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnSuccess
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions
import java.util.Locale
import java.util.Objects
import java.util.SortedSet


class AngularFormDef(
    val componentBaseName: AngularComponentBaseName,
    val requestDtoDef: RequestDtoDef,
    val featureNames: SortedSet<String>,
    val htmlFormFields: List<AngularFormFieldDef>,
    val formModelFields: List<AngularFormFieldDef>,
    val delegateFormSubmission: DelegateFormSubmission,
    val emitEventOnSuccess: EmitEventsOnSuccess,
    val emitEventOnError: EmitEventsOnError,
    val onSuccessUrl: String?,
    val submitButtonText: String?,
    val inlineFormOrDialog: InlineFormOrDialog,
    val createOrEdit: CreateOrEdit?,
    val context: RequestDtoDef?,
    val dialogTitle: String?,
    val multiFieldDatabaseIndexDefs: List<DatabaseIndexDef>,
    val onSubmitServiceFunctionName: String,
    formServiceTypescriptImport: TypescriptImport? = null,
    val angularFormSystem: AngularFormSystem,
    val fetchForEditDtoDef: FetchForEditDtoDef? = null,
    val entityIdInjectType: String = "string"
) {


    init {

        if (htmlFormFields.filter { it.autoFocus }.size > 1) {
            throw RuntimeException("More than one autoFocus field on form ${this.requestDtoDef.dtoBaseName}")
        }

    }


    private val htmlFormKey: HtmlFormKey = HtmlFormKey("${requestDtoDef.dtoBaseName}${StringFunctions.firstToUpper(inlineFormOrDialog.name.lowercase(Locale.getDefault()))}")


    private val componentNamesSuffix = when (createOrEdit) {
        CreateOrEdit.create -> when (inlineFormOrDialog) {
            InlineFormOrDialog.INLINE_FORM -> "CreateForm"
            InlineFormOrDialog.DIALOG -> "CreateDialog"
        }
        CreateOrEdit.edit -> when (inlineFormOrDialog) {
            InlineFormOrDialog.INLINE_FORM -> "EditForm"
            InlineFormOrDialog.DIALOG -> "EditDialog"
        }
        null -> when (inlineFormOrDialog) {
            InlineFormOrDialog.INLINE_FORM -> "Form"
            InlineFormOrDialog.DIALOG -> "Dialog"
        }
    }


    val componentNames = AngularComponentNames(requestDtoDef.packageName, "$componentBaseName$componentNamesSuffix")


    val htmlFormName = componentNames.htmlFormName


    val scssRenderedFilePath = componentNames.componentScssRenderedFilePath


    val formComponentClassName = componentNames.componentName


    val angularServiceRenderedFilePath = componentNames.apiServiceRenderedFilePath


    val formServiceTypescriptImport = formServiceTypescriptImport ?: componentNames.apiServiceTypescriptImport


    val formServiceClassName = this.formServiceTypescriptImport.name


    val formHtmlFilePath = componentNames.htmlRenderedFilePath


    val allTypeaheadDefs = this.requestDtoDef.classFieldDefs.mapNotNull { it.typeaheadDef }


    val hasAnyMatSelectFields = htmlFormFields.any { it.isEnum || it.isEnumList }


    val hasAnyInstantFields = htmlFormFields.any { it.fieldType is InstantFieldType }


    val hasAnyBooleanFields = htmlFormFields.any {
        it.fieldType is BooleanFieldType
            || it.fieldType is BooleanTypeFieldType
            || it.fieldType is BooleanValueClassFieldType
    }


    val hasAnyValidationConstraints = this.formModelFields.any { it.hasAnyValidationConstraint() }


    val enumsForMatSelectFields = htmlFormFields
        .filter { it.isEnum || it.isEnumList }
        .map { it.enumDef ?: it.enumListDef!! }


    val uniqueIndexDefs = multiFieldDatabaseIndexDefs.filter { it.isUnique }.filter { it.isNotIdAndVersionIndex }


    val multiFieldUniqueIndexDefs: List<DatabaseIndexDef>
        get() = multiFieldDatabaseIndexDefs
            .filter { it.isMultiField }
            .filter { it.isUnique }
            .filter { it.indexDef.isForIdAndVersion == false }


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val entityDef = other as AngularFormDef?
        return htmlFormKey == entityDef!!.htmlFormKey

    }


    override fun hashCode(): Int {

        return Objects.hash(componentNames.baseName)

    }


    override fun toString(): String {

        return "AngularFormDef{" + this.componentNames.baseName + "}"

    }


}

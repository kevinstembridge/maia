package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.common.BlankStringException.Companion.throwIfBlank
import org.maiaframework.gen.spec.definition.AngularComponentBaseName
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.AngularFormType
import org.maiaframework.gen.spec.definition.FieldLabel
import org.maiaframework.gen.spec.definition.FormAutocompleteText
import org.maiaframework.gen.spec.definition.FormPlaceholderText
import org.maiaframework.gen.spec.definition.HtmlInputType
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.RequestDtoFieldDef
import org.maiaframework.gen.spec.definition.flags.CreateOrEdit
import org.maiaframework.gen.spec.definition.flags.DelegateFormSubmission
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnError
import org.maiaframework.gen.spec.definition.flags.EmitEventsOnSuccess
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.flags.TextCase
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotBlankConstraintDef


@MaiaDslMarker
class AngularFormDefBuilder(
    private val requestDtoDef: RequestDtoDef,
    private val angularComponentBaseName: AngularComponentBaseName
) {


    private val fieldBuilders = ArrayList<FieldBuilder>()


    private val featureNames = mutableListOf<String>()


    private var onSuccessUrl: String? = null


    private var submitButtonText: String? = null


    private var delegateFormSubmission: DelegateFormSubmission = DelegateFormSubmission.FALSE


    private var emitEventOnSuccess: EmitEventsOnSuccess = EmitEventsOnSuccess.FALSE


    private var emitEventOnError: EmitEventsOnError = EmitEventsOnError.FALSE


    private var context: RequestDtoDef? = null


    private var dialogTitle: String? = null


    private var inlineFormOrDialog = InlineFormOrDialog.INLINE_FORM


    private var createOrEdit: CreateOrEdit? = null


    private var angularFormType: AngularFormType = AngularFormType.REACTIVE


    fun field(
        fieldName: String,
        init: (FieldBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = FieldBuilder(fieldName)
        this.fieldBuilders.add(fieldBuilder)

        init?.invoke(fieldBuilder)

    }


    fun build(): AngularFormDef {

        val fieldDefs = this.fieldBuilders.map { buildField(it) }

        return AngularFormDef(
            this.angularComponentBaseName,
            this.requestDtoDef,
            this.featureNames.toSortedSet(),
            fieldDefs,
            fieldDefs,
            this.delegateFormSubmission,
            this.emitEventOnSuccess,
            this.emitEventOnError,
            this.onSuccessUrl,
            this.submitButtonText,
            this.inlineFormOrDialog,
            this.createOrEdit,
            this.context,
            this.dialogTitle,
            multiFieldDatabaseIndexDefs = emptyList(),
            onSubmitServiceFunctionName = "sendRequest",
            angularFormType = this.angularFormType
        )

    }


    private fun buildField(fieldBuilder: FieldBuilder): AngularFormFieldDef {

        val requestDtoFieldDef = classFieldDefFor(fieldBuilder)

        val fieldLabel = fieldBuilder.fieldLabel
            ?: requestDtoFieldDef.classFieldDef.displayName?.let { FieldLabel(it.value) }
            ?: throw RuntimeException("No field label or display name provided for field named [${requestDtoFieldDef.classFieldDef.classFieldName}] on requestDto [${requestDtoDef.dtoBaseName}] ")

        return AngularFormFieldDef(
            this.requestDtoDef.dtoBaseName,
            requestDtoFieldDef.classFieldDef,
            fieldLabel,
            fieldBuilder.renderFieldLabel,
            fieldBuilder.placeholder,
            fieldBuilder.htmlInputType,
            fieldBuilder.autoFocus,
            fieldBuilder.autocomplete,
            typeaheadRequiredValidatorFunctionName = null,
            typeaheadRequiredValidatorTypescriptImport = null,
            asyncValidatorDef = requestDtoFieldDef.databaseIndexDef?.asyncValidator,
        )

    }


    private fun classFieldDefFor(fieldBuilder: FieldBuilder): RequestDtoFieldDef {

        val requestDtoFieldDef = this.requestDtoDef.findFieldByName(fieldBuilder.fieldName)
        return fieldBuilder.enrichClassFieldDef(requestDtoFieldDef)

    }


    fun featureEventCapture(vararg featureNames: String): AngularFormDefBuilder {

        this.featureNames.addAll(featureNames)
        return this

    }


    fun onSuccessUrl(onSuccessUrl: String): AngularFormDefBuilder {

        this.onSuccessUrl = onSuccessUrl
        return this

    }

    fun submitButtonText(text: String): AngularFormDefBuilder {

        this.submitButtonText = text
        return this

    }


    fun delegateFormSubmission(): AngularFormDefBuilder {

        this.delegateFormSubmission = DelegateFormSubmission.TRUE
        return this

    }


    fun emitEventOnSuccess(): AngularFormDefBuilder {

        this.emitEventOnSuccess = EmitEventsOnSuccess.TRUE
        return this

    }


    fun emitEventOnError(): AngularFormDefBuilder {

        this.emitEventOnError = EmitEventsOnError.TRUE
        return this

    }


    fun withContextRequestDto(context: RequestDtoDef): AngularFormDefBuilder {

        this.context = context
        return this

    }


    fun asDialog(title: String): AngularFormDefBuilder {

        this.inlineFormOrDialog = InlineFormOrDialog.DIALOG
        this.dialogTitle = title
        return this

    }


    fun asCreateOrEdit(createOrEdit: CreateOrEdit): AngularFormDefBuilder {

        this.createOrEdit = createOrEdit
        return this

    }


    fun ofType(angularFormType: AngularFormType): AngularFormDefBuilder {

        this.angularFormType = angularFormType
        return this

    }


    class FieldBuilder(
        fieldName: String,
    ) {


        val fieldName: ClassFieldName = ClassFieldName(fieldName)


        var fieldLabel: FieldLabel? = null


        var placeholder: FormPlaceholderText? = null


        var autocomplete: FormAutocompleteText? = null


        var htmlInputType = HtmlInputType.text


        var textCase: TextCase = TextCase.ORIGINAL


        var autoFocus: Boolean = false


        var renderFieldLabel: Boolean = true


        private val validationConstraints = mutableSetOf<AbstractValidationConstraintDef>()


        fun withLabel(fieldLabel: String) {

            this.fieldLabel = if (fieldLabel.isNotBlank()) FieldLabel(fieldLabel) else null

        }


        /**
         *  The form field will not have a separate label HTML element. This might be useful
         *  if the placeholder of the field is sufficient to indicate what the field represents.
         */
        fun withoutSeparateFieldLabel() {

            this.renderFieldLabel = false

        }


        fun withPlaceholder(placeholder: String) {

            this.placeholder = if (placeholder.isNotBlank()) FormPlaceholderText(placeholder) else null

        }


        fun withHtmlAutocompleteAttribute(autocomplete: String) {

            this.autocomplete = if (autocomplete.isNotBlank()) FormAutocompleteText(autocomplete) else null

        }


        fun withAutoFocus() {

            this.autoFocus = true

        }


        fun withHtmlInputType(htmlInputType: HtmlInputType) {

            this.htmlInputType = htmlInputType

        }


        fun forceToUppercase() {

            this.textCase = TextCase.UPPER

        }


        fun forceToLowercase() {

            this.textCase = TextCase.LOWER

        }


        fun withNotBlankConstraint() {

            addValidationConstraint(NotBlankConstraintDef.INSTANCE)

        }


        private fun addValidationConstraint(validationConstraint: AbstractValidationConstraintDef) {

            this.validationConstraints.add(validationConstraint)

        }


        fun enrichClassFieldDef(requestDtoFieldDef: RequestDtoFieldDef): RequestDtoFieldDef {

            return requestDtoFieldDef.copyWith(this.validationConstraints)

        }


    }


}
